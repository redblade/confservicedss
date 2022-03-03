package eu.pledgerproject.confservice.optimisation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;

@Component
public class SLAViolationManager {
	private final Logger log = LoggerFactory.getLogger(ECODAOptimiser.class);

	private static final int RESOURCE_USED_PERCENTAGE_THRESHOLD = 80;
    
    private final SlaViolationRepository slaViolationRepository;
    private final ResourceDataReader resourceDataReader;
    
    public SLAViolationManager(SlaViolationRepository slaViolationRepository, ResourceDataReader resourceDataReader) {
    	this.slaViolationRepository = slaViolationRepository;
    	this.resourceDataReader = resourceDataReader;
    }
    
	@Scheduled(cron = "20/15 * * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("SLAViolationManager started");
	
			for(SlaViolation slaViolation : slaViolationRepository.findAllByStatus(SLAViolationStatus.open.name())){
				Service service = slaViolation.getSla().getService();
				
				//we ignore any SLAviolations happened on not running services
				if(!ExecStatus.RUNNING.equals(service.getStatus())){
					log.info("service " + service.getName() + " is not RUNNING, slaViolation " + slaViolation.getId() + " will be ignored");
					slaViolation.setStatus(SLAViolationStatus.closed_ignored.name());
					slaViolationRepository.save(slaViolation);	
				}
				else {
					Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
					int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
					
					Instant stopTime = Instant.now();
					Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
					
					//we ignore any SLAviolations happened before the monitoring period
					if(slaViolation.getTimestamp().isBefore(startTime)) {
						log.info("service " + service.getName() + " is RUNNING, slaViolation " + slaViolation.getId() + " is too old ("+slaViolation.getTimestamp()+") and will be ignored");
						slaViolation.setStatus(SLAViolationStatus.closed_ignored.name());
						slaViolationRepository.save(slaViolation);		
					}
					//we ignore any SLAviolations happened within the grace period from the latest service status change
					//basically, if the service just started, a SLA violation is ignored if happens in the next 60s (grace period)
					else if(slaViolation.getTimestamp().isBefore(service.getLastChangedStatus().plusSeconds(OptimisationConstants.GRACE_PERIOD_SERVICE_START_FOR_SLA_SEC))) {
						log.info("service " + service.getName() + " is RUNNING, slaViolation " + slaViolation.getId() + " is too close ("+slaViolation.getTimestamp()+") to service last status change ("+service.getLastChangedStatus()+") and will be ignored");
						slaViolation.setStatus(SLAViolationStatus.closed_ignored.name());
						slaViolationRepository.save(slaViolation);	
					}
					else {
	
						ServiceOptimisation serviceOptimisation = service.getServiceOptimisation();
						if(serviceOptimisation != null && serviceOptimisation.getOptimisation() != null) {
							
							
							//in case of ServiceOptimisation scaling or offloading, it is considered as critical without resource checking
							if(
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.scaling.name())
									||
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.offloading.name())
							) {
			
								//These two optimisations are really basic: shall we at least verify there is need of resources here? It seems UCs do not need this, so this check is disabled
								slaViolation.setStatus(SLAViolationStatus.elab_resources_needed.name());
								slaViolationRepository.save(slaViolation);
	
							}
							//in case of ServiceOptimisation resource or resources_latency or resources_latency_faredge, we check whether there is ACTUAL need of more resources or not
							else if(
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources.name())
									||
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources_latency.name())
									||
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources_latency_faredge.name())
							) {
			
								int resourceUsedPerc = resourceDataReader.getResourceUsagePercentage(slaViolation.getSla().getService());
								if(resourceUsedPerc > RESOURCE_USED_PERCENTAGE_THRESHOLD) {
									slaViolation.setStatus(SLAViolationStatus.elab_resources_needed.name());
									slaViolationRepository.save(slaViolation);
								}
								else {
									slaViolation.setStatus(SLAViolationStatus.elab_no_action_needed.name());
									slaViolationRepository.save(slaViolation);
								}
							}
							//else, in case ServiceOptimisation latency, webhook, there are no Optimiser, so violations are moved to 'not_critical'
							else if(
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.latency.name())
									||
									serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.latency_faredge.name())
							) {
								slaViolation.setStatus(SLAViolationStatus.closed_not_critical.name());
								slaViolationRepository.save(slaViolation);
							}
							else if(serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.webhook.name())) {
								slaViolation.setStatus(SLAViolationStatus.elab_no_action_needed.name());
								slaViolationRepository.save(slaViolation);					
							}
						}			
					}
				}
			}
		}
	}
		
	
	
}
