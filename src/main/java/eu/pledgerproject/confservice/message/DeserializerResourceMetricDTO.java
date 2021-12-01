package eu.pledgerproject.confservice.message;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO.ResourceAppMetricDTO;

public class DeserializerResourceMetricDTO implements Deserializer<ResourceMetricDTO> {
    private static final Logger log = LoggerFactory.getLogger(DeserializerResourceMetricDTO.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@Override
	public ResourceMetricDTO deserialize(String topic, byte[] data) {
		String source = new String(data);
		JSONObject jsonObject = new JSONObject(source);
		
		ResourceMetricDTO result = new ResourceMetricDTO();
		
		if(!jsonObject.has("DocumentType")) {
			log.error("ERROR: resource metric message has no 'DocumentType'. " + source);
		}
		else {
			result.documentType = jsonObject.getString("DocumentType");
			
			if( result.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC) ||
				result.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)
			) {
			
				if(!jsonObject.has("TimeStamp")){
					log.error("ERROR: resource metric message has no 'Timestamp'. " + source);
				}
				else {
					String timestampString = jsonObject.getString("TimeStamp");
					try {
						timestampString = timestampString.indexOf(".")>0 ? timestampString.substring(0, timestampString.indexOf(".")) : timestampString;
						result.timestamp = Instant.ofEpochMilli(sdf.parse(timestampString).getTime());
					}catch(Exception e ) {
						log.warn("DeserializerResourceMetricDTO. Wrong timestamp format, using now(): got " + timestampString + ", using " + sdf.format(new Date()));
						result.timestamp = Instant.now();
					}
					
					try {
						result.infrastructureName = jsonObject.getString("InfrastructureName");
						result.infrastructureProvider = jsonObject.getString("InfrastructureProvider");
						result.nodeName = jsonObject.getString("NodeName");
				
						if(result.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC)) {
							JSONObject documentBody = jsonObject.getJSONObject("DocumentBody");
							JSONObject cpu_stats = documentBody.getJSONObject("cpu_stats");
							result.totalCapacityCpuMillicore = cpu_stats.getDouble("total_millicore");
							result.totalUsedCpuMillicore = cpu_stats.getDouble("used_millicore");
				
							JSONObject mem_stats = documentBody.getJSONObject("memory_stats");
							result.totalCapacityMemoryMB = mem_stats.getDouble("total_megabyte");
							result.totalUsedMemoryMB = mem_stats.getDouble("used_megabyte");
						}
						else if(result.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)) {
							JSONObject documentBody = jsonObject.getJSONObject("DocumentBody");
							JSONArray appMetrics = documentBody.getJSONArray("Service");
							for(int i=0; i < appMetrics.length(); i++) {
								JSONObject appMetric = appMetrics.getJSONObject(i);
								
								String serviceName = appMetric.getString("name");
								JSONObject cpu_stats = appMetric.getJSONObject("cpu_stats");
				
								Double serviceLimitCpuMillicore = cpu_stats.getDouble("limit_millicore");
								Double serviceUsedCpuMillicore = cpu_stats.getDouble("usage_millicore");
								
								JSONObject mem_stats = appMetric.getJSONObject("memory_stats");
								Double serviceLimitMemoryMB = mem_stats.getDouble("limit_megabyte");
								Double serviceUsedMemoryMB = mem_stats.getDouble("usage_megabyte");
								
								result.appMetrics.add(new ResourceAppMetricDTO(serviceName, serviceLimitCpuMillicore, serviceUsedCpuMillicore, serviceLimitMemoryMB, serviceUsedMemoryMB));
							}
						}
					}catch(Exception e) {
						log.error("ERROR: resource metric message has a wrong format. " + source, e);
					}
				}
			}
		}
		return result;
	}
    
	
}

