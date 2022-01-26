package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.domain.enumeration.SlaViolationType;
import eu.pledgerproject.confservice.message.dto.SlaViolationDTO;

public class DeserializerSlaViolationDTO implements Deserializer<SlaViolationDTO> {
	private final Logger log = LoggerFactory.getLogger(DeserializerSlaViolationDTO.class);


	@Override
	public SlaViolationDTO deserialize(String topic, byte[] data) {
		try {
			String source = new String(data);
			JSONObject jsonObject = new JSONObject(source);
			
			SlaViolationDTO result = new SlaViolationDTO();
			
			result.datetime = jsonObject.optString("datetime", null);
			result.description = jsonObject.optString("description", "guarantee violation");
			result.guarantee_id = jsonObject.getLong("guarantee_id");
			result.importance_name = jsonObject.optString("importanceName", null);
			if(result.importance_name == null) {
				result.importance_name = jsonObject.optString("importance_name", SlaViolationType.Serious.name());
			}
			return result;
		}catch(RuntimeException e) {
			log.error(e.getClass() + " error parsing msg " + new String(data));
			throw e;
		}
	}
    
	
}

