package eu.pledgerproject.confservice.monitoring;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.scheduler.OrchestratorKubernetes;

@Component
public class CredentialService {
    private final Logger log = LoggerFactory.getLogger(CredentialService.class);

    private final OrchestratorKubernetes orchestratorKubernetes;
    private final ProjectRepository projectRepository;
    
    public CredentialService(OrchestratorKubernetes orchestratorKubernetes, ProjectRepository projectRepository) {
    	this.orchestratorKubernetes = orchestratorKubernetes;
    	this.projectRepository = projectRepository;
    }
    
	@EventListener(ApplicationReadyEvent.class)
	public void initialiseCredentials() {
		log.info("DSS just started, initialising credentialService..");
		updateCredentials();
		log.info("credentialService initialising completed");
	}
	
	@Scheduled(cron = "0 */5 * * * *")
	public void executeTask() {
		updateCredentials();
	}

	private void updateCredentials() {
		for(Project project : projectRepository.findAll()) {
			if(project.getInfrastructure().getType().equals("K8S")) {

				Map<String, String> projectProperties = ConverterJSON.convertToMap(project.getProperties());
				String namespace = projectProperties.get("namespace");
				String secretName = projectProperties.get("secret_name");
				
				if(namespace != null && secretName != null) {
					String token = orchestratorKubernetes.getKubernetesToken(project.getInfrastructure(), namespace, secretName);
					project.setCredentials(token);
					projectRepository.save(project);
				}
			}
		}
		log.info("credentialService updates completed");
	}
}
