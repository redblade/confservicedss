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
TTODA algorithm is described here (TODO add link after the paper is accepted) 

R & M are 0..1 

//TODO check formula: total capacity?
Rn = percentage of CPU requested wrt total capacity on edge and far-edge (expressed as 0..1)
Mn = percentage of MEM requested wrt total capacity on edge and far-edge (expressed as 0..1)
Tn = Rn + Mn

//TODO check formula: reverse?
Wn = service priority / Tn 

R_caret & R_tilde are 0..1

R_caret = reserved/far edge capacity
R_tilde = reserved/edge capacity

In = time (ms) to start a service (startup time)
Lec = time (ms) to communicate from edge to cloud nodes (latency)
Lne = time (ms) to communicate from far-edge to edge nodes (latency)
Lnc = time (ms) to communicate from far-edge to cloud nodes (latency) = Lec+Lne
Pne = time (ms) to have a service ready on the edge (0 if images are pre-loaded)
Pnc = time (ms) to have a service ready on the cloud (0 if images are pre-loaded)

WORK IN PROGRESS

*/

@Component
public class TTODAHelper {
	public static final int SCORE_22_CONSTANT = 1000000;

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
	public Double getOptimisationScore(ServiceData serviceData, Set<Node> nodeSetOnFarEdge, int faredgeTotalCpu4SP, int faredgeTotalMem4SP, Set<Node> nodeSetOnEdge, int edgeTotalCpu4SP, int edgeTotalMem4SP, Set<Node> nodeSetOnCloud) {
		double score = 0.0;

		Node anyNodeOnFarEdge = nodeSetOnFarEdge.iterator().next();
		Node anyNodeOnEdge = nodeSetOnEdge.iterator().next();
		Node currentNode = resourceDataReader.getCurrentNode(serviceData.service);
		if(currentNode != null) {
			double serviceRequestCpu = 1.0 * ResourceDataReader.getServiceRuntimeCpuRequest(serviceData.service);
			double serviceRequestMem = 1.0 * ResourceDataReader.getServiceRuntimeMemRequest(serviceData.service);

			double Rn = 1.0 * serviceRequestCpu / (faredgeTotalCpu4SP + edgeTotalCpu4SP);
			double Mn = 1.0 * serviceRequestMem / (faredgeTotalMem4SP + edgeTotalMem4SP);
			double Tn = Rn + Mn; 
			
			double Wn = Tn / serviceData.priority; 
			double In = resourceDataReader.getServiceStartupTimeSec(serviceData.service) * 1000;

			double R_caret = (serviceRequestCpu + serviceRequestMem) / (faredgeTotalCpu4SP + faredgeTotalMem4SP);
			double R_tilde = (serviceRequestCpu + serviceRequestMem) / (edgeTotalCpu4SP + edgeTotalMem4SP);
			
			Instant timestampLatency = Instant.now().minusSeconds(LATENCY_CHECK_PERIOD_SEC);
			
			double Lec = 1.0 * goldPingerReader.getAverageLatencyAmongTwoNodeGroups(anyNodeOnEdge, nodeSetOnCloud, timestampLatency);
			double Lne = 1.0 * goldPingerReader.getAverageLatencyAmongTwoNodeGroups(anyNodeOnFarEdge, nodeSetOnEdge, timestampLatency);
			double Lnc = Lec + Lne;
			
			double Pnc = 0.0; //we assume images are already in the node, so it is 0
			double Pne = 0.0; //we assume images are already in the node, so it is 0
			
			double score_22 = Wn * In + (Lnc + Pnc) * (1 - 1/R_caret) - (Lnc + Pnc -Lne -Pne) * R_tilde;
			double lambda = Wn * (Lnc + Pnc) / R_caret;
			double score_15 = Wn * In + Wn * (Lne + Pne - Lnc - Pnc) * score_22 + lambda * (R_caret -1);

			if(score_22 > SCORE_22_CONSTANT) {
				throw new RuntimeException("score_22 is "+score_22+" BUT must be lower than " + SCORE_22_CONSTANT);
			}
			score = SCORE_22_CONSTANT * score_22 + score_15;
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
				String nodeType = nodeProperties.get(NodeGroup.NODE_TYPE);
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
				String nodeType = nodeProperties.get(NodeGroup.NODE_TYPE);
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
