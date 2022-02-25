package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.message.dto.DeploymentFeedbackDTO;

public class DeserializerDeploymentFeedbackDTO implements Deserializer<DeploymentFeedbackDTO> {
	private final Logger log = LoggerFactory.getLogger(DeserializerDeploymentFeedbackDTO.class);

	@Override
	public DeploymentFeedbackDTO deserialize(String topic, byte[] data) {
		try {

			String source = new String(data);
			JSONObject jsonObject = new JSONObject(source);
			
			DeploymentFeedbackDTO result = new DeploymentFeedbackDTO();
			result.id = jsonObject.getLong("id");
			result.status = jsonObject.getString("status");
			return result;
		}catch(RuntimeException e) {
			log.error(e.getClass() + " error parsing msg " + new String(data));
			throw e;
		}
	}
    
	
}

