package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SteadyServiceRepository;

@Component
public class SteadyServiceOptimiser {
	public static int STEADY_PERIOD_SEC_FACTOR = 2;
	
    private final Logger log = LoggerFactory.getLogger(SteadyServiceOptimiser.class);
    
    public static final String RESOURCE_USAGE_CATEGORY = "resource-usage";
    
    public static int SCORE_THRESHOLD = 100;
    
    private final ResourceDataReader resourceDataReader;
    private final ServiceProviderRepository serviceProviderRepository;
    private final SteadyServiceRepository steadyServiceRepository;
    private final ServiceReportRepository serviceReportRepository;
    private final ServiceResourceOptimiser serviceResourceOptimiser;
    private final ServiceRepository serviceRepository;
    private final EventRepository eventRepository;
    
    public SteadyServiceOptimiser(ResourceDataReader resourceDataReader, ServiceProviderRepository serviceProviderRepository, SteadyServiceRepository steadyServiceRepository, ServiceReportRepository serviceReportRepository, ServiceResourceOptimiser serviceResourceOptimiser, ServiceRepository serviceRepository, EventRepository eventRepository) {
    	this.resourceDataReader = resourceDataReader;
    	this.serviceProviderRepository = serviceProviderRepository;
        this.steadyServiceRepository = steadyServiceRepository;
        this.serviceReportRepository = serviceReportRepository;
        this.serviceResourceOptimiser = serviceResourceOptimiser;
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
    }
	
	
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		log.info("SteadyServiceOptimiser started");
		
		//first, we create steadyServices and delete old ones
		List<SteadyService> newSteadyServiceList = new ArrayList<SteadyService>();
		for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
			for(SteadyService steadyService : getSteadyServiceListByServiceProvider(serviceProvider)) {
				newSteadyServiceList.add(steadyService);
			}
		}
		for(SteadyService steadyService : newSteadyServiceList) {
			saveOrMerge(steadyService);
		}
		removeEverythingElse(newSteadyServiceList);
		
		if(newSteadyServiceList.size() > 0) {
			//an event to track activities
			Event event = new Event();
			event.setCategory("SteadyServiceOptimiser");
			event.setDetails("started");
			eventRepository.save(event);
		}
		
		//then, we check what to do with them
		for(SteadyService steadyService : steadyServiceRepository.getOpenWithActionsToTakeOrdered()) {
			log.info("SteadyService " + steadyService + " has score " + steadyService.getScore());
			
			//is the service is stopped, we can close the steadyService entry
			if(steadyService.getService().getStatus().equals(ExecStatus.STOPPED)) {
				String actionTaken = "No actions to take (service stopped)";
				steadyService.setActionTaken(actionTaken);
				steadyService.setTimestampProcessed(Instant.now());
				saveOrMerge(steadyService);
			}
			
			//else we activate only if the score is greater than the warning threshold
			else if(steadyService.getScore() > SCORE_THRESHOLD) {
				String actionTaken = serviceResourceOptimiser.optimise(steadyService.getService(), false);
				steadyService.setActionTaken(actionTaken);
				steadyService.setTimestampProcessed(Instant.now());
				saveOrMerge(steadyService);
			}
			else {
				steadyService.setActionTaken("No actions to take (score below warning threshold)");
				steadyService.setTimestampProcessed(Instant.now());
				saveOrMerge(steadyService);
			}
		}
	}
	
	private void saveOrMerge(SteadyService steadyService) {
		Instant timestamp = Instant.now();
		Optional<SteadyService> steadyServiceDB = steadyServiceRepository.getByServiceID(steadyService.getService().getId());
		if(steadyServiceDB.isPresent()) {
			steadyServiceDB.get().setScore(steadyService.getScore());
			steadyServiceDB.get().setTimestampProcessed(timestamp);
			steadyServiceDB.get().setActionTaken(steadyService.getActionTaken());
			steadyServiceDB.get().setDetails(steadyService.getDetails());
			steadyServiceRepository.save(steadyServiceDB.get());
		}
		else {
			steadyService.setTimestampCreated(timestamp);
			steadyServiceRepository.save(steadyService);
		}
	}
	
	private void removeEverythingElse(List<SteadyService> steadyServiceList) {
		Set<Service> serviceListToKeep = new HashSet<Service>();
		for(SteadyService steadyService : steadyServiceList) {
			serviceListToKeep.add(steadyService.getService());
		}
		for(SteadyService steadyService : steadyServiceRepository.findAll()) {
			if(!serviceListToKeep.contains(steadyService.getService())){
				steadyServiceRepository.delete(steadyService);
			}
		}
	}

	private List<SteadyService> getSteadyServiceListByServiceProvider(ServiceProvider serviceProvider) {
		List<SteadyService> result = new ArrayList<SteadyService>();
		
		//create the steadyService list iterating over the ServiceProviders
		if(serviceProvider != null) {
			Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
			
			int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
	
			//prepare the timestamp for the range of time to check for slaViolations before now() 
			Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec * STEADY_PERIOD_SEC_FACTOR);
			log.info("SteadyOptimizer started for serviceProvider " + serviceProvider.getName());
			
			List<Service> steadyServiceList = serviceRepository.findSteadyServiceListByServiceProviderServiceOptimisationSinceTimestamp(serviceProvider, ServiceOptimisationType.resources.name(), timestamp);
			for(Service service : steadyServiceList) {
				if(service.getStatus().equals(ExecStatus.RUNNING) && service.getServiceOptimisation() != null && service.getServiceOptimisation().getOptimisation().equals(ServiceOptimisationType.resources.name())) {
					//get service scaling type
					Map<String, String> serviceInitialConfigurationProperties = ConverterJSON.convertToMap(service.getInitialConfiguration());
					String scaling = serviceInitialConfigurationProperties.get("scaling");
					//and skip the check if scaling is not "horizontal" or "vertical"
					if(ServiceResourceOptimiser.SCALING_VERTICAL.equals(scaling)) {
						log.info("SteadyOptimizer found a steady service with vertical scaling to be checked " + service.getName());
						addSteadyServiceForVerticalScaling(timestamp, result, service, serviceInitialConfigurationProperties, monitoringSlaViolationPeriodSec);
					}
					else if(ServiceResourceOptimiser.SCALING_HORIZONTAL.equals(scaling)) {
						log.info("SteadyOptimizer found a steady service with horizontal scaling to be checked " + service.getName());
						addSteadyServiceForHorizontalScaling(timestamp, result, service, serviceInitialConfigurationProperties, monitoringSlaViolationPeriodSec);
					}
				}
			}
		}
		
		return result;
	}
	
	private void addSteadyServiceForVerticalScaling(Instant timestamp, List<SteadyService> result, Service service, Map<String, String> serviceInitialConfigurationProperties, int slaViolationMonitoringPeriodSec) {
		//get the resource requested
		Integer maxServiceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemSoFar(service);
		Integer maxServiceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuSoFar(service);
		
		//get the max resource used in the last period
		Integer maxServiceUsedMem = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.MEMORY_LABEL, timestamp);
		Integer maxServiceUsedCpu = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.CPU_LABEL, timestamp);

		//get the min memory configured
		String minConfiguredServiceMemString = serviceInitialConfigurationProperties.get("min_memory_mb");
		if(minConfiguredServiceMemString == null) {
			minConfiguredServiceMemString = serviceInitialConfigurationProperties.get("initial_memory_mb");
		}
		Integer minConfiguredServiceMem = Integer.parseInt(minConfiguredServiceMemString);  

		//get the min cpu configured
		String minConfiguredServiceCpuString = serviceInitialConfigurationProperties.get("min_cpu_millicore");
		if(minConfiguredServiceCpuString == null) {
			minConfiguredServiceCpuString = serviceInitialConfigurationProperties.get("initial_cpu_millicore");
		}
		Integer minConfiguredServiceCpu = Integer.parseInt(minConfiguredServiceCpuString);  
		
		//finally compute the score
		if(maxServiceReservedMem!=null && maxServiceReservedCpu!=null && maxServiceUsedMem != null && maxServiceUsedCpu != null) {
			
			//get params
			Map<String, String> serviceProviderPreferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
			String resourceUsedSteadyPercentage = serviceProviderPreferences.get("monitoring.steadyServices.maxResourceUsedPercentage");
			String autoscalePercentage = serviceProviderPreferences.get("autoscale.percentage");

			//compute score
			long score = getScoreScalingVertical(service, Integer.parseInt(resourceUsedSteadyPercentage), Integer.parseInt(autoscalePercentage), minConfiguredServiceMem, minConfiguredServiceCpu, maxServiceUsedMem, maxServiceUsedCpu, maxServiceReservedMem, maxServiceReservedCpu);
			log.info("SteadyOptimizer computed the steady service " + service.getName() + " score " + score);

			//create a steadyService per Service if score is above threshold
			if(score > SCORE_THRESHOLD) {
				SteadyService steadyService = new SteadyService();
				steadyService.setTimestampCreated(Instant.now());
				
				steadyService.setId(service.getId());
				steadyService.setService(service);
				steadyService.setMonitoringPeriodSec(slaViolationMonitoringPeriodSec);
				steadyService.setDetails(getDetails(maxServiceUsedMem, maxServiceReservedMem, maxServiceUsedCpu, maxServiceReservedCpu));
						
				steadyService.setScore(score);
				
				result.add(steadyService);
			}
		}
	}
	
	private long getScoreScalingVertical(Service service, int resourceUsedSteadyPerc, int autoscalePercentage, int minConfiguredServiceMem, int minConfiguredServiceCpu, int maxServiceUsedMem, int maxServiceUsedCpu, int maxServiceReservedMem, int maxServiceReservedCpu) {
				
		//compute percentage of resources used
		int percMemUsed =  (int) (100.0 * maxServiceUsedMem / (maxServiceReservedMem));
		int percCpuUsed =  (int) (100.0 * maxServiceUsedCpu / (maxServiceReservedCpu));
		log.info("SteadyServiceOptimiser.getScoreScalingVertical(" + service.getName() + ") : percMemUsed: "+percMemUsed+", percCpuUsed:"+percCpuUsed+". [maxServiceUsedMem:"+maxServiceUsedMem+", maxServiceReservedMem:"+maxServiceReservedMem + ", maxServiceUsedCpu:"+maxServiceUsedCpu + ", maxServiceReservedCpu:"+maxServiceReservedCpu+"]");

		//we consider STEADY a service that uses less than the min percentage (eg. 70%). If not, we skip it!
		if(	percMemUsed > resourceUsedSteadyPerc || percCpuUsed > resourceUsedSteadyPerc ) {
			return SCORE_THRESHOLD;
		}
		//we also DO NOT consider STEADY a service that is "too close" (100 + autoscale %) to the min values. Basically, we want to stop reducing max_resources when below a min value
		if(maxServiceReservedMem < minConfiguredServiceMem * (100.0 + autoscalePercentage)/100.0 || maxServiceReservedCpu < minConfiguredServiceCpu * (100.0 + autoscalePercentage)/100.0) {
			return SCORE_THRESHOLD;
		}
		
		long score_mem = (long) (SCORE_THRESHOLD * (1.0 + percMemUsed / 100.0));
		long score_cpu = (long) (SCORE_THRESHOLD * (1.0 + percCpuUsed / 100.0));
		return Math.min(score_mem, score_cpu);
	}
	
	private String getDetails(long maxServiceUsedMem, long maxServiceReservedMem, long maxServiceUsedCpu, long maxServiceReservedCpu) {
    	return "" + 
    		"Mem (max used/reserved) " + maxServiceUsedMem + "/" + maxServiceReservedMem+"\n" +
			"Cpu (max used/reserved) " + maxServiceUsedCpu + "/" + maxServiceReservedCpu+"\n";
    }
	
	
	private void addSteadyServiceForHorizontalScaling(Instant timestamp, List<SteadyService> result, Service service, Map<String, String> serviceInitialConfigurationProperties, int slaViolationMonitoringPeriodSec) {
		//get the resource requested
		Integer maxServiceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemSoFar(service);
		Integer maxServiceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuSoFar(service);
		
		//get the max resource used in the last period
		Integer maxServiceUsedMem = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.MEMORY_LABEL, timestamp);
		Integer maxServiceUsedCpu = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.CPU_LABEL, timestamp);

		//finally compute the score
		if(maxServiceReservedMem!=null && maxServiceReservedCpu!=null && maxServiceUsedMem != null && maxServiceUsedCpu != null) {
			
			//get params
			String resourceUsedSteadyPercentage = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences()).get("monitoring.steadyServices.maxResourceUsedPercentage");
			String replicas = ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");

			//compute score
			long score = getScoreScalingHorizontal(service, Integer.parseInt(resourceUsedSteadyPercentage), Integer.parseInt(replicas), maxServiceUsedMem, maxServiceReservedMem, maxServiceUsedCpu, maxServiceReservedCpu);
			log.info("SteadyOptimizer computed the steady service " + service.getName() + " score " + score);

			//create a steadyService per Service if score is above threshold
			if(score > SCORE_THRESHOLD) {
				SteadyService steadyService = new SteadyService();
				steadyService.setTimestampCreated(Instant.now());
				
				steadyService.setId(service.getId());
				steadyService.setService(service);
				steadyService.setMonitoringPeriodSec(slaViolationMonitoringPeriodSec);
				steadyService.setDetails(getDetails(maxServiceUsedMem, maxServiceReservedMem, maxServiceUsedCpu, maxServiceReservedCpu));
						
				steadyService.setScore(score);
				
				result.add(steadyService);
			}
		}
	}
	
	private long getScoreScalingHorizontal(Service service, int resourceUsedSteadyPerc, int replicas, long maxServiceUsedMem, long maxServiceReservedMem, long maxServiceUsedCpu, long maxServiceReservedCpu) {
		//compute percentage of resources used
		int percMemUsed =  (int) (100.0 * maxServiceUsedMem / (maxServiceReservedMem*replicas));
		int percCpuUsed =  (int) (100.0 * maxServiceUsedCpu / (maxServiceReservedCpu*replicas));
		log.info("SteadyServiceOptimiser.getScoreScalingHorizontal(" + service.getName() + ") : percMemUsed: "+percMemUsed+", percCpuUsed:"+percCpuUsed+". [maxServiceUsedMem:"+maxServiceUsedMem+", maxServiceReservedMem:"+maxServiceReservedMem + ", maxServiceUsedCpu:"+maxServiceUsedCpu + ", maxServiceReservedCpu:"+maxServiceReservedCpu+", replicas:"+replicas+"]");
		
		//we DO NOT consider STEADY a service that uses more than minimal margin
		if(	percMemUsed > resourceUsedSteadyPerc || percCpuUsed > resourceUsedSteadyPerc ) {
			return SCORE_THRESHOLD;
		}
		//we also DO NOT consider STEADY a service that has 1 replica
		if(replicas == 1) {
			return SCORE_THRESHOLD;
		}
		
		long score_mem = (long) (SCORE_THRESHOLD * (1.0 + percMemUsed / 100.0));
		long score_cpu = (long) (SCORE_THRESHOLD * (1.0 + percCpuUsed / 100.0));
		return Math.min(score_mem, score_cpu);
	}

	
}
