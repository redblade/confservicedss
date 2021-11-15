package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.scheduler.TokenKubernetes;
import io.kubernetes.client.extended.kubectl.Kubectl;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1Node;

@Component
public class NodeAutodiscovery {
	public static final String DEFAULT_NODE_PROPERTIES = "{'node_type': 'cloud', 'node_master': 'true' }";
	private final Logger log = LoggerFactory.getLogger(NodeAutodiscovery.class);
	private final NodeRepository nodeRepository;
	private final EventRepository eventRepository;
	private final TokenKubernetes tokenKubernetes;

	public NodeAutodiscovery(NodeRepository nodeRepository, EventRepository eventRepository, TokenKubernetes tokenKubernetes) {
		this.nodeRepository = nodeRepository;
		this.eventRepository = eventRepository;
		this.tokenKubernetes = tokenKubernetes;
	}
	
	private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("NodeAutodiscovery");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	public void autodiscoveryNodes(Infrastructure infrastructure) {
		if("K8S".equals(infrastructure.getType())) {

			ApiClient apiClient = tokenKubernetes.getKubernetesApiClient(infrastructure);

			if(apiClient != null) {
				log.info("NodeAutodiscovery got a K8S client");
				Configuration.setDefaultApiClient(apiClient);
				
				try {
					for(V1Node v1Node : Kubectl.get(V1Node.class).execute()) {
						Node node = new Node();
						node.setName(v1Node.getMetadata().getName());
						node.setInfrastructure(infrastructure);
						node.setProperties(DEFAULT_NODE_PROPERTIES);
						String ipaddress = v1Node.getStatus().getAddresses().get(0).getAddress();
						node.setIpaddress(ipaddress);
						
						nodeRepository.save(node);
					}
					
				}catch(Exception e) {
					log.error("NodeDataAutodiscovery", e);
					saveErrorEvent("NodeDataAutodiscovery error " + e.getClass() + " " + e.getMessage());
				}
			}
		}
	}
}
