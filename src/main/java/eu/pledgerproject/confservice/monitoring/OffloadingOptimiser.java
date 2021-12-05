package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

@Component
public class OffloadingOptimiser {
    private final Logger log = LoggerFactory.getLogger(OffloadingOptimiser.class);
    
    private final ServiceProviderRepository serviceProviderRepository;
    private final SlaViolationRepository slaViolationRepository;
	private final EventRepository eventRepository;
	private final ServiceScheduler serviceScheduler;
	private final ServiceRepository serviceRepository;

    public OffloadingOptimiser(ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository, ServiceScheduler serviceScheduler, ServiceRepository serviceRepository) {
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.slaViolationRepository = slaViolationRepository;
    	this.eventRepository = eventRepository;
    	this.serviceScheduler = serviceScheduler;
    	this.serviceRepository = serviceRepository;
    }
    
    private void saveInfoEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("OffloadingOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
    
    private void saveWarnEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("OffloadingOptimiser");
		event.severity(Event.WARNING);
		eventRepository.save(event);
	}
	
		
	@Scheduled(cron = "30 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("OffloadingOptimiser started");
	
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				
				for(Service service : serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.offloading.name())){
					Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
					int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
					
					Instant stopTime = Instant.now();
					Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
					
					boolean criticalService = false;
					boolean steadyService = false;
					
					for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.elab_resources_needed.name(), ServiceOptimisationType.offloading.name(), startTime)) {
						if(service.getLastChangedStatus().isBefore(slaViolation.getTimestamp())) {
							slaViolation.setStatus(SlaViolationStatus.closed_critical.toString());
							slaViolationRepository.save(slaViolation);
							criticalService = true;
						}
						else {
							slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
							slaViolationRepository.save(slaViolation);
						}
					}
					for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.offloading.name(), startTime)) {
						slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
						slaViolationRepository.save(slaViolation);
					}
					List<SlaViolation> slaViolationCritical = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.closed_critical.name(), ServiceOptimisationType.offloading.name(), startTime);
					steadyService = slaViolationCritical.size() == 0 && service.getLastChangedStatus().isBefore(startTime);
					
					if(criticalService) {
						Node bestNode = serviceScheduler.migrateToRanking(service, false);
						if(bestNode != null) {
							log.info("Offloading to worse ranking service " + service.getName() + " to Node " + bestNode.getName() + " on Infrastructure " + bestNode.getInfrastructure().getName());
							saveInfoEvent(service, "Offloading to worse ranking service " + service.getName()+" to Node " + bestNode.getName() + " on Infrastructure " + bestNode.getInfrastructure().getName());
						}
						else {
							log.warn("There are no worse ranking options with enough resources for Offloading service " + service.getName());
							saveWarnEvent(service, "There are no other options with enough resources for Offloading");
						}
					}
					if(steadyService) {
						Node bestNode = serviceScheduler.migrateToRanking(service, true);
						if(bestNode != null) {
							log.info("Offloading to better ranking service " + service.getName() + " to Node " + bestNode.getName() + " on Infrastructure " + bestNode.getInfrastructure().getName());
							saveInfoEvent(service, "Offloading to better ranking service " + service.getName() + " to Node " + bestNode.getName() + " on Infrastructure " + bestNode.getInfrastructure().getName());
						}
					}
				}
			}
		}
	}
}
