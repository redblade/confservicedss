package eu.pledgerproject.confservice.optimisation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.GoldPingerReader;
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

/*

EA-ECODA algorithm will be submitted by the end of September 2022 to IEEE Transactions on Vehicular Technology.
The difference between ECODA and EA-ECODA is in the percentage of CPU/MEM available on each node which is static in ECODA and changed dynamically in EA-ECODA.
Basically, in ECODA edge nodes CPU/MEM can be saturated (100%) while in EA-ECODA they are changed to save energy.

Compared to pure ECODA, the changes to the code are in the methods used to compute available CPU/MEM. The rest is IDENTICAL to ECODAHelper.java
In particular, quotaMonitoringReader.XXX methods use a "maxPercentageToUse" variable that is changed periodically by the "updateMaxPercentageToUse" method

*/

@Component
public class EAECODAHelper {

	public static final int LATENCY_CHECK_PERIOD_SEC = 7 * 24 * 60 * 60; //7d 
	
	enum NodeGroupPriority{
		edge(1), cloud(2);
		
		private int priority;
		
		NodeGroupPriority(int priority){
			this.priority = priority;
		}
		
		int getPriority(){
			return priority;
		}
		
	}
	
	private final EventRepository eventRepository;
	private final ResourceDataReader resourceDataReader;
	private final ServiceRepository serviceRepository;
	private final NodeRepository nodeRepository;
	private final NodeReportRepository nodeReportRepository;
	private final EdgeResourcePercentageManager edgeResourcePercentageManager;
	private final GoldPingerReader goldPingerReader;
	private final QuotaMonitoringReader quotaMonitoringReader;
	private final DeploymentOptionsManager deploymentOptionsManager;
	
	public EAECODAHelper(EventRepository eventRepository, ResourceDataReader resourceDataReader, ServiceRepository serviceRepository, NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, EdgeResourcePercentageManager edgeResourcePercentageManager, GoldPingerReader goldPingerReader, QuotaMonitoringReader quotaMonitoringReader, DeploymentOptionsManager deploymentOptionsManager) {
		this.eventRepository = eventRepository;
		this.resourceDataReader = resourceDataReader;
		this.serviceRepository = serviceRepository;
		this.nodeRepository = nodeRepository;
		this.nodeReportRepository = nodeReportRepository;
		this.edgeResourcePercentageManager = edgeResourcePercentageManager;
		this.goldPingerReader = goldPingerReader;
		this.quotaMonitoringReader = quotaMonitoringReader;
		this.deploymentOptionsManager = deploymentOptionsManager;
	}
	
	/*
	 * returns the Service allocation score based on ECODA algorithm. See ECODAOptimiser class comment
	 * 
	 */
	public Double getOptimisationScore(ServiceData serviceData, Set<Node> nodeSetOnEdge, int totalEdgeCpu4SP, int totalEdgeMem4SP) {
		double score = 0.0;

    	Map<String, String> preferences = ConverterJSON.convertToMap(serviceData.service.getApp().getServiceProvider().getPreferences());
		int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
		Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		
		Node currentNode = resourceDataReader.getCurrentNode(serviceData.service);
		if(currentNode != null) {
			int serviceRequestCpu = resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(serviceData.service, timestamp);
			int serviceRequestMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(serviceData.service, timestamp);

			double Rn = 1.0 * serviceRequestCpu / totalEdgeCpu4SP;
			double Mn = 1.0 * serviceRequestMem / totalEdgeMem4SP;
			double Tn = Rn + Mn; 
			
			double Wn = serviceData.priority / Tn; 
			double In = resourceDataReader.getServiceStartupTimeSec(serviceData.service) * 1000;
			
			Instant timestampLatency = Instant.now().minusSeconds(LATENCY_CHECK_PERIOD_SEC);
			long Ln = goldPingerReader.getAverageLatencyAmongTwoNodeGroups(currentNode, nodeSetOnEdge, timestampLatency);
			
			long Pn = 0; //we assume images are already in the node, so it is 0
			score = Wn * (In + (Ln + Pn) * (1 - 2/Tn));
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
	 this returns the prioritized nodeGroups (first edge, then cloud) 
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
		boolean isEdge = true; //first priority is edge, the rest is cloud
		for(int priority : nodeMap.keySet()) {
			List<Node> nodeSet = nodeMap.get(priority);
			if(nodeSet != null && nodeSet.size() > 0) {
				
				//EAECODAHelper differs from ECODAHelper here: call has been changed to include maxPercentageToUseOnEdge
				if(isEdge) {
					Integer[] spCapacity = quotaMonitoringReader.getTotalCapacityForSPOnNodes(serviceProvider, nodeSet, getMaxPercentageToUseOnEdge(serviceProvider));
					int spCapacityForNodesCPU = spCapacity[0];
					int spCapacityForNodesMEM = spCapacity[1];
					log.info("EA-ECODAHelper new edge nodeGroup#"+priority + " capacity computed with threshold " + getMaxPercentageToUseOnEdge(serviceProvider) + "%: spCapacityForNodesCPU is " + spCapacityForNodesCPU + ", spCapacityForNodesMEM is " + spCapacityForNodesMEM);
					result.add(new NodeGroup("nodeGroup#"+priority, nodeSet, spCapacityForNodesCPU, spCapacityForNodesMEM));
				}
				else {
					Integer[] spCapacity = quotaMonitoringReader.getTotalCapacityForSPOnNodes(serviceProvider, nodeSet);
					int spCapacityForNodesCPU = spCapacity[0];
					int spCapacityForNodesMEM = spCapacity[1];
					result.add(new NodeGroup("nodeGroup#"+priority, nodeSet, spCapacityForNodesCPU, spCapacityForNodesMEM));
				}

			}
			isEdge = false;
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
			//EAECODAHelper differs from ECODAHelper here: call has been changed to include maxPercentageToUse
			Integer[] spRemainingCapacity = quotaMonitoringReader.getRemainingCapacityForSPOnNodes(serviceProvider, nodeSet, getMaxPercentageToUseOnEdge(serviceProvider));
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
		//EAECODAHelper differs from ECODAHelper here: call has been changed to include maxPercentageToUse
		return quotaMonitoringReader.getTotalCapacityForSPOnNodes(serviceProvider, nodeSet, getMaxPercentageToUseOnEdge(serviceProvider));
	}
	public Integer[] getRemainingCapacityForSPOnNodeSet(ServiceProvider serviceProvider, Set<Node> nodeSet){
		//EAECODAHelper differs from ECODAHelper here: call has been changed to include maxPercentageToUse
		return quotaMonitoringReader.getRemainingCapacityForSPOnNodes(serviceProvider, nodeSet, getMaxPercentageToUseOnEdge(serviceProvider));
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
	
	//EAECODAHelper differs from ECODAHelper.java starting from here, with the logic to compute maxPercentageToUse
	private final Logger log = LoggerFactory.getLogger(EAECODAHelper.class);

	public static final int MIN_PERCENTAGE = 5;
	public static final int MAX_PERCENTAGE = 95;
	
	//here we store the timestamp of the last reports checked 
	private Instant lastCheck = Instant.EPOCH;
	private Double lastUtilityFunctionValue;

	private void saveInfoEvent(String msg) {
    	if(log.isInfoEnabled()) {
    		Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("EAEcodaResourceOptimiser");
			event.severity(Event.INFO);
			eventRepository.save(event);
    	}
	}
	
	public boolean isMaxPercentageToUseOnEdgeChanged(ServiceProvider serviceProvider) {
		return edgeResourcePercentageManager.isValueChanged(serviceProvider);
	}
	public int getMaxPercentageToUseOnEdge(ServiceProvider serviceProvider) {
		return edgeResourcePercentageManager.getValue(serviceProvider);
	}
	public void setMaxPercentageToUseOnEdge(ServiceProvider serviceProvider, int maxPercentageToUseOnEdge) {
		edgeResourcePercentageManager.setValue(serviceProvider, maxPercentageToUseOnEdge);
	}
	
	//this method is called periodicallys by EAECODAResourceOptimiser.java
	public void updateMaxPercentageToUseOnEdge(ServiceProvider serviceProvider) {
		log.info("EA-ECODAHelper: checking if it is data to update maxPercentageToUseOnEdge... ");
		log.info("EA-ECODAHelper: current maxPercentageToUseOnEdge is " + getMaxPercentageToUseOnEdge(serviceProvider));
		
		/*
		
		PSEUDO-CODE
		
		"distinct timestamp" is done on node_report, with category='energy' order by last timestamp desc
		
		if lastCheck null 
		  if "distinct timestamp" size > 1
		    do.1 + update lastCheck
		  else if distinct size == 1
		    do.2 + update lastCheck
		  else 
		    do nothing
		- else
		    if "distinct timestamp" size, done on timestamp > lastCheck, > 1
		      do.action1
		      update lastCheck
		    else if distinct size == 1
		      do.action2
		      update lastCheck
		    else 
		      do.nothing
		
		THIS IS EQUIVALENT TO
		
		lastCheck = 0 (01.01.1970)
		  if distinct size done on timestamp > lastCheck > 1
		    do.action1 + update lastCheck
		  else if distinct size == 1
		    do.action2 + update lastCheck
		  else 
		    do.nothing
		
		do.action1 is 
		  A = compute v(t-1) 
          B = compute v(t) 
          vt_history = B
          compute threshold
		
		do.action2 is
		  if vt_history exists
		    A = vt_history
            B = compute v(t) 
            vt_history = B
            compute threshold
          else
            B = compute v(t) 
            vt_history = B
		*/
		
		List<Instant> lastDistinctTimestampsForCategory = nodeReportRepository.findLastDistinctTimestampsForCategory("energy", lastCheck);
		if(lastDistinctTimestampsForCategory.size() > 1) {
			Instant timestamp_t_less_1 = lastDistinctTimestampsForCategory.get(1);
			Instant timestamp_t = lastDistinctTimestampsForCategory.get(0);
			lastCheck = timestamp_t;
			double utility_function_t_less_1 = getUtilityFunctionValue(timestamp_t_less_1);
			double utility_function_t = getUtilityFunctionValue(timestamp_t);
			lastUtilityFunctionValue = utility_function_t;
			int maxPercentageToUseOnEdge = getThresholdValue(getMaxPercentageToUseOnEdge(serviceProvider), utility_function_t_less_1, utility_function_t);
			setMaxPercentageToUseOnEdge(serviceProvider, maxPercentageToUseOnEdge);
			log.info("NEW EA-ECODA edge threshold computed: " + getMaxPercentageToUseOnEdge(serviceProvider));
			saveInfoEvent("NEW EA-ECODA edge threshold computed: " + getMaxPercentageToUseOnEdge(serviceProvider));
		}
		else if(lastDistinctTimestampsForCategory.size() == 1) {
			Instant timestamp_t = lastDistinctTimestampsForCategory.get(0);
			lastCheck = timestamp_t;
			if(lastUtilityFunctionValue != null) {
				double utility_function_t_less_1 = lastUtilityFunctionValue;
				double utility_function_t = getUtilityFunctionValue(timestamp_t);
				lastUtilityFunctionValue = utility_function_t;
				int maxPercentageToUseOnEdge = getThresholdValue(getMaxPercentageToUseOnEdge(serviceProvider), utility_function_t_less_1, utility_function_t);
				setMaxPercentageToUseOnEdge(serviceProvider, maxPercentageToUseOnEdge);
				log.info("NEW EA-ECODA edge threshold computed: " + getMaxPercentageToUseOnEdge(serviceProvider));
				saveInfoEvent("NEW EA-ECODA edge threshold computed: " + getMaxPercentageToUseOnEdge(serviceProvider));
			}
			else {
				double utility_function_t = getUtilityFunctionValue(timestamp_t);
				lastUtilityFunctionValue = utility_function_t;
				log.info("EA-ECODAHelper: NO update on maxPercentageToUseOnEdge");
			}
		}
		else {
			log.info("EA-ECODAHelper: NO update on maxPercentageToUseOnEdge");
		}
		
	}
	
	
	private int getThresholdValue(int oldThreshold, double utility_function_t_less_1, double utility_function_t) {
		log.info("EA-ECODAHelper: computing thresholdValue_t");

		int newThreshold;
		if(utility_function_t >= utility_function_t_less_1) {
			newThreshold = (int) (oldThreshold + utility_function_t * (1 - oldThreshold));
		}
		else {
			newThreshold = (int) (oldThreshold - utility_function_t * oldThreshold);
		}
		if(newThreshold < MIN_PERCENTAGE) {
			newThreshold = MIN_PERCENTAGE;
		}
		else if (newThreshold > MAX_PERCENTAGE) {
			newThreshold = MAX_PERCENTAGE;
		}
		
		return newThreshold;
	}
	
	
	public static int DEFAULT_THRESHOLD = MAX_PERCENTAGE;

	
	//here we compute the utility function
	private double getUtilityFunctionValue(Instant timestampToCheck) {
		log.info("EA-ECODAHelper: computing utilityFunctionValue");
		
		//prepare the list of the edge nodes
		int totalCpuOnEdgeNodes = 0;
		List<Node> nodeEdgeList = new ArrayList<Node>();
		for(Node node : nodeRepository.findAll()) {
			Map<String, String> nodeProperties = ConverterJSON.convertToMap(node.getProperties());
			String nodeType = nodeProperties.get(NodeGroup.NODE_TYPE);
			if(NodeGroup.NODE_EDGE.equals(nodeType)){
				nodeEdgeList.add(node);
				Map<String, String> nodeTotalResources = ConverterJSON.convertToMap(node.getTotalResources());
				totalCpuOnEdgeNodes += Integer.parseInt(nodeTotalResources.get(MonitoringService.CPU_LABEL));
			}
		}
		int lastN = 1;
		Pageable topN = PageRequest.of(0, lastN);
		int edgeNodeSize = nodeEdgeList.size();
		
		
		//compute the energy_cost
		double energy_cost_t = 0;
		for(Node node : nodeEdgeList) {
			List<NodeReport> nodeReportList_energy_cost = nodeReportRepository.findByNodeCategoryKeyAndTimestamp(topN, node, "energy", "energy_cost", timestampToCheck).getContent();
			if(nodeReportList_energy_cost.size() > 0) {
				energy_cost_t += nodeReportList_energy_cost.get(0).getValue();
				log.info("EA-ECODAHelper. Timestamp:" + timestampToCheck + ", NodeID: " + node.getId() + ", energy_cost_t:" + nodeReportList_energy_cost.get(0).getValue());
			}
		}
		energy_cost_t /= edgeNodeSize;
		

		//compute the energy_stored
		double energy_stored_t = 0;
		for(Node node : nodeEdgeList) {
			List<NodeReport> nodeReportList_energy_stored = nodeReportRepository.findByNodeCategoryKeyAndTimestamp(topN, node, "energy", "energy_stored", timestampToCheck).getContent();
			if(nodeReportList_energy_stored.size() > 0) {
				energy_stored_t += nodeReportList_energy_stored.get(0).getValue();
				log.info("EA-ECODAHelper. Timestamp:" + timestampToCheck + ", NodeID: " + node.getId() + ", energy_stored_t: " + nodeReportList_energy_stored.get(0).getValue());
			}
		}
		energy_stored_t /= edgeNodeSize;

		
		//compute the battery_level
		double battery_level_t = 0;
		for(Node node : nodeEdgeList) {
			List<NodeReport> nodeReportList_battery_level = nodeReportRepository.findByNodeCategoryKeyAndTimestamp(topN, node, "energy", "battery_level", timestampToCheck).getContent();
			if(nodeReportList_battery_level.size() > 0) {
				battery_level_t += nodeReportList_battery_level.get(0).getValue();
				log.info("EA-ECODAHelper. Timestamp:" + timestampToCheck + ", NodeID: " + node.getId() + ", battery_level_t: " + nodeReportList_battery_level.get(0).getValue());
			}
		}
		battery_level_t /= edgeNodeSize;
		

		//we start to compute the utilityFunction value: first, we need edgeAppsOmega and allAppsOmega

		//edgeAppsOmega = sum[1/omegaN] = sum[priority * Total resource on edge/App resource request] FOR EACH APP ON EDGE
		//allAppsOmega  = sum[1/omegaN] = sum[priority * Total resource on edge/App resource request] FOR ALL APPS 
		double edgeAppsOmega = 0.0;
		double allAppsOmega = 0.0;
		for(Service service : serviceRepository.getRunningServiceList()) {
			Map<String, String> runtimeConfigurationMap = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
			String nodeSelected = runtimeConfigurationMap.get("nodes_selected");
			Optional<Node> node = nodeRepository.findByName(nodeSelected);
			if(node.isPresent()) {
				Map<String, String> nodeProperties = ConverterJSON.convertToMap(node.get().getProperties());
				String nodeType = nodeProperties.get(NodeGroup.NODE_TYPE);
				int priority = service.getPriority();
				int cpuRequest = Integer.parseInt(runtimeConfigurationMap.get(MonitoringService.CPU_LABEL));

				if(NodeGroup.NODE_EDGE.equals(nodeType)){
					edgeAppsOmega += (1.0 * priority * totalCpuOnEdgeNodes / cpuRequest);
				}
				allAppsOmega += (1.0 * priority * totalCpuOnEdgeNodes / cpuRequest);
			}
		}
		log.info("EA-ECODAHelper. Ready to compute utilityFunction. Timestamp:" + timestampToCheck + ", energy_stored_t: " + energy_stored_t + ", energy_cost_t: " + energy_cost_t + ", battery_level_t: " + battery_level_t + ", edgeAppsOmega: " + edgeAppsOmega + ", allAppsOmega: " + allAppsOmega);
		
		//then we produce the utilityFunction, using battery_level, energy_stored, edgeAppsOmega, allAppsOmega, energy_cost 
		double utilityFunctionValue = battery_level_t + energy_stored_t + edgeAppsOmega/allAppsOmega - energy_cost_t;
		log.info("EA-ECODAHelper. Timestamp:" + timestampToCheck + ", utilityFunctionValue: " + utilityFunctionValue);

		return utilityFunctionValue;
		
	}
	
	
}
