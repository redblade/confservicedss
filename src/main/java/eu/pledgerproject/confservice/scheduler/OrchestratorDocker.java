package eu.pledgerproject.confservice.scheduler;

import java.io.IOException;
import java.time.Instant;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.Kubernetes2DockerConverter;
import eu.pledgerproject.confservice.repository.EventRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class OrchestratorDocker {

	private final Logger log = LoggerFactory.getLogger(OrchestratorDocker.class);
	
	private final EventRepository eventRepository;

	public OrchestratorDocker(EventRepository eventRepository) {
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
	
	private static String getContainerID(String endpoint, String serviceName) throws IOException{
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
		  .url(endpoint + "/containers/json?filters={%22name%22:[%22"+serviceName+"%22]}")
		  .method("GET", null)
		  .build();
		Response response = client.newCall(request).execute();
		if(!response.isSuccessful()){
			throw new IOException("Unable to do getContainerID. Error code is " + response.code());
		}
		String responseText = response.body().string();
		JSONArray responseJSON = new JSONArray(responseText);
		return responseJSON.isEmpty() ? null : responseJSON.getJSONObject(0).optString("Id", null);
	}
	
	private static String createContainer(String endpoint, String serviceName, String serviceImage) throws IOException{
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\"Image\": \""+serviceImage+"\"}");
		Request request = new Request.Builder()
		  .url(endpoint + "/containers/create?name=" + serviceName)
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		Response response = client.newCall(request).execute();
		if(!response.isSuccessful()){
			throw new IOException("Unable to do createContainer. Error code is " + response.code());
		}

		JSONObject responseJSON = new JSONObject(response.body().string());
		return responseJSON.optString("Id", null);
	}	
	
	private static void start(String endpoint, String serviceName, String containerID) throws IOException{
		if(containerID != null) {

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = RequestBody.create(mediaType, "");
			Request request = new Request.Builder()
			  .url(endpoint + "/containers/"+containerID+"/start")
			  .method("POST", body)
			  .build();
			Response response = client.newCall(request).execute();
			if(!response.isSuccessful()) {
				throw new IOException("Unable to start serviceName " + serviceName);
			}
		}
	}
	
	private static void stop(String endpoint, String serviceName, String containerID) throws IOException{
		if(containerID != null) {
			OkHttpClient client = new OkHttpClient().newBuilder().build();

			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = RequestBody.create(mediaType, "");
			Request request = new Request.Builder()
			  .url(endpoint + "/containers/"+containerID+"/stop")
			  .method("POST", body)
			  .build();
			Response response = client.newCall(request).execute();
			if(!response.isSuccessful()) {
				throw new IOException("Unable to stop serviceName " + serviceName);
			}
		}
	}
	
	public void start(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorDocker starts " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			try {
				String endpoint = infrastructure.getEndpoint();
				String serviceName = deploymentName;
				String serviceImage = Kubernetes2DockerConverter.getServiceImage(deploymentDescriptor);
				
				String containerID = getContainerID(endpoint, serviceName);
				if(containerID == null) {
					containerID = createContainer(endpoint, serviceName, serviceImage);
				}
				start(endpoint, serviceName, containerID);
				
			}catch(IOException e) {
				log.error("OrchestratorDocker start error " + e);
				saveErrorEvent("OrchestratorDocker start error " + e.getClass() + " " + e.getMessage());
			}
		}
	}
	
	public void stop(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorDocker stop " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			try {
				String endpoint = infrastructure.getEndpoint();
				String serviceName = deploymentName;
				String serviceImage = Kubernetes2DockerConverter.getServiceImage(deploymentDescriptor);
				
				String containerID = getContainerID(endpoint, serviceName);
				if(containerID == null) {
					containerID = createContainer(endpoint, serviceName, serviceImage);
				}
				stop(endpoint, serviceName, containerID);
				
			}catch(IOException e) {
				log.error("OrchestratorDocker stop error " + e);
				saveErrorEvent("OrchestratorDocker stop error " + e.getClass() + " " + e.getMessage());
			}
		}
	}
	
	public void scale(String namespace, String deploymentName, int numberOfReplicas, Infrastructure infrastructure) {
		log.info("OrchestratorDocker scales " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			log.error("OrchestratorDocker.scale is not supported");
		}
	}
	
	public void replace(String namespace, String deploymentName, String deploymentDescriptor, Infrastructure infrastructure) {
		log.info("OrchestratorDocker replaces " + deploymentName);
		
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			log.error("OrchestratorDocker.replace is not supported");
		}
	}
	
}
