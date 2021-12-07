package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;

import eu.pledgerproject.confservice.message.dto.SlaViolationDTO;

public class DeserializerSlaViolationDTO implements Deserializer<SlaViolationDTO> {
    

	@Override
	public SlaViolationDTO deserialize(String topic, byte[] data) {
		String source = new String(data);
		JSONObject jsonObject = new JSONObject(source);
		
		SlaViolationDTO result = new SlaViolationDTO();
		
		result.datetime = jsonObject.getString("datetime");
		result.description = jsonObject.optString("description", "guarantee violation");
		result.guarantee_id = jsonObject.getLong("guarantee_id");
		result.importance_name = jsonObject.getString("importance_name");

		return result;
	}
    
	
}

