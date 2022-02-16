package eu.pledgerproject.confservice.monitoring;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class PrometheusRuleManager {
    private final Logger log = LoggerFactory.getLogger(PrometheusRuleManager.class);

	public String readPrometheusRule(Guarantee guarantee) {
		String result = null;
		if(guarantee.getSla().getService().getApp().getManagementType().equals(ManagementType.MANAGED)) {
			String slamanagerURL = System.getenv("SLAMANAGER_URL");
			String slamanagerRuleNamespace = System.getenv("SLAMANAGER_RULE_NAMESPACE");
			if(slamanagerURL != null && slamanagerRuleNamespace != null) {
		
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				MediaType mediaType = MediaType.parse("application/json");
				String requestJSON = getRequestJSON(guarantee, slamanagerRuleNamespace);
				RequestBody body = RequestBody.create(mediaType, requestJSON);
				
				Request request = new Request.Builder().url(slamanagerURL+"/readSLA/").method("POST", body).build();
				Response response;
				try {
					response = client.newCall(request).execute();
				} catch (IOException e) {
					log.error("PrometheusRuleManager.readPrometheusRule error " + e);
					throw new RuntimeException(e);
				}
				
				result = response.body().toString();
			}
		}
		return result;
	}
	
	public void applyPrometheusRule(Guarantee guarantee) {
		if(guarantee.getSla().getService().getApp().getManagementType().equals(ManagementType.MANAGED)) {
			String slamanagerURL = System.getenv("SLAMANAGER_URL");
			String slamanagerRuleNamespace = System.getenv("SLAMANAGER_RULE_NAMESPACE");
			if(slamanagerURL != null && slamanagerRuleNamespace != null) {
		
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				MediaType mediaType = MediaType.parse("application/json");
				String requestJSON = getRequestJSON(guarantee, slamanagerRuleNamespace);
				RequestBody body = RequestBody.create(mediaType, requestJSON);
				
				Request request = new Request.Builder().url(slamanagerURL+"/applySLA/").method("POST", body).build();
				try {
					client.newCall(request).execute();
				} catch (IOException e) {
					log.error("PrometheusRuleManager.applyPrometheusRule error " + e);
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public void deletePrometheusRule(Guarantee guarantee) {
		if(guarantee.getSla().getService().getApp().getManagementType().equals(ManagementType.MANAGED)) {
			String slamanagerURL = System.getenv("SLAMANAGER_URL");
			String slamanagerRuleNamespace = System.getenv("SLAMANAGER_RULE_NAMESPACE");
			if(slamanagerURL != null && slamanagerRuleNamespace != null) {
		
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				MediaType mediaType = MediaType.parse("application/json");
				String requestJSON = getRequestJSON(guarantee, slamanagerRuleNamespace);
				RequestBody body = RequestBody.create(mediaType, requestJSON);
				
				Request request = new Request.Builder().url(slamanagerURL+"/deleteSLA/").method("POST", body).build();
				try {
					client.newCall(request).execute();
				} catch (IOException e) {
					log.error("PrometheusRuleManager.deletePrometheusRule error " + e);
					throw new RuntimeException(e);
				}
				
			}
		}
	}
	
	private String getRequestJSON(Guarantee guarantee, String namespace) {
		JsonObject result = new JsonObject();
		result.addProperty("sla", guarantee.getSla().getName());
		result.addProperty("namespace", namespace);
		result.add("guarantee", new JsonObject());

		JsonObject guaranteeJson = result.getAsJsonObject("guarantee");
		guaranteeJson.addProperty("id", ""+guarantee.getId());
		guaranteeJson.addProperty("name", guarantee.getName());
		guaranteeJson.addProperty("description", guarantee.getName() + " violation");
		guaranteeJson.add("thresholds", new JsonArray());

		JsonArray thresholdsArrayJson = guaranteeJson.getAsJsonArray("thresholds");
		if(guarantee.getThresholdMild() != null && !guarantee.getThresholdMild().isEmpty()) {
			JsonObject elem = new JsonObject();
			elem.addProperty("Mild", guarantee.getThresholdMild());
			thresholdsArrayJson.add(elem);
		}
		if(guarantee.getThresholdWarning() != null && !guarantee.getThresholdWarning().isEmpty()) {
			JsonObject elem = new JsonObject();
			elem.addProperty("Warning", guarantee.getThresholdWarning());
			thresholdsArrayJson.add(elem);
		}
		if(guarantee.getThresholdSerious() != null && !guarantee.getThresholdSerious().isEmpty()) {
			JsonObject elem = new JsonObject();
			elem.addProperty("Serious", guarantee.getThresholdSerious());
			thresholdsArrayJson.add(elem);
		}
		if(guarantee.getThresholdSevere() != null && !guarantee.getThresholdSevere().isEmpty()) {
			JsonObject elem = new JsonObject();
			elem.addProperty("Severe", guarantee.getThresholdSevere());
			thresholdsArrayJson.add(elem);
		}
		if(guarantee.getThresholdCatastrophic() != null && !guarantee.getThresholdCatastrophic().isEmpty()) {
			JsonObject elem = new JsonObject();
			elem.addProperty("Catastrophic", guarantee.getThresholdCatastrophic());
			thresholdsArrayJson.add(elem);
		}
		
		return result.getAsString();
	}
}
