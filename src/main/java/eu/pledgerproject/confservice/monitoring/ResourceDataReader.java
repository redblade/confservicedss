package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

@Component
public class ResourceDataReader {
    
	private final NodeReportRepository nodeReportRepository;
	private final NodeRepository nodeRepository;
	private final ServiceReportRepository serviceReportRepository;
	
	public static String MAX_REQUEST_LABEL = "max_request";
	public static String MIN_REQUEST_LABEL = "min_request"; 
	
	public static final int PERCENTAGE_USED_IF_NO_DATA = 10;
	
	public ResourceDataReader(NodeReportRepository nodeReportRepository, NodeRepository nodeRepository, ServiceReportRepository serviceReportRepository) {
		this.nodeReportRepository = nodeReportRepository;
		this.nodeRepository = nodeRepository; 
		this.serviceReportRepository = serviceReportRepository;
	}
	
	//as a CSV
	public static String printNodeSet(Collection<Node> nodeSet) {
		StringBuilder sb = new StringBuilder();
		for(Node node : nodeSet) {
			sb.append(node.getName()).append(",");
		}
		if(sb.length() > 0) {
			sb.setLength(sb.length()-1);
		}
		return sb.toString();
	}
	
	public int getResourceUsagePercentage(Service service) {
    	Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
    	int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));

		Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		
    	//get service scaling type
		Map<String, String> serviceInitialConfigurationProperties = ConverterJSON.convertToMap(service.getInitialConfiguration());
		String scaling = serviceInitialConfigurationProperties.get("scaling");

		//get the max resource reserved
		Integer maxServiceReservedMem = getServiceMaxResourceReservedMemInPeriod(service, timestamp);
		Integer maxServiceReservedCpu = getServiceMaxResourceReservedCpuInPeriod(service, timestamp);
		
		//get the max resource used in the last period
		Integer maxServiceUsedMem = getServiceMaxMemUsedInPeriod(service, timestamp);
		Integer maxServiceUsedCpu = getServiceMaxCpuUsedInPeriod(service, timestamp);

		if(ServiceResourceOptimiser.SCALING_VERTICAL.equals(scaling)) {			
			//compute percentage of resources used
			int percMemUsed =  (int) (100.0 * maxServiceUsedMem / (maxServiceReservedMem));
			int percCpuUsed =  (int) (100.0 * maxServiceUsedCpu / (maxServiceReservedCpu));
			return Math.max(percMemUsed, percCpuUsed);
		}
		else if(ServiceResourceOptimiser.SCALING_HORIZONTAL.equals(scaling)) {
			int replicas = Integer.parseInt(ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas"));
			int percMemUsed =  (int) (100.0 * maxServiceUsedMem / (maxServiceReservedMem*replicas));
			int percCpuUsed =  (int) (100.0 * maxServiceUsedCpu / (maxServiceReservedCpu*replicas));
			return Math.max(percMemUsed, percCpuUsed);
		}
		
		return -1;
    }
	
	public Node getCurrentNode(Service service) {
		Node result = null;
		
		String nodeName = ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get(ServiceScheduler.RUNTIME_NODE_SELECTED);
		if(nodeName != null) {
			Optional<Node> optionalNode = nodeRepository.findByName(nodeName);
			if(optionalNode.isPresent()) {
				result = optionalNode.get();
			}
		}
		
		return result;
	}
	
	public Infrastructure getInfrastructureFromSelectedNodes(String nodeStringCSV) {
		String firstNodeName = nodeStringCSV.contains(",") ? nodeStringCSV.substring(0, nodeStringCSV.indexOf(",")) : nodeStringCSV;
		Optional<Node> nodeDB = nodeRepository.findByName(firstNodeName);
		if(nodeDB.isPresent()) {
			return nodeDB.get().getInfrastructure();
		}
		return null;

	}
	
	public Integer getServiceStartupTimeSec(Service service) {
    	Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
		int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
		Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);

		List<ServiceReport> serviceReportStartupTime = serviceReportRepository.findServiceReportByServiceIdCategoryNameInPeriod(service.getId(), ServiceMonitor.STARTUP_TIME, ServiceMonitor.KEY, timestamp);

		Integer result = 1000; try{result = serviceReportStartupTime.get(0).getValue().intValue();}catch(Exception e) {}
		return result;
	}
	
	public static int getServiceRuntimeCpuRequest(Service service) {
		int result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get(MonitoringService.CPU_LABEL));}catch(Exception e) {}
		return result;
	}
	public static int getServiceRuntimeMemRequest(Service service) {
		int result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get(MonitoringService.MEMORY_LABEL));}catch(Exception e) {}
		return result;
	}
	
	public static int getServiceInitialCpuRequest(Service service) {
		int result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(MonitoringService.INITIAL_CPU_MILLICORE));}catch(Exception e) {}
		return result;
	}
	public static int getServiceInitialMemRequest(Service service) {
		int result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(MonitoringService.INITIAL_MEMORY_MB));}catch(Exception e) {}
		return result;
	}
	public static Integer getServiceMinCpuRequest(Service service) {
		Integer result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(MonitoringService.MIN_CPU_MILLICORE));}catch(Exception e) {}
		return result;
	}
	public static Integer getServiceMinMemRequest(Service service) {
		Integer result = 0; 
		try{result = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(MonitoringService.MIN_MEMORY_MB));}catch(Exception e) {}
		return result;
	}
	public Integer getServiceMaxCpuUsedInPeriod(Service service, Instant timestamp) {
		Integer result = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), ResourceSteadyServiceOptimiser.RESOURCE_USAGE_CATEGORY, MonitoringService.MEMORY_LABEL, timestamp);
		if(result == null) {
			result = ResourceDataReader.getServiceMinCpuRequest(service);
		}
		return result;
	}
	public Integer getServiceMaxMemUsedInPeriod(Service service, Instant timestamp) {
		Integer result = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), ResourceSteadyServiceOptimiser.RESOURCE_USAGE_CATEGORY, MonitoringService.CPU_LABEL, timestamp);
		if(result == null) {
			result = ResourceDataReader.getServiceMinMemRequest(service);
		}
		return result;
	}

	public Integer getServiceMaxResourceReservedCpuInPeriod(Service service, Instant timestamp) {
		List<ServiceReport> serviceReportCpu = serviceReportRepository.findServiceReportByServiceIdCategoryNameInPeriod(service.getId(), MAX_REQUEST_LABEL, MonitoringService.CPU_LABEL, timestamp);
		
		Integer result = null; try{result = serviceReportCpu.get(0).getValue().intValue();}catch(Exception e) {}
		if(result == null) {
			result = ResourceDataReader.getServiceInitialCpuRequest(service);
		}
		return result;
	}
	public Integer getServiceMaxResourceReservedMemInPeriod(Service service, Instant timestamp) {
		List<ServiceReport> serviceReportMem = serviceReportRepository.findServiceReportByServiceIdCategoryNameInPeriod(service.getId(), MAX_REQUEST_LABEL, MonitoringService.MEMORY_LABEL, timestamp);

		Integer result = null; try{result = serviceReportMem.get(0).getValue().intValue();}catch(Exception e) {}
		if(result == null) {
			result = ResourceDataReader.getServiceInitialMemRequest(service);
		}
		return result;
	}
	
	public Map<Node, Integer[]> getTotalNodeCapacityCpuMem(Collection<Node> nodeSet) {
		Map<Node, Integer[]> capacityCpuMem = new HashMap<Node, Integer[]>();
		for(Node node : nodeSet) {
			int totalCapacityCpu = getNodeCpuCapacityMillicore(node);
			int totalCapacityMem = getNodeMemCapacityMb(node);
			capacityCpuMem.put(node, new Integer[] {totalCapacityCpu, totalCapacityMem});
		}
		
		return capacityCpuMem;
	}
	
	public Map<Node, Integer[]> getTotalNodeAvailabilityCpuMem(Collection<Node> nodeSet) {
		Map<Node, Integer[]> result = new HashMap<Node, Integer[]>();
		for(Node node : nodeSet) {
			int totalCapacityCpu = getNodeCpuCapacityMillicore(node);
			int totalCapacityMem = getNodeMemCapacityMb(node);
			int totalAvailabilityCpu = totalCapacityCpu - getNodeCpuUsedMillicore(node);
			int totalAvailabilityMem = totalCapacityMem - getNodeMemUsedMb(node);
			
			result.put(node, new Integer[] {totalAvailabilityCpu, totalAvailabilityMem});
		}
		
		return result;
	}
	
	public Node getNodeWithMoreAvailability(Set<Node> nodeSet) {
		Map<Node, Integer[]> capacityCpuMemLeft = getTotalNodeAvailabilityCpuMem(nodeSet);
		Node resultFound = null;
		int maxCpuFound = 0;
		int maxMemFound = 0;
		for(Node node : capacityCpuMemLeft.keySet()) {
			Integer[] capacityLeft = capacityCpuMemLeft.get(node);
			if(resultFound == null || capacityLeft[0] > maxCpuFound && capacityLeft[1] > maxMemFound) {
				resultFound = node;
				maxCpuFound = capacityLeft[0];
				maxMemFound = capacityLeft[1];
			}
		}
		
		return resultFound;
	}
	
	public static int getNodeCpuCapacityMillicore(Node node) {
		int result = 0; try{result = Integer.parseInt(ConverterJSON.convertToMap(node.getTotalResources()).get("cpu_millicore"));}catch(Exception e) {}
		return result;
	}
	public int getNodeCpuUsedMillicore(Node node) {
		List<NodeReport> nodeReportList = nodeReportRepository.findNodeReportByNodeIdAndKey(node.getId(), MonitoringService.CPU_LABEL);
		
		int result = 0; 
		try{
			result = nodeReportList.get(0).getValue().intValue();
		}catch(Exception e) {
			result = getNodeCpuCapacityMillicore(node) / PERCENTAGE_USED_IF_NO_DATA;
		}
		return result;
	}
	public static int getNodeMemCapacityMb(Node node) {

		int result = 0; try{result = Integer.parseInt(ConverterJSON.convertToMap(node.getTotalResources()).get("memory_mb"));}catch(Exception e) {}
		return result;
	}
	public static int getNodeMemCapacityMb(Collection<Node> nodes) {
		int result = 0;
		for(Node node : nodes) {
			result += getNodeMemCapacityMb(node);
		}
		return result;
	}
	
	public int getNodeMemUsedMb(Node node) {
		List<NodeReport> nodeReportList = nodeReportRepository.findNodeReportByNodeIdAndKey(node.getId(), MonitoringService.MEMORY_LABEL);
		
		int result = 0; 
		try{
			result = nodeReportList.get(0).getValue().intValue();
		}catch(Exception e) {
			result = getNodeMemCapacityMb(node) / PERCENTAGE_USED_IF_NO_DATA;
		}
		return result;
	}
	
	

}
