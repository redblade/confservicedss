package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.repository.NodeRepository;

@Component
public class EcodaHelper {

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
	
	public EcodaHelper(ResourceDataReader resourceDataReader, NodeRepository nodeRepository, GoldPingerReader goldPingerReader, QuotaMonitoringReader quotaMonitoringReader, DeploymentOptionsManager deploymentOptionsManager) {
		this.resourceDataReader = resourceDataReader;
		this.nodeRepository = nodeRepository;
		this.goldPingerReader = goldPingerReader;
		this.quotaMonitoringReader = quotaMonitoringReader;
		this.deploymentOptionsManager = deploymentOptionsManager;
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
	 this returns the prioritized nodeGroups (first edge, than cloud etc.) 
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
	
	public Set<Node> getEdgeNodesBySP(ServiceProvider serviceProvider){
		Set<Node> result = new HashSet<Node>();
		
		for(Node node : nodeRepository.findBySP(serviceProvider.getName())) {
			if(ConverterJSON.getProperty(node.getProperties(), "node_type").equals("edge")) {
				result.add(node);
			}
		}
		return result;
	}
	
	/*
	 * returns the Service allocation score based on ECODA algorithm. See EcodaOptimiser class comment
	 * 
	 */
	public Double getOptimisationScore(ServiceData serviceData, Set<Node> nodeSetOnEdge, int edgeTotalCpu4SP, int edgeTotalMem4SP) {
		double score = 0.0;

		Node currentNode = resourceDataReader.getCurrentNode(serviceData.service);
		if(currentNode != null) {
			int serviceRequestCpu = resourceDataReader.getServiceMaxResourceReservedCpuSoFar(serviceData.service);
			int serviceRequestMem = resourceDataReader.getServiceMaxResourceReservedMemSoFar(serviceData.service);

			double tN = 1.0 * serviceRequestCpu / edgeTotalCpu4SP + 1.0 * serviceRequestMem / edgeTotalMem4SP;
			double wN = tN / serviceData.priority; 
			double instantiationN_ms = resourceDataReader.getServiceStartupTimeSec(serviceData.service) * 1000;
			
			Instant timestamp = Instant.now().minusSeconds(LATENCY_CHECK_PERIOD_SEC);
			long latencyN_ms = goldPingerReader.getAverageLatencyAmongTwoNodeGroups(currentNode, nodeSetOnEdge, timestamp);
			
			long loadingN_ms = 0; //we assume images are already in the node, so it is 0
			score = wN * (instantiationN_ms + (latencyN_ms + loadingN_ms) * (1 - 2/tN));
		}
		
		return score;
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
