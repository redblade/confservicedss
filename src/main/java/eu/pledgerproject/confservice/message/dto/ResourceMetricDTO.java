package eu.pledgerproject.confservice.message.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ResourceMetricDTO {
	public static final String DOCUMENT_TYPE_SYSTEM_METRIC = "SystemMonitoring";
	public static final String DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC = "AppResourceMonitoring";
	
	public static class ResourceAppMetricDTO {
		
		public String serviceName;
		public Double serviceLimitCpuMillicore;
		public Double serviceUsedCpuMillicore;
		public Double serviceLimitMemoryMB;
		public Double serviceUsedMemoryMB;
		public ResourceAppMetricDTO() {}
		public ResourceAppMetricDTO(
				String serviceName, 
				Double serviceLimitCpuMillicore, 
				Double serviceUsedCpuMillicore,
				Double serviceLimitMemoryMB, 
				Double serviceUsedMemoryMB
		) {
			this();
			
			this.serviceName = serviceName;
			this.serviceLimitCpuMillicore = serviceLimitCpuMillicore;
			this.serviceUsedCpuMillicore = serviceUsedCpuMillicore;
			this.serviceLimitMemoryMB = serviceLimitMemoryMB;
			this.serviceUsedMemoryMB = serviceUsedMemoryMB;
		}
		@Override
		public String toString() {
			return "ResourceAppMetricDTO [serviceName=" + serviceName + ", serviceLimitCpuMillicore="
					+ serviceLimitCpuMillicore + ", serviceUsedCpuMillicore=" + serviceUsedCpuMillicore
					+ ", serviceLimitMemoryMB=" + serviceLimitMemoryMB + ", serviceUsedMemoryMB=" + serviceUsedMemoryMB
					+ "]";
		}
		
	}
	
    public String documentType; 
    public Instant timestamp;
	public String infrastructureProvider;
	public String infrastructureName;
	public String nodeName;

	public Double totalCapacityCpuMillicore;
	public Double totalUsedCpuMillicore;
	public Double totalCapacityMemoryMB;
	public Double totalUsedMemoryMB;

	public List<ResourceAppMetricDTO> appMetrics;
	
	public ResourceMetricDTO() {
		this.appMetrics = new ArrayList<ResourceAppMetricDTO>();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("ResourceMetricDTO [documentType=" + documentType + ", timestamp=" + timestamp
				+ ", infrastructureProvider=" + infrastructureProvider + ", infrastructureName=" + infrastructureName
				+ ", nodeName=" + nodeName + "[");
		for(ResourceAppMetricDTO appMetric : appMetrics) {
			result.append("{"+appMetric+"},");
		}
		result.append("]");
		return result.toString();
	}

	
}

