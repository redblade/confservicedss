package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.InfrastructureReportRepository;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;


@Component
public class QuotaMonitoringReader {
	public static final String HEADER_QUOTA_USED = "quota-used-perc";
	public static final String HEADER_QUOTA_REQUESTED = "quota-requested-perc";
	
	private final org.slf4j.Logger log = LoggerFactory.getLogger(QuotaMonitoringReader.class);
	
	private final ProjectRepository projectRepository;
	private final InfrastructureReportRepository infrastructureReportRepository;
	private final ServiceRepository serviceRepository;
	private final ResourceDataReader resourceDataReader;
	private final DeploymentOptionsManager deploymentOptionsManager;
	private final EventRepository eventRepository;

	public QuotaMonitoringReader(ProjectRepository projectRepository, InfrastructureReportRepository infrastructureReportRepository, ServiceRepository serviceRepository, ResourceDataReader resourceDataReader, DeploymentOptionsManager deploymentOptionsManager, EventRepository eventRepository) {
		this.projectRepository = projectRepository;
		this.infrastructureReportRepository = infrastructureReportRepository;
		this.serviceRepository = serviceRepository;
		this.resourceDataReader = resourceDataReader;
		this.deploymentOptionsManager = deploymentOptionsManager;
		this.eventRepository = eventRepository;
	}
	
	private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("QuotaMonitoringReader");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	private double getCpuUsageByServiceProviderAndInfrastructure(ServiceProvider serviceProvider, Infrastructure infrastructure) {
		double result = 0.0;
		List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findInfrastructureReportListByServiceProviderNameAndKeyAndInfrastructure(serviceProvider.getName(), MonitoringService.CPU_LABEL, infrastructure);
		if(infrastructureReportList.size() > 0) {
			result = infrastructureReportList.get(0).getValue();
		}
		return result;
	}
	
	private double getMemoryUsageByServiceProviderAndInfrastructure(ServiceProvider serviceProvider, Infrastructure infrastructure) {
		double result = 0.0;
		List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findInfrastructureReportListByServiceProviderNameAndKeyAndInfrastructure(serviceProvider.getName(), MonitoringService.MEMORY_LABEL, infrastructure);
		if(infrastructureReportList.size() > 0) {
			result = infrastructureReportList.get(0).getValue();
		}
		return result;
	}
	
	public Integer[] getRemainingCapacityForSPCurrentRankingNodes(ServiceProvider serviceProvider, Service service) {
		Set<Node> currentRankingNodeSet = deploymentOptionsManager.getCurrentNodeSet(service.getId());
		if(currentRankingNodeSet != null) {
			return getRemainingCapacityForSPOnNodes(serviceProvider, currentRankingNodeSet);
		}
		else {
			String errorMessage = "Service " + service.getName() + " does not have a currentRanking";
			log.error(errorMessage);
			saveErrorEvent(errorMessage);
			return new Integer[] {0, 0};
		}
	}
	
	public Integer[] getRemainingCapacityForSPOnNodes(ServiceProvider serviceProvider, Collection<Node> nodeSet) {
		int cpu = 0;
		int mem = 0;

		//this is the actual Node capacity left
		Map<Node, Integer[]> nodesCapacityLeft = resourceDataReader.getTotalNodeAvailabilityCpuMem(nodeSet);		
		for(Node node : nodeSet) {
			Integer[] nodeCapacityLeft = nodesCapacityLeft.get(node);
			cpu +=nodeCapacityLeft[0];
			mem +=nodeCapacityLeft[1];
		}

		Infrastructure infrastructure = nodeSet.iterator().next().getInfrastructure();
		Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(serviceProvider.getId(), infrastructure.getId());
		
		//if a project exists AND there are quotas, then the SP capacity left is based on the quota actually used. Then the min(quota used, node capacity left) is returned
		if(project.isPresent() && project.get().getQuotaCpuMillicore() > 0 && project.get().getQuotaMemMB() > 0) {
			int serviceProviderCapacityCPU = project.get().getQuotaCpuMillicore();
			int serviceProviderCapacityMEM = project.get().getQuotaMemMB();
			int serviceProviderUsedCPU = getTotalRequestCpuOnRunningServices4SP(serviceProvider);
			int serviceProviderUsedMEM = getTotalRequestMemoryOnRunningServices4SP(serviceProvider);
			
			int serviceProviderRemainingCPU = serviceProviderCapacityCPU - serviceProviderUsedCPU;
			int serviceProviderRemainingMEM = serviceProviderCapacityMEM - serviceProviderUsedMEM;
			
			cpu = Math.min(cpu, serviceProviderRemainingCPU);
			mem = Math.min(mem, serviceProviderRemainingMEM);
		}

		return new Integer[] {cpu, mem};
	}
	
	public Integer[] getTotalCapacityForSPOnNodes(ServiceProvider serviceProvider, Collection<Node> nodeSet) {
		int cpu = 0;
		int mem = 0;

		Map<Node, Integer[]> nodesCapacity = resourceDataReader.getTotalNodeCapacityCpuMem(nodeSet);		
		for(Node node : nodeSet) {
			Integer[] nodeCapacity = nodesCapacity.get(node);
			cpu +=nodeCapacity[0];
			mem +=nodeCapacity[1];
		}

		Infrastructure infrastructure = nodeSet.iterator().next().getInfrastructure();
		Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(serviceProvider.getId(), infrastructure.getId());
		if(project.isPresent() && project.get().getQuotaCpuMillicore() > 0 && project.get().getQuotaMemMB() > 0) {
			int serviceProviderCapacityCPU = project.get().getQuotaCpuMillicore();
			int serviceProviderCapacityMEM = project.get().getQuotaMemMB();

			cpu = Math.min(cpu, serviceProviderCapacityCPU);
			mem = Math.min(mem, serviceProviderCapacityMEM);
		}
		return new Integer[] {cpu, mem};
	}

	
	
	public void storeMetrics(Instant timestamp) {
		try {
			log.info("QuotaMonitoringReader storeMetrics...");
			for(Project project : projectRepository.findAll()) {
				if(project.getQuotaCpuMillicore() > 0 && project.getQuotaMemMB() > 0) {
					InfrastructureReport infrastructureReport4SP;

					//quota requested
					double quotaRequestedCpuPercentage = project.getQuotaCpuMillicore() == 0 ? 0 : 100.0 * getTotalRequestCpuOnRunningServices4SP(project.getServiceProvider()) / project.getQuotaCpuMillicore();
					double quotaRequestedMemPercentage = project.getQuotaMemMB() == 0 ? 0 : 100.0 * getTotalRequestMemoryOnRunningServices4SP(project.getServiceProvider()) / project.getQuotaCpuMillicore();
					
					infrastructureReport4SP = new InfrastructureReport();
					infrastructureReport4SP.setTimestamp(timestamp);
					infrastructureReport4SP.setInfrastructure(project.getInfrastructure());
					infrastructureReport4SP.setCategory(HEADER_QUOTA_REQUESTED);
					infrastructureReport4SP.setGroup(project.getServiceProvider().getName());
					infrastructureReport4SP.setKey(MonitoringService.CPU_LABEL);
					infrastructureReport4SP.setValue(quotaRequestedCpuPercentage);
					infrastructureReportRepository.save(infrastructureReport4SP);
					
					infrastructureReport4SP = new InfrastructureReport();
					infrastructureReport4SP.setTimestamp(timestamp);
					infrastructureReport4SP.setInfrastructure(project.getInfrastructure());
					infrastructureReport4SP.setCategory(HEADER_QUOTA_REQUESTED);
					infrastructureReport4SP.setGroup(project.getServiceProvider().getName());
					infrastructureReport4SP.setKey(MonitoringService.MEMORY_LABEL);
					infrastructureReport4SP.setValue(quotaRequestedMemPercentage);
					infrastructureReportRepository.save(infrastructureReport4SP);
					
					
					//actual quota used
					double quotaUsedCpuPercentage = project.getQuotaCpuMillicore() == 0 ? 0 : 100.0 * getCpuUsageByServiceProviderAndInfrastructure(project.getServiceProvider(), project.getInfrastructure()) / project.getQuotaCpuMillicore();
					double quotaUsedMemPercentage = project.getQuotaMemMB() == 0 ? 0 : 100.0 * getMemoryUsageByServiceProviderAndInfrastructure(project.getServiceProvider(), project.getInfrastructure()) / project.getQuotaMemMB();

					infrastructureReport4SP = new InfrastructureReport();
					infrastructureReport4SP.setTimestamp(timestamp);
					infrastructureReport4SP.setInfrastructure(project.getInfrastructure());
					infrastructureReport4SP.setCategory(HEADER_QUOTA_USED);
					infrastructureReport4SP.setGroup(project.getServiceProvider().getName());
					infrastructureReport4SP.setKey(MonitoringService.CPU_LABEL);
					infrastructureReport4SP.setValue(quotaUsedCpuPercentage);
					infrastructureReportRepository.save(infrastructureReport4SP);
					
					infrastructureReport4SP = new InfrastructureReport();
					infrastructureReport4SP.setTimestamp(timestamp);
					infrastructureReport4SP.setInfrastructure(project.getInfrastructure());
					infrastructureReport4SP.setCategory(HEADER_QUOTA_USED);
					infrastructureReport4SP.setGroup(project.getServiceProvider().getName());
					infrastructureReport4SP.setKey(MonitoringService.MEMORY_LABEL);
					infrastructureReport4SP.setValue(quotaUsedMemPercentage);
					infrastructureReportRepository.save(infrastructureReport4SP);
				}
			}
			
		}catch(Exception e) {
			log.error("QuotaMonitoringReader", e);
			saveErrorEvent("QuotaMonitoringReader error " + e.getClass() + " " + e.getMessage());
		}

	}
	
	public int getTotalRequestCpuOnRunningServices4SP(ServiceProvider serviceProvider) {
		int result = 0;
		for(Service service : serviceRepository.getRunningServiceListByServiceProviderId(serviceProvider.getId())) {
			int requestedCpu = 0;
			try{requestedCpu = Integer.parseInt(ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get(MonitoringService.CPU_LABEL));}catch(Exception e) {}
			result += requestedCpu;
		}
		return result;
	}
	public int getTotalRequestMemoryOnRunningServices4SP(ServiceProvider serviceProvider) {
		int result = 0;
		for(Service service : serviceRepository.getRunningServiceListByServiceProviderId(serviceProvider.getId())) {
			int requestedMem = 0;
			try{requestedMem = Integer.parseInt(ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get(MonitoringService.MEMORY_LABEL));}catch(Exception e) {}
			result += requestedMem;
		}
		return result;
	}
	
	private Set<Node> getNodeSetThatCanHostRequests(Collection<Node> candidateNodes, int requestedCpuMillicore, int requestedMemoryMB) {
		Set<Node> result = new HashSet<Node>();
		
		List<Service> runningServiceBySP = serviceRepository.getRunningServiceList();
		for(Node node : candidateNodes) {
			int totalNodeCpuRequests = 0;
			int totalNodeMemRequests = 0;
			for(Service service : runningServiceBySP) {
				Node currentNode = resourceDataReader.getCurrentNode(service);
				if(currentNode != null && node.getName().equals(currentNode.getName())) {
					totalNodeCpuRequests += ResourceDataReader.getServiceRuntimeCpuRequest(service);
					totalNodeMemRequests += ResourceDataReader.getServiceRuntimeMemRequest(service);
				}
			}
			long nodeCpuCapacity = ResourceDataReader.getNodeCpuCapacityMillicore(node);
			long nodeMemCapacity = ResourceDataReader.getNodeMemCapacityMb(node);
			if(
				nodeCpuCapacity > totalNodeCpuRequests + requestedCpuMillicore 
				&&
				nodeMemCapacity > totalNodeMemRequests + requestedMemoryMB
			){
				result.add(node);
			}
		}
		return result;
	}
	
	/*
	 * this method removes the nodes that cannot host the requested resources based on the SP quota used for the belonging infrastructure
	 * we assume ALL the nodes belong to the same infrastructure 
	 */
	public Set<Node> filterNodeSetThatCanHostResourceRequest(ServiceProvider serviceProvider, Collection<Node> candidateNodes, int requestCpuMillicore, int requestMemoryMB) {
		Set<Node> result = new HashSet<Node>();
		
		//first, if the SP hits the quota with the requests, the answer is easy: no!
		if(candidateNodes.size() > 0) {
			Integer[] nodesCapacityLeft = getRemainingCapacityForSPOnNodes(serviceProvider, candidateNodes);
			int remainingCapacityCpuMillicore = nodesCapacityLeft[0];
			int remainingCapacityMemoryMB = nodesCapacityLeft[1];
			if( remainingCapacityCpuMillicore > requestCpuMillicore && remainingCapacityMemoryMB > requestMemoryMB) {
				//so the SP can host such resources, but there could be Nodes where there is no total (cross SP) capacity left, let's check:
				Set<Node> nodesThatCanHostRequests = getNodeSetThatCanHostRequests(candidateNodes, requestCpuMillicore, requestMemoryMB); 
				result = nodesThatCanHostRequests;
			}
		}
		
		return result;
	}
	
}
