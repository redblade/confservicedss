package eu.pledgerproject.confservice.monitoring;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.scheduler.OrchestratorKubernetes;

@Component
public class CredentialManager {
    private final Logger log = LoggerFactory.getLogger(CredentialManager.class);

    private final PublisherConfigurationUpdate publisherConfigurationUpdate;
    private final OrchestratorKubernetes orchestratorKubernetes;
    private final ProjectRepository projectRepository;
    
    public CredentialManager(PublisherConfigurationUpdate publisherConfigurationUpdate, OrchestratorKubernetes orchestratorKubernetes, ProjectRepository projectRepository) {
    	this.publisherConfigurationUpdate = publisherConfigurationUpdate;
    	this.orchestratorKubernetes = orchestratorKubernetes;
    	this.projectRepository = projectRepository;
    }
    
	@EventListener(ApplicationReadyEvent.class)
	public void initialiseCredentials() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){
			log.info("DSS just started, initialising CredentialManager..");
			updateCredentials();
			log.info("CredentialManager initialising completed");
		}
	}
	
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){
			updateCredentials();
		}
	}

	private void updateCredentials() {
		for(Project project : projectRepository.findAll()) {
			if(project.getInfrastructure().getType().equals("K8S")) {

				if(project.getCredentials() == null || project.getCredentials().trim().length() == 0) {
				
					Map<String, String> projectProperties = ConverterJSON.convertToMap(project.getProperties());
					String namespace = projectProperties.get("namespace");
					String secretName = projectProperties.get("secret_name");
					
					if(namespace != null && secretName != null)  {
						String token = orchestratorKubernetes.getKubernetesToken(project.getInfrastructure(), namespace, secretName);
						project.setCredentials(token);
						projectRepository.save(project);
						publisherConfigurationUpdate.publish(project.getId(), "project", "update");
					}
					else if(project.getInfrastructure().getCredentials() != null && (project.getInfrastructure().getCredentials() == null || project.getInfrastructure().getCredentials().trim().length()== 0)) {
						project.setCredentials(project.getInfrastructure().getCredentials());
						projectRepository.save(project);
						publisherConfigurationUpdate.publish(project.getId(), "project", "update");
					}
				}
			}
		}
		log.info("credentialManager updates completed");
	}
}
