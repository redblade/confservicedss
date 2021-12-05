package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.repository.CriticalServiceRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;

@Component
public class ResourceCriticalServiceOptimiser {
	public static final String NO_ACTION_TAKEN = "No action taken - score within limits";
    private final Logger log = LoggerFactory.getLogger(ResourceCriticalServiceOptimiser.class);
    
    public static int SCORE_THRESHOLD = 100;

    private final CriticalServiceRepository criticalServiceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final SlaViolationRepository slaViolationRepository;
	private final EventRepository eventRepository;
	private final ServiceResourceOptimiser serviceResourceOptimiser;
	private final ResourceDataReader resourceDataReader;

    public ResourceCriticalServiceOptimiser(CriticalServiceRepository criticalServiceRepository, ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository, ServiceResourceOptimiser serviceResourceOptimiser, ResourceDataReader resourceDataReader) {
    	this.criticalServiceRepository = criticalServiceRepository;
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.slaViolationRepository = slaViolationRepository;
    	this.eventRepository = eventRepository;
    	this.serviceResourceOptimiser = serviceResourceOptimiser;
    	this.resourceDataReader = resourceDataReader;
    }
	
    private void saveOrMerge(CriticalService criticalService) {
		Instant timestamp = Instant.now();
		Optional<CriticalService> criticalServiceDB = criticalServiceRepository.getOpenByServiceID(criticalService.getService().getId());
		if(criticalServiceDB.isPresent()) {
			criticalServiceDB.get().setScore(criticalService.getScore());
			criticalServiceDB.get().setTimestampProcessed(timestamp);
			criticalServiceDB.get().setActionTaken(criticalService.getActionTaken());
			criticalServiceDB.get().setDetails(criticalService.getDetails());
			criticalServiceRepository.save(criticalServiceDB.get());
		}
		else {
			criticalService.setTimestampCreated(timestamp);
			criticalServiceRepository.save(criticalService);
		}
	}
	
	private void keepOnlyThisListAndOldRecordsWithActions(List<CriticalService> criticalServiceList) {
		Set<eu.pledgerproject.confservice.domain.Service> serviceListToKeep = new HashSet<eu.pledgerproject.confservice.domain.Service>();
		for(CriticalService criticalService : criticalServiceList) {
			serviceListToKeep.add(criticalService.getService());
		}
		for(CriticalService criticalService : criticalServiceRepository.findAll()) {
			if(!serviceListToKeep.contains(criticalService.getService()) && criticalService.getActionTaken().equals(NO_ACTION_TAKEN)){
				criticalServiceRepository.delete(criticalService);
			}
		}
	}
    
	@Scheduled(cron = "30 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("ResourceCriticalServiceOptimiser started");
	
			//first, we create criticalServices and delete old ones
			List<CriticalService> newCriticalServiceList = new ArrayList<CriticalService>();
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				for(CriticalService criticalService : getCriticalServiceListByServiceProvider(serviceProvider)) {
					newCriticalServiceList.add(criticalService);
				}
			}
			for(CriticalService criticalService : newCriticalServiceList) {
				saveOrMerge(criticalService);
			}
			keepOnlyThisListAndOldRecordsWithActions(newCriticalServiceList);
	
			if(newCriticalServiceList.size() > 0) {
				//an event to track activities
				Event event = new Event();
				event.setCategory("ResourceCriticalServiceOptimiser");
				event.setDetails("monitoring started");
				eventRepository.save(event);
			}
	
			
			//then, we check what to do with them
			for(CriticalService criticalService : criticalServiceRepository.getAllOrderedByScoreDesc()) {
				log.info("ResourceCriticalService " + criticalService + " has score " + criticalService.getScore());
				
				// we activate only if the score is greater than the warning threshold
				if(criticalService.getScore() > SCORE_THRESHOLD) {
					String actionTaken = serviceResourceOptimiser.optimise(criticalService.getService(), true);
					criticalService.setActionTaken(actionTaken);
				}
				else {
					criticalService.setActionTaken(NO_ACTION_TAKEN);
				}
				criticalService.setTimestampProcessed(Instant.now());
				saveOrMerge(criticalService);
			}
		}
	}
		
    private List<CriticalService> getCriticalServiceListByServiceProvider(ServiceProvider serviceProvider) {
		List<CriticalService> result = new ArrayList<CriticalService>();
		
		//create the criticalService list iterating over the ServiceProviders
		if(serviceProvider != null) {
			Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
			int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
			
			Instant stopTime = Instant.now();
			Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
			
			//create a Map service -> List<SlaViolation>
			Map<Service, List<SlaViolation>> serviceSlaViolationMap = new HashMap<Service, List<SlaViolation>>();
			for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SlaViolationStatus.elab_resources_needed.name(), ServiceOptimisationType.resources.name(), startTime)) {
				if(!serviceSlaViolationMap.containsKey(slaViolation.getSla().getService())) {
					serviceSlaViolationMap.put(slaViolation.getSla().getService(), new ArrayList<SlaViolation>());	
				}
				serviceSlaViolationMap.get(slaViolation.getSla().getService()).add(slaViolation);
				slaViolation.setStatus(SlaViolationStatus.closed_critical.toString());
				slaViolationRepository.save(slaViolation);
			}
			for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SlaViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.resources.name(), startTime)) {
				slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
				slaViolationRepository.save(slaViolation);
			}
			//iterate over service in Map and create one CriticalService with a score computed over the associated List<SlaViolation>
			for(Service service : serviceSlaViolationMap.keySet()) {
				if(service.getStatus().equals(ExecStatus.RUNNING)) {

					List<SlaViolation> slaViolationList = serviceSlaViolationMap.get(service);
					
					//compute the score
					long score = getScore(service, startTime, stopTime, slaViolationList);
					//create a criticalService per Service if score is above threshold
					if(score > SCORE_THRESHOLD) {
						CriticalService criticalService = new CriticalService();
						criticalService.setTimestampCreated(Instant.now());
	
						criticalService.setId(service.getId());
						criticalService.setService(service);
						criticalService.setMonitoringPeriodSec(monitoringSlaViolationPeriodSec);
						criticalService.setDetails(getDetails(startTime, stopTime, slaViolationList));
						
						criticalService.setScore(score);
						
						result.add(criticalService);
					}
				}
			}
		}
		
		//sort, giving precedence to higher scores
		Collections.sort(result, Collections.reverseOrder());
		return result;
	}
    
    private int getScore(Service service, Instant startTime, Instant stopTime, List<SlaViolation> slaViolationList) {
		double result = SCORE_THRESHOLD;

		int timeFactor = 1;
		int severityFactor = 1;
		int resourceAvailabilityFactor = 1;
		
		for(int i=0; i<slaViolationList.size(); i++) {
			
			SlaViolation slaViolation = slaViolationList.get(i);
			
			double timeScore = getTimeScore(slaViolation, startTime, stopTime, timeFactor);
			double slaViolationSeverityScore = getSlaViolationSeverityScore(slaViolation, severityFactor);
			double value = timeScore + slaViolationSeverityScore;

			result += value;
		}
		double resourceAvailabilityScore = getResourceAvailabilityScore(service, resourceAvailabilityFactor);
		result -= resourceAvailabilityScore;
		return (int) result;
	}
	
    //returns a score that is the percentage of time a violation was on in a given period of time/100. Range is [0-1]*timeFactor
	private static double getTimeScore(SlaViolation slaViolation, Instant startTime, Instant stopTime, double timeFactor) {
		long totalElapsedTime = stopTime.getEpochSecond() - startTime.getEpochSecond();
		long violationElapsedTime = slaViolation.getTimestamp().getEpochSecond() - startTime.getEpochSecond();
		double violationElapsedTimeWeight = 1.0*violationElapsedTime/totalElapsedTime;
		return timeFactor * violationElapsedTimeWeight / 100;
	}
	
	//returns a score based on how serious a violation is. Range is [0-5]*severityFactor
	private static double getSlaViolationSeverityScore(SlaViolation slaViolation, double severityFactor) {
		switch(slaViolation.getSeverityType()) {
			case Warning       : return severityFactor * 1; 
			case Mild          : return severityFactor * 2; 
			case Serious       : return severityFactor * 3; 
			case Severe        : return severityFactor * 4; 
			case Catastrophic  : return severityFactor * 5; 
			default            : return severityFactor * 0; 
		}
	}
	
	
	
	//returns a score that is related to available resources percentage. It uses a threshold, resourceBufferPercentage (eg. 20).
	//Below 20% is 0. From 20% to 100% is equal to the percentage.
	//basically, below 20%, any violation will generate a request to add more resources
	private double getResourceAvailabilityScore(Service service, int resourceAvailabilityFactor) {
		int resourceAvailabilityPercentage = 100 - resourceDataReader.getResourceUsagePercentage(service);
		int resourceBufferPercentage = Integer.parseInt(ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences()).get("monitoring.criticalServices.maxResourceBufferPercentage"));
		return resourceAvailabilityFactor * (resourceAvailabilityPercentage <= resourceBufferPercentage ? 0 : resourceAvailabilityPercentage);
	}
    
    private String getDetails(Instant startTime, Instant stopTime, List<SlaViolation> slaViolationList) {
    	
    	StringBuilder result = new StringBuilder("Monitored period is since " + startTime);
    	for(SlaViolation slaViolation : slaViolationList) {
    		result.append("\n" + slaViolation.getSeverityType() + " SLA violation received on " + slaViolation.getTimestamp());
    	}
    	
    	return result.toString();

    }

	
}
