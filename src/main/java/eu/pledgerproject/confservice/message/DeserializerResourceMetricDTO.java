package eu.pledgerproject.confservice.message;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.apache.kafka.common.serialization.Deserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO.ResourceAppMetricDTO;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO.ResourceSystemMetricDTO;
import eu.pledgerproject.confservice.util.DoubleFormatter;

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
			
			if( result.documentType.equals(ResourceMetricDTO.ResourceSystemMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC) ||
				result.documentType.equals(ResourceMetricDTO.ResourceAppMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)
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
						
						if(result.documentType.equals(ResourceMetricDTO.ResourceSystemMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC)) {
							JSONObject documentBody = jsonObject.getJSONObject("DocumentBody");

							JSONObject metaInfo = documentBody.getJSONObject("meta_info");
							result.infrastructureName = metaInfo.getString("InfrastructureName");
							result.infrastructureProvider = metaInfo.getString("InfrastructureProvider");
							result.nodeName = metaInfo.getString("NodeName");

							JSONObject metrics = documentBody.getJSONObject("metrics");
							JSONObject cpu_stats = metrics.getJSONObject("cpu_stats");
							Double totalCapacityCpuMillicore = DoubleFormatter.format(cpu_stats.getDouble("total_millicore"));
							Double totalUsedCpuMillicore = DoubleFormatter.format(cpu_stats.getDouble("used_millicore"));
							
							JSONObject mem_stats = metrics.getJSONObject("memory_stats");
							Double totalCapacityMemoryMB = DoubleFormatter.format(mem_stats.getDouble("total_megabyte"));
							Double totalUsedMemoryMB = DoubleFormatter.format(mem_stats.getDouble("used_megabyte"));

							result.systemMetric = new ResourceSystemMetricDTO(totalCapacityCpuMillicore, totalUsedCpuMillicore, totalCapacityMemoryMB, totalUsedMemoryMB);
						}
						else if(result.documentType.equals(ResourceMetricDTO.ResourceAppMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)) {
							JSONObject documentBody = jsonObject.getJSONObject("DocumentBody");
							
							JSONObject metaInfo = documentBody.getJSONObject("meta_info");
							result.infrastructureName = metaInfo.getString("InfrastructureName");
							result.infrastructureProvider = metaInfo.getString("InfrastructureProvider");
							result.nodeName = metaInfo.getString("NodeName");

							JSONObject appMetric = documentBody.getJSONObject("service");
								
							String serviceName = appMetric.getString("name");

							JSONObject cpu_stats = appMetric.getJSONObject("cpu_stats");
							Double serviceLimitCpuMillicore = DoubleFormatter.format(cpu_stats.getDouble("limit_millicore"));
							Double serviceUsedCpuMillicore = DoubleFormatter.format(cpu_stats.getDouble("usage_millicore"));

							JSONObject mem_stats = appMetric.getJSONObject("memory_stats");
							Double serviceLimitMemoryMB = DoubleFormatter.format(mem_stats.getDouble("limit_megabyte"));
							Double serviceUsedMemoryMB = DoubleFormatter.format(mem_stats.getDouble("usage_megabyte"));

							result.appMetric = new ResourceAppMetricDTO(serviceName, serviceLimitCpuMillicore, serviceUsedCpuMillicore, serviceLimitMemoryMB, serviceUsedMemoryMB);
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

