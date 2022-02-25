package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.scheduler.TokenKubernetes;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeStatus;

/*
requires a monitoring_plugin configured as 

*/

@Component
public class NodeDataReader {
	
	private static final String RESOURCE_TEMPLATE = "{'"+MonitoringService.CPU_LABEL+"': 'CPU','"+MonitoringService.MEMORY_LABEL+"': 'MEMORY'}";

	private final Logger log = LoggerFactory.getLogger(NodeDataReader.class);

	private final InfrastructureRepository infrastructureRepository;
	private final NodeRepository nodeRepository;
	private final TokenKubernetes tokenKubernetes;
	private final EventRepository eventRepository;


	public NodeDataReader(InfrastructureRepository infrastructureRepository,  NodeRepository nodeRepository, TokenKubernetes tokenKubernetes, EventRepository eventRepository) {
		this.infrastructureRepository = infrastructureRepository;
		this.nodeRepository = nodeRepository;
		this.tokenKubernetes = tokenKubernetes;
		this.eventRepository = eventRepository;
	}
	
	private void saveErrorEvent(String msg) {
    	if(log.isErrorEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("NodeDataReader");
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}
	
	@Scheduled(cron = "0 */1 * * * *")
	public void scheduleTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("NodeDataReader started");
			Event event = new Event();
			event.setCategory("NodeDataReader");
			event.setDetails("monitoring started");
			eventRepository.save(event);
			
			for (Infrastructure infrastructure : infrastructureRepository.findAll()) {
				log.info("NodeDataReader is working on infrastructure " + infrastructure.getName());
				try {
					log.info("NodeDataReader: " + infrastructure.getName());
	
					String infrastructureType = infrastructure.getType();
					if(infrastructureType != null && infrastructureType.equals("K8S")) {
						
						ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
						
						if(client != null) {
							log.info("NodeDataReader got a K8S client");
	
							List<Node> nodeList = nodeRepository.findAllNodesByInfrastructureId(infrastructure.getId());
							
							int infrastructureCpu = 0;
							int infrastructureMemMB = 0;
							
							try {
								Configuration.setDefaultApiClient(client);
							    CoreV1Api coreApi = new CoreV1Api(client);
							    for(V1Node v1Node : coreApi.listNode(null, null, null, null, null, null, null, null, null, null).getItems()) {
							    	
							    	Node nodeFound = null;
							    	for(Node node : nodeList) {
							    		if(node.getName().equals(v1Node.getMetadata().getName())) {
							    			nodeFound = node;
							    			break;
							    		}
							    	}
							    	if(nodeFound != null && (nodeFound.getTotalResources() == null || nodeFound.getTotalResources().isEmpty())) {
							    		V1NodeStatus v1NodeStatus = v1Node.getStatus();
							    		int nodeCpu = 1000 * v1NodeStatus.getCapacity().get("cpu").getNumber().intValue();
							    		int nodeMemMB = (int) (v1NodeStatus.getCapacity().get("memory").getNumber().longValue() / 1024 / 1024);
							    		StringBuilder ipaddress = new StringBuilder();
							    		for(V1NodeAddress nodeAddress : v1NodeStatus.getAddresses()) {
							    			ipaddress.append(" " + nodeAddress.getAddress() + " ");
							    		}
							    		Map<String, String> labels = v1Node.getMetadata().getLabels();
							    		String features = ConverterJSON.convertToJSON(labels);
							    		nodeFound.setIpaddress(ipaddress.toString().trim());
							    		nodeFound.setFeatures(features);
							    		
							    		String totalResources = RESOURCE_TEMPLATE.replace("CPU", ""+nodeCpu).replace("MEMORY", ""+nodeMemMB);
							    		nodeFound.setTotalResources(totalResources);
							    		
							    		nodeRepository.saveAndFlush(nodeFound);
							    		
							    		infrastructureCpu += nodeCpu;
							    		infrastructureMemMB += nodeMemMB;
							    	}
							    }
							    
							    String totalResources = RESOURCE_TEMPLATE.replace("CPU", ""+infrastructureCpu).replace("MEMORY", ""+infrastructureMemMB);
					    		if(infrastructure.getTotalResources() == null || infrastructure.getTotalResources().length() == 0) {
					    			infrastructure.setTotalResources(totalResources);
					    			infrastructureRepository.saveAndFlush(infrastructure);
					    		}
					    		
							    
							}catch (ApiException e) {
								log.warn("Error getting info from Kubernetes " + e.getMessage());
							}
						}
						
					}
				}catch(Exception e) {
					log.error("NodeDataReader", e);
					saveErrorEvent("NodeDataReader error " + e.getClass() + " " + e.getMessage());
				}
			}
		}
	}
	
	
	
}
