package eu.pledgerproject.confservice.monitoring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;

@Component
public class SLAViolationManager {
	private static final int RESOURCE_USED_PERCENTAGE_THRESHOLD = 80;
    
    private final SlaViolationRepository slaViolationRepository;
    private final ResourceDataReader resourceDataReader;
    
    public SLAViolationManager(SlaViolationRepository slaViolationRepository, ResourceDataReader resourceDataReader) {
    	this.slaViolationRepository = slaViolationRepository;
    	this.resourceDataReader = resourceDataReader;
    }
    
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {

		for(SlaViolation slaViolation : slaViolationRepository.findAllByStatus(SlaViolationStatus.open.name())){
			Service service = slaViolation.getSla().getService();

			//we process this SLA Violation ONLY if there are no past violations on the same Service with status 'created'
			ServiceOptimisation serviceOptimisation = service.getServiceOptimisation();
			if(serviceOptimisation != null && serviceOptimisation.getOptimisation() != null) {
				
				//in case of ServiceOptimisation resource or resource_latency, we check whether there is ACTUAL need of more resources or not
				if(
						serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources_latency.name())
						||
						serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.resources.name())
				) {

					int resourceUsedPerc = resourceDataReader.getResourceUsagePercentage(slaViolation.getSla().getService());
					if(resourceUsedPerc > RESOURCE_USED_PERCENTAGE_THRESHOLD) {
						slaViolation.setStatus(SlaViolationStatus.elab_add_more_resources.name());
						slaViolationRepository.save(slaViolation);
					}
					else {
						slaViolation.setStatus(SlaViolationStatus.elab_keep_same_resources.name());
						slaViolationRepository.save(slaViolation);
					}
				}
				//else, in case ServiceOptimisation latency, energy, webhook, there are no Optimiser, so violations are moved to 'not_critical'
				else if(
						serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.latency.name())
						||
						serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.energy.name())
						||
						serviceOptimisation.getOptimisation().equals(ServiceOptimisationType.webhook.name())

				) {
					slaViolation.setStatus(SlaViolationStatus.closed_not_critical.name());
					slaViolationRepository.save(slaViolation);
				}
			}				
		}
	}
		
	
	
}
