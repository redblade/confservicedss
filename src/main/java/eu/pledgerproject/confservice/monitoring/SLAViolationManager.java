package eu.pledgerproject.confservice.monitoring;

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
    
	@Scheduled(cron = "15/30 * * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("SLAViolationManager started");
	
			for(SlaViolation slaViolation : slaViolationRepository.findAllByStatus(SlaViolationStatus.open.name())){
				Service service = slaViolation.getSla().getService();
				Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
				int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
				
				Instant stopTime = Instant.now();
				Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
				
				if(slaViolation.getTimestamp().isBefore(startTime)) {
					slaViolation.setStatus(SlaViolationStatus.closed_ignored.name());
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
		
							slaViolation.setStatus(SlaViolationStatus.elab_resources_needed.name());
							slaViolationRepository.save(slaViolation);
						}
						//in case of ServiceOptimisation resource or resource_latency, we check whether there is ACTUAL need of more resources or not
						else if(
								serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources_latency.name())
								||
								serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources.name())
						) {
		
							int resourceUsedPerc = resourceDataReader.getResourceUsagePercentage(slaViolation.getSla().getService());
							if(resourceUsedPerc > RESOURCE_USED_PERCENTAGE_THRESHOLD) {
								slaViolation.setStatus(SlaViolationStatus.elab_resources_needed.name());
								slaViolationRepository.save(slaViolation);
							}
							else {
								slaViolation.setStatus(SlaViolationStatus.elab_no_action_needed.name());
								slaViolationRepository.save(slaViolation);
							}
						}
						//else, in case ServiceOptimisation latency, energy, webhook, there are no Optimiser, so violations are moved to 'not_critical'
						else if(
								serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.latency.name())
								||
								serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.energy.name())
						) {
							slaViolation.setStatus(SlaViolationStatus.closed_not_critical.name());
							slaViolationRepository.save(slaViolation);
						}
						else if(serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.webhook.name())) {
							slaViolation.setStatus(SlaViolationStatus.elab_no_action_needed.name());
							slaViolationRepository.save(slaViolation);					
						}
					}			
				}
			}
		}
	}
		
	
	
}
