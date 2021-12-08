package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.repository.NodeRepository;

/*
TTODA algorithm

R & M are percentage or 0..1 

//TODO check formula: total edge + faredge capacity?
rN = percentage of CPU requested wrt total capacity (expressed as 0..1)
mN = percentage of MEM requested wrt total capacity (expressed as 0..1)
tN = rN + mN

//TODO check formula: reverse?
wN = tN / service priority

instantiationN = time (ms) to load service image (0 if images are pre-loaded)
latencyN = time (ms) to communicate from edge to cloud nodes
loadingN = time (ms) to have a service ready

#for CPU/MEM constrained services

from function (26) in ECODA in https://www.techrxiv.org/articles/preprint/An_Optimization_Framework_for_Edge-to-Cloud_Offloading_of_Kubernetes_Pods_in_V2X_Scenarios/16725643/1

for all service[i] compute score(service[i]) = wN * (instantiationN + (latencyN + loadingN) * (1 - 2/tN)	

all infrastructures are organized as "node groups", ordered by position: far-edge(1), edge(2), finally cloud(3).
for all nodegroups, check if all services fit: 
- if (resources > capability max threshold), use score(i) with Max score to choose which service to offload to the node_group with worse position (eg., from 1->2)
- else if (resources < capability min threshold) use (score(i) with Min score to choose which service to offloaded to the node_group with better position (eg., from 2->1)


"capability max threshold" and "capability min threshold" are used to avoid too many back and forth. For example, 70% and 30%

Implementation details:
service deployment options are ignored, so nodes are organised in nodegroups with far-edge, edge, cloud (possibly more)
services are sorted using the score: when the thresholds are not met, those with higher priority are kept on the far-edge (according to the thresholds) and so on

*/

@Component
public class TTODAHelper {

	public static final int LATENCY_CHECK_PERIOD_SEC = 7 * 24 * 60 * 60; //7d 
	
	enum NodeGroupPriority{
		faredge(0), edge(1), cloud(2);
		
		private int priority;
		
		NodeGroupPriority(int priority){
			this.priority = priority;
		}
		
		int getPriority(){
			return priority;
		}
		
	}
	
	private final ResourceDataReader resourceDataReader;
	private final NodeRepository nodeRepository;
	private final GoldPingerReader goldPingerReader;
	private final QuotaMonitoringReader quotaMonitoringReader;
	private final DeploymentOptionsManager deploymentOptionsManager;
	
	public TTODAHelper(ResourceDataReader resourceDataReader, NodeRepository nodeRepository, GoldPingerReader goldPingerReader, QuotaMonitoringReader quotaMonitoringReader, DeploymentOptionsManager deploymentOptionsManager) {
		this.resourceDataReader = resourceDataReader;
		this.nodeRepository = nodeRepository;
		this.goldPingerReader = goldPingerReader;
		this.quotaMonitoringReader = quotaMonitoringReader;
		this.deploymentOptionsManager = deploymentOptionsManager;
	}
	
	/*
	 * returns the Service allocation score based on TTODA algorithm. See TTODAOptimiser class comment
	 * 
	 */
	public Double getOptimisationScore(ServiceData serviceData, Set<Node> nodeSetOnEdge, int edgeTotalCpu4SP, int edgeTotalMem4SP) {
		double score = 0.0;

    	Map<String, String> preferences = ConverterJSON.convertToMap(serviceData.service.getApp().getServiceProvider().getPreferences());
		int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
		Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		
		Node currentNode = resourceDataReader.getCurrentNode(serviceData.service);
		if(currentNode != null) {
			int serviceRequestCpu = resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(serviceData.service, timestamp);
			int serviceRequestMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(serviceData.service, timestamp);

			double tN = 1.0 * serviceRequestCpu / edgeTotalCpu4SP + 1.0 * serviceRequestMem / edgeTotalMem4SP;
			double wN = tN / serviceData.priority; 
			double instantiationN_ms = resourceDataReader.getServiceStartupTimeSec(serviceData.service) * 1000;
			
			Instant timestampLatency = Instant.now().minusSeconds(LATENCY_CHECK_PERIOD_SEC);
			long latencyN_ms = goldPingerReader.getAverageLatencyAmongTwoNodeGroups(currentNode, nodeSetOnEdge, timestampLatency);
			
			long loadingN_ms = 0; //we assume images are already in the node, so it is 0
			score = wN * (instantiationN_ms + (latencyN_ms + loadingN_ms) * (1 - 2/tN));
		}
		
		return score;
	}
	
	//FIRST we check if the Node is mentioned in AT LEAST one service.
	//THEN we check if ALL services HAS the same Node
	private boolean isNodeValidForServiceConstraints(Node node, List<Service> serviceList) {
		for(Service service : serviceList) {
			if(!isNodeValidForServiceConstraints(node, service)) {
				return false;
			}
		}
		return true;
	}
	private boolean isNodeValidForServiceConstraints(Node node, Service service) {
		for(Set<Node> nodeSet : deploymentOptionsManager.getServiceDeploymentOptions(service.getId()).values()) {
			if(nodeSet.contains(node)) {
				return true;
			}
		}
		return false;
	}

	
	/*
	 this returns the prioritized nodeGroups (first faredge, then edge, then cloud) 
	 creates aggregation by node_type and excludes those with node_master=true
	 */
	public List<NodeGroup> getNodeGroupListForSPWithTotalCapacityAndFilterByServiceContraints(ServiceProvider serviceProvider, List<Service> serviceList){
		List<NodeGroup> result = new ArrayList<NodeGroup>();

		Map<Integer, List<Node>> nodeMap = new TreeMap<Integer, List<Node>>();
		for(Node node : nodeRepository.findBySP(serviceProvider.getName())) {
			if(isNodeValidForServiceConstraints(node, serviceList)) {
				Map<String, String> nodeProperties = ConverterJSON.convertToMap(node.getProperties());
				String nodeType = nodeProperties.get("node_type");
				NodeGroupPriority nodeGroupPriority = NodeGroupPriority.valueOf(nodeType);
				
				if(nodeGroupPriority != null) {
					String nodeMaster = nodeProperties.get("node_master");
					if(nodeMaster != null && nodeMaster.equals("false")) {
						if(!nodeMap.containsKey(nodeGroupPriority.priority)) {
							nodeMap.put(nodeGroupPriority.priority, new ArrayList<Node>());
						}
						nodeMap.get(nodeGroupPriority.priority).add(node);
					}
				}
			}
		}
		for(int priority : nodeMap.keySet()) {
			List<Node> nodeSet = nodeMap.get(priority);
			if(nodeSet != null && nodeSet.size() > 0) {
				Integer[] spCapacity = quotaMonitoringReader.getTotalCapacityForSPOnNodes(serviceProvider, nodeSet);
				int spCapacityForNodesCPU = spCapacity[0];
				int spCapacityForNodesMEM = spCapacity[1];
				result.add(new NodeGroup("nodeGroup#"+priority, nodeSet, spCapacityForNodesCPU, spCapacityForNodesMEM));
			}
		}

		return result;
	}


	public List<NodeGroup> getNodeGroupListForSPWithRemainingCapacityThatCanHostRequestsAndFilterByServiceContraints(ServiceProvider serviceProvider, Service service, int requestCpu, int requestMem){
		List<NodeGroup> tempResult = new ArrayList<NodeGroup>();

		Map<Integer, List<Node>> nodeMap = new TreeMap<Integer, List<Node>>();
		for(Node node : nodeRepository.findBySP(serviceProvider.getName())) {
			if(isNodeValidForServiceConstraints(node, service)) {
				Map<String, String> nodeProperties = ConverterJSON.convertToMap(node.getProperties());
				String nodeType = nodeProperties.get("node_type");
				NodeGroupPriority nodeGroupPriority = NodeGroupPriority.valueOf(nodeType);
				
				if(nodeGroupPriority != null) {
					String nodeMaster = nodeProperties.get("node_master");
					if(nodeMaster != null && nodeMaster.equals("false")) {
						if(!nodeMap.containsKey(nodeGroupPriority.priority)) {
							nodeMap.put(nodeGroupPriority.priority, new ArrayList<Node>());
						}
						nodeMap.get(nodeGroupPriority.priority).add(node);
					}
				}
			}
		}
		for(int priority : nodeMap.keySet()) {
			List<Node> nodeSet = nodeMap.get(priority);
			Integer[] spRemainingCapacity = quotaMonitoringReader.getRemainingCapacityForSPOnNodes(serviceProvider, nodeSet);
			int spRemainingCapacityForNodesCPU = spRemainingCapacity[0];
			int spRemainingCapacityForNodesMEM = spRemainingCapacity[1];
			tempResult.add(new NodeGroup("nodeGroup#"+priority, nodeSet, spRemainingCapacityForNodesCPU, spRemainingCapacityForNodesMEM));
		}		
		
		
		List<NodeGroup> result = new ArrayList<NodeGroup>();
		
		for(NodeGroup nodeGroup : tempResult) {
			if(nodeGroup.availabilityCpuMillicore > requestCpu && nodeGroup.availabilityMemoryMB > requestMem) {
				result.add(nodeGroup);
			}
		}
		return result;
	}
	
	
	public Integer[] getTotalCapacityForSPOnNodeSet(ServiceProvider serviceProvider, Set<Node> nodeSet){
		return quotaMonitoringReader.getTotalCapacityForSPOnNodes(serviceProvider, nodeSet);
	}
	public Integer[] getRemainingCapacityForSPOnNodeSet(ServiceProvider serviceProvider, Set<Node> nodeSet){
		return quotaMonitoringReader.getRemainingCapacityForSPOnNodes(serviceProvider, nodeSet);
	}
	
	/*
	 * returns on which Node a Service should be placed - only changes are returned
	 * 
	 * group by group, we compute the total capability cross SP, then we start allocating service data in order until either SP hits the quota or the nodeGroup ends the availability
	 * eventually, we check whether a service was already on a node, and return only the changes
	 */
	public static Map<ServiceData, NodeGroup> getServiceOptimisedAllocationPlan(List<ServiceData> serviceDataOptimisedList, List<NodeGroup> nodeGroupList) {
		
		Map<ServiceData, NodeGroup> result = new HashMap<ServiceData, NodeGroup>();
		
		//prepare the plan		
		Map<NodeGroup, List<ServiceData>> tempAllocationMap = new HashMap<NodeGroup, List<ServiceData>>();
		for(NodeGroup nodeGroup : nodeGroupList) {
			tempAllocationMap.put(nodeGroup, new ArrayList<ServiceData>());
		}


		//cycle the Service and pack resources
		for(ServiceData serviceData : serviceDataOptimisedList) {
			//pointer to the first nodeGroup to start packing into
			int nodeGroupIndex = 0;

			boolean isServiceDataHosted = false;
			while(nodeGroupIndex < nodeGroupList.size() && !isServiceDataHosted) {
				NodeGroup nodeGroup = nodeGroupList.get(nodeGroupIndex);
				isServiceDataHosted = nodeGroup.hostResources(serviceData.requestCpuMillicore, serviceData.requestMemoryMB);
				if(isServiceDataHosted) {
					tempAllocationMap.get(nodeGroup).add(serviceData);
				}
				else{
					nodeGroupIndex++;
				}
			}
			if(!isServiceDataHosted) {
				//this is a problem, not enough resources in the cloud!
				return null;
			}
		}
		
		//build the plan
		for(NodeGroup nodeGroup : tempAllocationMap.keySet()) {
			for(ServiceData serviceData : tempAllocationMap.get(nodeGroup)) {
				//if the proposed node is different from the current, we need to offload
				if(!nodeGroup.nodes.contains(serviceData.currentNode)){
					result.put(serviceData, nodeGroup);
				}
				//but also if the proposed node is the same but the service has different requests
				else if(
						ResourceDataReader.getServiceRuntimeCpuRequest(serviceData.service) != serviceData.requestCpuMillicore ||
						ResourceDataReader.getServiceRuntimeMemRequest(serviceData.service) != serviceData.requestMemoryMB
				) {
					result.put(serviceData, nodeGroup);
				}
			}
		}
		return result;
	}
	
	
}
