package eu.pledgerproject.confservice.scheduler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.repository.EventRepository;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;

@Component
public class OrchestratorKubernetes {

	private final Logger log = LoggerFactory.getLogger(OrchestratorKubernetes.class);
	
	private final TokenKubernetes tokenKubernetes;
	private final EventRepository eventRepository;

	public OrchestratorKubernetes(TokenKubernetes tokenKubernetes, EventRepository eventRepository) {
		this.tokenKubernetes = tokenKubernetes;
		this.eventRepository = eventRepository;
	}
	
	private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("OrchestratorKubernetes");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	public String getKubernetesToken(Infrastructure infrastructure, String namespace, String secretName) {
		ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);

		if(client != null) {
			log.info("OrchestratorKubernetes got a K8S client");
			Configuration.setDefaultApiClient(client);
			
			CoreV1Api coreV1Api = new CoreV1Api(client);
			try {
				V1Secret secret = coreV1Api.readNamespacedSecret(secretName, namespace, null, false, false);
				String result = new String(secret.getData().get("token"));
				
				return result;
			}catch(ApiException e) {
				log.error("OrchestratorKubernetes getKubernetesToken error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				saveErrorEvent("OrchestratorKubernetes getKubernetesToken error " + e.getClass() + " " + e.getMessage());
			}
			
		}
		
		return "";
	}
	
	public void start(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorKubernetes starts " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("OrchestratorKubernetes got a K8S client");
	
				Configuration.setDefaultApiClient(client);
				
				try {
					for(String deploymentElem : deploymentDescriptor.split("---")) {
						if(deploymentElem.contains("kind: Deployment")) {
							V1Deployment deployment = (V1Deployment) Yaml.load(deploymentElem);
							new AppsV1Api(client).createNamespacedDeployment(namespace, deployment, null, null, null);
						}
						if(deploymentElem.contains("kind: Service")) {
							V1Service service = (V1Service) Yaml.load(deploymentElem);
							new CoreV1Api(client).createNamespacedService(namespace, service, null, null, null);
	
						}
					}
	
				}catch(IOException e) {
					log.error("OrchestratorKubernetes start error " + e);
					saveErrorEvent("OrchestratorKubernetes start error " + e.getClass() + " " + e.getMessage());
				}catch(ApiException e) {
					log.error("OrchestratorKubernetes start error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
					saveErrorEvent("OrchestratorKubernetes start error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				}
	
			}
		}
	}
	
	public void stop(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorKubernetes stops " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {

			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("OrchestratorKubernetes got a K8S client");
	
				Configuration.setDefaultApiClient(client);
				
				try {
					for(String deploy : deploymentDescriptor.split("---")) {
						if(deploy.contains("kind: Service")) {
							new CoreV1Api(client).deleteNamespacedService(deploymentName, namespace, null, null, null, null, null, null);
						}
						if(deploy.contains("kind: Deployment")) {
							new AppsV1Api(client).deleteNamespacedDeployment(deploymentName, namespace, null, null, null, null, null, null);						
						}
					}
					
				}catch(ApiException e) {
					log.error("OrchestratorKubernetes stop error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
					saveErrorEvent("OrchestratorKubernetes stop error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				}
			}
		}
	}
	
	public void scale(String namespace, String deploymentName, int numberOfReplicas, Infrastructure infrastructure) {
		log.info("OrchestratorKubernetes scales horizontally " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {

			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("OrchestratorKubernetes got a K8S client");
	
				Configuration.setDefaultApiClient(client);
				
				AppsV1Api appsV1Api = new AppsV1Api(client);
					
				try {
					String jsonPatchStr = String.format("[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":%d}]", numberOfReplicas);
	
					appsV1Api.patchNamespacedDeployment(deploymentName, namespace, new V1Patch(jsonPatchStr), null, null, null, null);
				}catch(ApiException e) {
					log.error("OrchestratorKubernetes scale error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
					saveErrorEvent("OrchestratorKubernetes scale error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				}
			}
		}
	}
	
	private String replaceAnnotation(Map<String, String> annotationsOrig, Map<String, String> annotationsNew) {
		JSONObject annotationsJSON = new JSONObject();
		for(String key : annotationsOrig.keySet()) {
			if(!annotationsNew.containsKey(key)) {
				annotationsJSON.put(key, annotationsOrig.get(key));
			}
			else {
				String newValue = annotationsNew.get(key);
				if(!newValue.equals("")) {
					annotationsJSON.put(key, newValue);
				}
			}
		}
		for(String key : annotationsNew.keySet()) {
			if(!annotationsOrig.containsKey(key)) {
				String newValue = annotationsNew.get(key);
				if(!newValue.equals("")) {
					annotationsJSON.put(key, newValue);
				}
			}
		}
		
		return annotationsJSON.toString();
	}
	
	public void annotate(String namespace, String deploymentName, Map<String, String> annotationsNew, Infrastructure infrastructure) {
		log.info("OrchestratorKubernetes annotate " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {

			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("Annotate Deployment got a K8S client");
	
				Configuration.setDefaultApiClient(client);
				
				AppsV1Api appsV1Api = new AppsV1Api(client);
				try {
					V1Deployment deployment = appsV1Api.readNamespacedDeployment(deploymentName, namespace, "false", false, false);
					Map<String, String> annotationsOrig = deployment.getMetadata().getAnnotations();

					String annotationsJSON = replaceAnnotation(annotationsOrig, annotationsNew);
					String jsonPatchStr = String.format("[{\"op\":\"replace\",\"path\":\"/metadata/annotations\",\"value\":%s}]", annotationsJSON);
					
					appsV1Api.patchNamespacedDeployment(deploymentName, namespace, new V1Patch(jsonPatchStr), null, null, null, null);
				}catch(ApiException e) {
					log.error("OrchestratorKubernetes annotate error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
					saveErrorEvent("OrchestratorKubernetes annotate error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				}
			}
		}
	}

	
	public void replace(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorKubernetes replaces " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {

			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("OrchestratorKubernetes got a K8S client");
	
				Configuration.setDefaultApiClient(client);
				
				AppsV1Api appsV1Api = new AppsV1Api(client);
					
				try {
					V1Deployment deployment = (V1Deployment) Yaml.load(deploymentDescriptor);
					appsV1Api.replaceNamespacedDeployment(deploymentName, namespace, deployment, null, null, null);
				}catch(IOException e) {
					log.error("OrchestratorKubernetes start error " + e);
					saveErrorEvent("OrchestratorKubernetes replace error " + e.getClass() + " " + e.getMessage());
				}catch(ApiException e) {
					log.error("OrchestratorKubernetes replace error " + e);
					saveErrorEvent("OrchestratorKubernetes replace error " + e.getClass() + " " + ConverterJSON.getProperty(e.getResponseBody(), "message"));
				}
			}
		}
	}
	
}
