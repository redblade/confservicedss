package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

@Component
public class ScalingOptimiser {
    public static final String SCALING_HORIZONTAL = "horizontal";
    public static final String SCALING_VERTICAL = "vertical";


    private final Logger log = LoggerFactory.getLogger(ScalingOptimiser.class);
    
    private final ServiceProviderRepository serviceProviderRepository;
    private final SlaViolationRepository slaViolationRepository;
	private final EventRepository eventRepository;
	private final ServiceScheduler serviceScheduler;
	private final ServiceRepository serviceRepository;

    public ScalingOptimiser(ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository, ServiceScheduler serviceScheduler, ServiceRepository serviceRepository) {
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
		event.setCategory("ScalingOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
	
	@Scheduled(cron = "30 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("ScalingOptimiser started");
	
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				
				for(Service service : serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.scaling.name())){
					Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
					int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
					
					Instant stopTime = Instant.now();
					Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
					
					if(service.getLastChangedStatus().isBefore(startTime)) {
						boolean criticalSlaViolationFound = false;
						for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.elab_resources_needed.name(), ServiceOptimisationType.scaling.name(), startTime)) {
							slaViolation.setStatus(SlaViolationStatus.closed_critical.toString());
							slaViolationRepository.save(slaViolation);
							criticalSlaViolationFound = true;
						}
						for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.scaling.name(), startTime)) {
							slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
							slaViolationRepository.save(slaViolation);
						}
						
						
						String autoscalePercentage = preferences.get("autoscale.percentage");
						boolean increaseResources = criticalSlaViolationFound;
						
						String scaling = ConverterJSON.convertToMap(service.getInitialConfiguration()).get("scaling"); 
						if(SCALING_VERTICAL.equals(scaling)){
							//get max resource requests for the current service
							Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
							Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);
							
							Integer initialCpuRequest = ResourceDataReader.getServiceInitialMinCpuRequest(service);
							Integer initialMemRequest = ResourceDataReader.getServiceInitialMinMemRequest(service);
								
								
							//compute the new resource requests, for scale up/down
							int newCpuRequested;
							if(increaseResources) {
								newCpuRequested = (int) (cpuRequest * (1+Integer.parseInt(autoscalePercentage)/100.0));
							}
							else {
								newCpuRequested = (int) (cpuRequest / (1+Integer.parseInt(autoscalePercentage)/100.0));
							}
							int newMemRequested;
							if(increaseResources) {
								newMemRequested = (int) (memRequest * (1+Integer.parseInt(autoscalePercentage)/100.0));
							}
							else {
								newMemRequested = (int) (memRequest / (1+Integer.parseInt(autoscalePercentage)/100.0));
							}

							if(criticalSlaViolationFound) {
								serviceScheduler.scaleVertically(service, newCpuRequested, newMemRequested, true);
								log.info("Scaling up service " + service.getName());
								saveInfoEvent(service, "Scaling up");
							}
							else if(newCpuRequested >= initialCpuRequest && newMemRequested >= initialMemRequest){
								serviceScheduler.scaleVertically(service, newCpuRequested, newMemRequested, false);
								log.info("Scaling down service " + service.getName());
								saveInfoEvent(service, "Scaling down");
							}
						}
						else if(SCALING_HORIZONTAL.equals(scaling)){
							Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
							Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);
							
							String replicasString = ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");
							int replicas = replicasString == null ? 1 : Integer.parseInt(replicasString);

							if(criticalSlaViolationFound) {
								serviceScheduler.scaleHorizontally(service, replicas+1, true);
								log.info("Scaling out service " + service.getName());
								saveInfoEvent(service, "Scaling out");
							}
							else if (replicas > 1){
								serviceScheduler.scaleHorizontally(service, replicas-1, false);
								log.info("Scaling in service " + service.getName());
								saveInfoEvent(service, "Scaling in");
							}	
						}	
						
					}
				}
			}
		}
	}
}
