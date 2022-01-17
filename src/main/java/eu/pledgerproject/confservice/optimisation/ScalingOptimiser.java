package eu.pledgerproject.confservice.optimisation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

/**
this optimiser implements "scaling" Optimisation

*/


@Component
public class ScalingOptimiser {
    public static final String SCALING_HORIZONTAL = "horizontal";
    public static final String SCALING_VERTICAL = "vertical";


    private final Logger log = LoggerFactory.getLogger(ScalingOptimiser.class);
    
    private final ServiceProviderRepository serviceProviderRepository;
    private final SlaViolationRepository slaViolationRepository;
	private final EventRepository eventRepository;
	private final ServiceScheduler serviceScheduler;
	private final QuotaMonitoringReader quotaMonitoringReader;
	private final ServiceRepository serviceRepository;

    public ScalingOptimiser(ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository, ServiceScheduler serviceScheduler, QuotaMonitoringReader quotaMonitoringReader, ServiceRepository serviceRepository) {
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.slaViolationRepository = slaViolationRepository;
    	this.eventRepository = eventRepository;
    	this.serviceScheduler = serviceScheduler;
    	this.quotaMonitoringReader = quotaMonitoringReader; 
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
	
    private void saveWarnEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("ScalingOptimiser");
		event.severity(Event.WARNING);
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
					
					boolean criticalService = false;
					boolean steadyService = false;
					for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.elab_resources_needed.name(), ServiceOptimisationType.scaling.name(), startTime)) {
						if(service.getLastChangedStatus().isBefore(slaViolation.getTimestamp())) {
							slaViolation.setStatus(SLAViolationStatus.closed_critical.toString());
							slaViolationRepository.save(slaViolation);
							criticalService = true;
						}
						else {
							slaViolation.setStatus(SLAViolationStatus.closed_not_critical.toString());
							slaViolationRepository.save(slaViolation);
						}
					}
					for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.scaling.name(), startTime)) {
						slaViolation.setStatus(SLAViolationStatus.closed_not_critical.toString());
						slaViolationRepository.save(slaViolation);
					}

					List<SlaViolation> slaViolationCritical = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.closed_critical.name(), ServiceOptimisationType.scaling.name(), startTime);
					steadyService = slaViolationCritical.size() == 0 && service.getLastChangedStatus().isBefore(startTime);
					
					String autoscalePercentage = preferences.get("autoscale.percentage");
					String scaling = ConverterJSON.convertToMap(service.getInitialConfiguration()).get("scaling"); 
					if(SCALING_VERTICAL.equals(scaling)){
						//get max resource requests for the current service
						Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
						Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);
						
						Integer minCpuRequest = ResourceDataReader.getServiceMinCpuRequest(service);
						Integer minMemRequest = ResourceDataReader.getServiceMinMemRequest(service);
							
							
						//compute the new resource requests, for scale up/down
						int newCpuRequested = cpuRequest;
						if(criticalService) {
							newCpuRequested = (int) (cpuRequest * (1+Integer.parseInt(autoscalePercentage)/100.0));
						}
						else if(steadyService) {
							newCpuRequested = (int) (cpuRequest / (1+Integer.parseInt(autoscalePercentage)/100.0));
						}
						int newMemRequested = memRequest;
						if(criticalService) {
							newMemRequested = (int) (memRequest * (1+Integer.parseInt(autoscalePercentage)/100.0));
						}
						else if(steadyService) {
							newMemRequested = (int) (memRequest / (1+Integer.parseInt(autoscalePercentage)/100.0));
						}

						Integer[] remainingCapacityForSPCurrentRankingNodes = quotaMonitoringReader.getRemainingCapacityForSPCurrentRankingNodes(serviceProvider, service);
						
						if(criticalService) {
							if(newCpuRequested < remainingCapacityForSPCurrentRankingNodes[0] && newMemRequested < remainingCapacityForSPCurrentRankingNodes[1]) {
								serviceScheduler.scaleVertically(service, newCpuRequested, newMemRequested, true);
								log.info("Scaling up service " + service.getName());
								saveInfoEvent(service, "Scaling up " + service.getName());
							}
							else {
								log.warn("Not enough resources for Scaling up service " + service.getName());
								saveWarnEvent(service, "Not enough resources for Scaling up service " + service.getName());
							}
						}
						else if(steadyService && newCpuRequested >= minCpuRequest && newMemRequested >= minMemRequest){
							serviceScheduler.scaleVertically(service, newCpuRequested, newMemRequested, false);
							log.info("Scaling down service " + service.getName());
							saveInfoEvent(service, "Scaling down " + service.getName());
						}
					}
					else if(SCALING_HORIZONTAL.equals(scaling)){
						Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
						Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);
						
						String replicasString = ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");
						int replicas = replicasString == null ? 1 : Integer.parseInt(replicasString);

						Integer[] remainingCapacityForSPCurrentRankingNodes = quotaMonitoringReader.getRemainingCapacityForSPCurrentRankingNodes(serviceProvider, service);
						
						if(criticalService) {
							if((replicas+1)*cpuRequest < remainingCapacityForSPCurrentRankingNodes[0] && (replicas+1)*memRequest < remainingCapacityForSPCurrentRankingNodes[1]) {
								serviceScheduler.scaleHorizontally(service, replicas+1, true);
								log.info("Scaling out service " + service.getName());
								saveInfoEvent(service, "Scaling out service " + service.getName());
							}
							else {
								log.warn("Not enough resources for Scaling out service " + service.getName());
								saveWarnEvent(service, "Not enough resources for Scaling out service " + service.getName());
							}
						}
						else if (steadyService && replicas > 1){
							serviceScheduler.scaleHorizontally(service, replicas-1, false);
							log.info("Scaling in service " + service.getName());
							saveInfoEvent(service, "Scaling in " + service.getName());
						}	
					}	
				}
			}
		}
	}
}