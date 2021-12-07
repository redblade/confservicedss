package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;

import eu.pledgerproject.confservice.message.dto.AppProfilerDTO;

public class DeserializerAppProfilerDTO implements Deserializer<AppProfilerDTO> {
    

	@Override
	public AppProfilerDTO deserialize(String topic, byte[] data) {
		String source = new String(data);
		JSONObject jsonObject = new JSONObject(source);
		
		AppProfilerDTO result = new AppProfilerDTO();
		result.service_id = jsonObject.getLong("service_id");
		result.benchmark_name = jsonObject.getString("benchmark_name");

		return result;
	}
    
	
}

