package eu.pledgerproject.confservice.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.message.dto.BenchmarkReportDTO;

public class DeserializerBenchmarkReportDTO implements Deserializer<BenchmarkReportDTO> {
    
	private final Logger log = LoggerFactory.getLogger(DeserializerBenchmarkReportDTO.class);

	@Override
	public BenchmarkReportDTO deserialize(String topic, byte[] data) {
		try {
			String source = new String(data);
			JSONObject jsonObject = new JSONObject(source);
			
			BenchmarkReportDTO result = new BenchmarkReportDTO();
			result.category = jsonObject.optString("categories", null);
			result.interval = jsonObject.getInt("interval");
			result.mean = jsonObject.getDouble("mean");
			result.metric = jsonObject.getString("metric");
			result.pledgerInfrastructure = jsonObject.isNull("pledgerInfrastructure") ? null : jsonObject.getLong("pledgerInfrastructure");
			result.pledgerNode = jsonObject.isNull("pledgerNode") ? null : jsonObject.getLong("pledgerNode");
			result.pledgerProject = jsonObject.isNull("pledgerProject") ? null : jsonObject.getLong("pledgerProject");
			result.pledgerServiceProvider = jsonObject.isNull("pledgerServiceProvider") ? null : jsonObject.getLong("pledgerServiceProvider");
			result.stabilityIndex = jsonObject.getDouble("stabilityIndex");
			result.tool = jsonObject.getString("tool");
			result.workloadId = jsonObject.getString("workloadId");
			return result;
		}catch(RuntimeException e) {
			log.error(e.getClass() + " error parsing msg " + new String(data));
			throw e;
		}
	}
    
	
}

