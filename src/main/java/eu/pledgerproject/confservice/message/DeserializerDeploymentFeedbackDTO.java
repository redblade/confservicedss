package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;

import eu.pledgerproject.confservice.message.dto.DeploymentFeedbackDTO;

public class DeserializerDeploymentFeedbackDTO implements Deserializer<DeploymentFeedbackDTO> {
    

	@Override
	public DeploymentFeedbackDTO deserialize(String topic, byte[] data) {
		String source = new String(data);
		JSONObject jsonObject = new JSONObject(source);
		
		DeploymentFeedbackDTO result = new DeploymentFeedbackDTO();
		result.id = jsonObject.getLong("id");
		result.entity = jsonObject.getString("entity");
		result.status = jsonObject.getString("status");
		return result;
	}
    
	
}

