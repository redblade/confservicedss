package eu.pledgerproject.confservice.scheduler;

import java.io.FileReader;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.repository.EventRepository;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;

@Component
public class TokenKubernetes {

	private final Logger log = LoggerFactory.getLogger(TokenKubernetes.class);
	private final EventRepository eventRepository;

	public TokenKubernetes(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	private void saveErrorEvent(String msg) {
    	if(log.isErrorEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("OrchestratorKubernetes");
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}
	
	public ApiClient getKubernetesApiClient(Infrastructure infrastructure) {
		ApiClient client = null;
		if(infrastructure.getType().equals("K8S")) {
			Map<String, String> monitoringProperties = ConverterJSON.convertToMap(infrastructure.getMonitoringPlugin());
			String kubeconfig = monitoringProperties.get("kubeconfig");
			if(kubeconfig != null && kubeconfig.trim().length() > 0) {
				try(FileReader fileReader = new FileReader(kubeconfig)){
					client = Config.fromConfig(fileReader);
					client.setVerifyingSsl(false);
				}catch(Exception e) {
					log.error("TokenKubernetes getKubernetesApiClient kubeconfig error - " + e);
					saveErrorEvent("TokenKubernetes getKubernetesApiClient kubeconfig error - " + e.getClass() + " " + e.getMessage());
				}
			}
			else if(infrastructure.getEndpoint() != null && infrastructure.getCredentials() != null && infrastructure.getCredentials().trim().length() > 0) {
				
				try {
					client = Config.fromToken(infrastructure.getEndpoint(), infrastructure.getCredentials());
					client.setVerifyingSsl(false);
				}catch(Exception e) {
					log.error("TokenKubernetes getKubernetesApiClient token error - " + e);
					saveErrorEvent("TokenKubernetes getKubernetesApiClient token error - " + e.getClass() + " " + e.getMessage());
				}
			}
		}
		return client;
	}
	

}
