package eu.pledgerproject.confservice.optimisation;

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
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.repository.SteadyServiceRepository;

/**
this optimiser implements "resources" Optimisation and works in tandem with ResourceCriticalOptimiser
Its goal is to reduce reserved resources if no SLA violations are received after some time
 */


@Component
public class ResourceSteadyOptimiser {

	public static final String NO_ACTION_TAKEN = "No action taken - score within limits";
    private final Logger log = LoggerFactory.getLogger(ResourceSteadyOptimiser.class);
    
    public static final String RESOURCE_USAGE_CATEGORY = "resource-used";
    
    public static int SCORE_THRESHOLD = 100;
    
    private final ResourceDataReader resourceDataReader;
    private final ServiceProviderRepository serviceProviderRepository;
    private final SteadyServiceRepository steadyServiceRepository;
    private final ServiceReportRepository serviceReportRepository;
    private final ServiceResourceOptimiser serviceResourceOptimiser;
    private final ServiceRepository serviceRepository;
    private final EventRepository eventRepository;
    private final SlaViolationRepository slaViolationRepository;
    
    public ResourceSteadyOptimiser(ResourceDataReader resourceDataReader, ServiceProviderRepository serviceProviderRepository, SteadyServiceRepository steadyServiceRepository, ServiceReportRepository serviceReportRepository, ServiceResourceOptimiser serviceResourceOptimiser, ServiceRepository serviceRepository, EventRepository eventRepository, SlaViolationRepository slaViolationRepository) {
    	this.resourceDataReader = resourceDataReader;
    	this.serviceProviderRepository = serviceProviderRepository;
        this.steadyServiceRepository = steadyServiceRepository;
        this.serviceReportRepository = serviceReportRepository;
        this.serviceResourceOptimiser = serviceResourceOptimiser;
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
        this.slaViolationRepository = slaViolationRepository;
    }
	
	
	@Scheduled(cron = "30 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("ResourceSteadyServiceOptimiser started");
			
			//first, we create steadyServices and delete old ones
			List<SteadyService> newSteadyServiceList = new ArrayList<SteadyService>();
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				for(SteadyService steadyService : getSteadyServiceListByServiceProvider(serviceProvider)) {
					newSteadyServiceList.add(steadyService);
				}
			}
			for(SteadyService newSteadyService : newSteadyServiceList) {
				saveOrMerge(newSteadyService);
			}
			keepOnlyThisListAndOldRecordsWithActions(newSteadyServiceList);
			
			if(newSteadyServiceList.size() > 0) {
				//an event to track activities
				Event event = new Event();
				event.setCategory("ResourceSteadyServiceOptimiser");
				event.setDetails("monitoring found new steady services");
				eventRepository.save(event);
			}
			
			//then, we check what to do with them
			for(SteadyService steadyService : steadyServiceRepository.getAllOpenOrderedByScoreDesc()) {
				log.info("ResourceSteadyService " + steadyService + " has score " + steadyService.getScore());
				
				//is the service is stopped, we can close the steadyService entry
				if(steadyService.getService().getStatus().equals(ExecStatus.STOPPED)) {
					String actionTaken = NO_ACTION_TAKEN;
					steadyService.setActionTaken(actionTaken);
				}
				//else we activate only if the score is greater than the warning threshold
				else if(steadyService.getScore() > SCORE_THRESHOLD) {
					String actionTaken = serviceResourceOptimiser.optimise(steadyService.getService(), false);
					steadyService.setActionTaken(actionTaken);
				}
				else {
					steadyService.setActionTaken("No actions to take (score below warning threshold)");
				}
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
	
	private void keepOnlyThisListAndOldRecordsWithActions(List<SteadyService> steadyServiceList) {
		Set<Service> serviceListToKeep = new HashSet<Service>();
		for(SteadyService steadyService : steadyServiceList) {
			serviceListToKeep.add(steadyService.getService());
		}
		for(SteadyService steadyService : steadyServiceRepository.findAll()) {
			if(!serviceListToKeep.contains(steadyService.getService()) && steadyService.getActionTaken() != null && steadyService.getActionTaken().equals(NO_ACTION_TAKEN)){
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
			Instant timestampSteady = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
			log.info("ResourceSteadyOptimizer started for serviceProvider " + serviceProvider.getName());
			
			List<Service> runningServiceList = serviceRepository.getRunningServiceListByServiceProviderServiceOptimisationSinceTimestamp(serviceProvider, ServiceOptimisationType.resources.name(), timestampSteady);
			for(Service service : runningServiceList) {
				List<SlaViolation> slaViolationCriticalList = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.closed_critical.name(), ServiceOptimisationType.resources.name(), timestampSteady); 
				if(slaViolationCriticalList.size() == 0) {
					//get service scaling type
					Map<String, String> serviceInitialConfigurationProperties = ConverterJSON.convertToMap(service.getInitialConfiguration());
					String scaling = serviceInitialConfigurationProperties.get(Constants.SCALING);
					//and skip the check if scaling is not "horizontal" or "vertical"
					if(Constants.SCALING_VERTICAL.equals(scaling)) {
						log.info("ResourceSteadyOptimizer found a steady service with vertical scaling to be checked " + service.getName());
						addSteadyServiceForVerticalScaling(timestampSteady, result, service, serviceInitialConfigurationProperties, monitoringSlaViolationPeriodSec);
					}
					else if(Constants.SCALING_HORIZONTAL.equals(scaling)) {
						log.info("ResourceSteadyOptimizer found a steady service with horizontal scaling to be checked " + service.getName());
						addSteadyServiceForHorizontalScaling(timestampSteady, result, service, serviceInitialConfigurationProperties, monitoringSlaViolationPeriodSec);
					}
				}
			}
		}
		
		return result;
	}
	
	private void addSteadyServiceForVerticalScaling(Instant timestamp, List<SteadyService> result, Service service, Map<String, String> serviceInitialConfigurationProperties, int slaViolationMonitoringPeriodSec) {
		
		//get the resource requested
		Integer maxServiceReservedMem = resourceDataReader.getLastServiceMaxResourceReservedMem(service);
		Integer maxServiceReservedCpu = resourceDataReader.getLastServiceMaxResourceReservedCpu(service);
		
		//get the max resource used in the last period
		Integer maxServiceUsedMem = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.MEMORY_LABEL, timestamp);
		Integer maxServiceUsedCpu = serviceReportRepository.findMaxResourceUsedByServiceIdCategoryKeyTimestamp(service.getId(), RESOURCE_USAGE_CATEGORY, MonitoringService.CPU_LABEL, timestamp);

		//get min requests
		Integer minConfiguredServiceMem  = ResourceDataReader.getServiceMinMemRequest(service);
		Integer minConfiguredServiceCpu = ResourceDataReader.getServiceMinCpuRequest(service);
		
		//finally compute the score
		if(maxServiceReservedMem!=null && maxServiceReservedCpu!=null && maxServiceUsedMem != null && maxServiceUsedCpu != null) {
			
			//get params
			Map<String, String> serviceProviderPreferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
			String resourceUsedSteadyPercentage = serviceProviderPreferences.get("monitoring.steadyServices.maxResourceUsedPercentage");
			String autoscalePercentage = serviceProviderPreferences.get("autoscale.percentage");

			//compute score
			long score = getScoreScalingVertical(service, Integer.parseInt(resourceUsedSteadyPercentage), Integer.parseInt(autoscalePercentage), minConfiguredServiceMem, minConfiguredServiceCpu, maxServiceUsedMem, maxServiceUsedCpu, maxServiceReservedMem, maxServiceReservedCpu);
			log.info("ResourceSteadyOptimizer computed the steady service " + service.getName() + " score " + score);

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
		log.info("ResourceSteadyServiceOptimiser.getScoreScalingVertical(" + service.getName() + ") : percMemUsed: "+percMemUsed+", percCpuUsed:"+percCpuUsed+". [maxServiceUsedMem:"+maxServiceUsedMem+", maxServiceReservedMem:"+maxServiceReservedMem + ", maxServiceUsedCpu:"+maxServiceUsedCpu + ", maxServiceReservedCpu:"+maxServiceReservedCpu+"]");

		//we consider STEADY a service that uses less than the min percentage (eg. min is 70%, if it is currently 75%, then we skip steady optimisation)
		if(	percMemUsed > resourceUsedSteadyPerc || percCpuUsed > resourceUsedSteadyPerc ) {
			
			return SCORE_THRESHOLD;
		}

		//high score is given to services which use less. 100 means nothing to do, 200 is max
		long score_mem = (long) (SCORE_THRESHOLD * (1.0 + (100-percMemUsed) / 100.0));
		long score_cpu = (long) (SCORE_THRESHOLD * (1.0 + (100-percCpuUsed) / 100.0));
		return Math.min(score_mem, score_cpu);
	}
	
	private String getDetails(long maxServiceUsedMem, long maxServiceReservedMem, long maxServiceUsedCpu, long maxServiceReservedCpu) {
    	return "" + 
    		"Mem (max used/reserved) " + maxServiceUsedMem + "/" + maxServiceReservedMem+"\n" +
			"Cpu (max used/reserved) " + maxServiceUsedCpu + "/" + maxServiceReservedCpu+"\n";
    }
	
	
	private void addSteadyServiceForHorizontalScaling(Instant timestamp, List<SteadyService> result, Service service, Map<String, String> serviceInitialConfigurationProperties, int slaViolationMonitoringPeriodSec) {
		//get the resource requested
		Integer maxServiceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp);
		Integer maxServiceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(service, timestamp);
		
		//get the max resource used in the last period
		Integer maxServiceUsedMem = resourceDataReader.getServiceMaxMemUsedInPeriod(service, timestamp);
		Integer maxServiceUsedCpu = resourceDataReader.getServiceMaxCpuUsedInPeriod(service, timestamp);

		//finally compute the score
		if(maxServiceReservedMem!=null && maxServiceReservedCpu!=null && maxServiceUsedMem != null && maxServiceUsedCpu != null) {
			
			//get params
			String resourceUsedSteadyPercentage = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences()).get("monitoring.steadyServices.maxResourceUsedPercentage");
			int replicas = ResourceDataReader.getServiceReplicas(service);

			//compute score
			long score = getScoreScalingHorizontal(service, Integer.parseInt(resourceUsedSteadyPercentage), replicas, maxServiceUsedMem, maxServiceReservedMem, maxServiceUsedCpu, maxServiceReservedCpu);
			log.info("ResourceSteadyOptimizer computed the steady service " + service.getName() + " score " + score);

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
	
	private void saveInfoEvent(Service service, String msg) {
    	if(log.isInfoEnabled()) {
    		Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setServiceProvider(service.getApp().getServiceProvider());
			event.setDetails(msg);
			event.setCategory("EcodaResourceOptimiser");
			event.severity(Event.INFO);
			eventRepository.save(event);
    	}
	}
	
	private long getScoreScalingHorizontal(Service service, int resourceUsedSteadyPerc, int replicas, long maxServiceUsedMem, long maxServiceReservedMem, long maxServiceUsedCpu, long maxServiceReservedCpu) {
		//compute percentage of resources used
		int percMemUsed =  (int) (100.0 * maxServiceUsedMem / (maxServiceReservedMem*replicas));
		int percCpuUsed =  (int) (100.0 * maxServiceUsedCpu / (maxServiceReservedCpu*replicas));
		log.info("ResourceSteadyServiceOptimiser.getScoreScalingHorizontal(" + service.getName() + ") : percMemUsed: "+percMemUsed+", percCpuUsed:"+percCpuUsed+". [maxServiceUsedMem:"+maxServiceUsedMem+", maxServiceReservedMem:"+maxServiceReservedMem + ", maxServiceUsedCpu:"+maxServiceUsedCpu + ", maxServiceReservedCpu:"+maxServiceReservedCpu+", replicas:"+replicas+"]");
		
		//we DO NOT consider STEADY a service that uses more than minimal margin
		if(	percMemUsed > resourceUsedSteadyPerc || percCpuUsed > resourceUsedSteadyPerc ) {
			saveInfoEvent(service, "percMemUsed is " + resourceUsedSteadyPerc + "% and percCpuUsed is " + resourceUsedSteadyPerc + "%, service is using almost all its resources, it is not steady, no need to scale down");
			return SCORE_THRESHOLD;
		}
		//we also DO NOT consider STEADY a service that has 1 replica
		if(replicas == 1) {
			saveInfoEvent(service, "replicas is 1, no need to scale down");
			return SCORE_THRESHOLD;
		}
		
		long score_mem = (long) (SCORE_THRESHOLD * (1.0 + percMemUsed / 100.0));
		long score_cpu = (long) (SCORE_THRESHOLD * (1.0 + percCpuUsed / 100.0));
		return Math.min(score_mem, score_cpu);
	}

	
}
