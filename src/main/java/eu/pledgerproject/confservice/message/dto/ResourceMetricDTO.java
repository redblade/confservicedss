package eu.pledgerproject.confservice.message.dto;

import java.time.Instant;

public class ResourceMetricDTO {
	
	public static class ResourceSystemMetricDTO{
		public static final String DOCUMENT_TYPE_SYSTEM_METRIC = "SystemMonitoring";
		
		public Double totalCapacityCpuMillicore;
		public Double totalUsedCpuMillicore;
		public Double totalCapacityMemoryMB;
		public Double totalUsedMemoryMB;
		
		public ResourceSystemMetricDTO() {}
		public ResourceSystemMetricDTO(
				Double totalCapacityCpuMillicore, 
				Double totalUsedCpuMillicore, 
				Double totalCapacityMemoryMB, 
				Double totalUsedMemoryMB
		) {
			this();

			this.totalCapacityCpuMillicore = totalCapacityCpuMillicore;
			this.totalUsedCpuMillicore = totalUsedCpuMillicore;
			this.totalCapacityMemoryMB = totalCapacityMemoryMB;
			this.totalUsedMemoryMB = totalUsedMemoryMB;
		}
		@Override
		public String toString() {
			return "ResourceSystemMetricDTO [totalCapacityCpuMillicore=" + totalCapacityCpuMillicore
					+ ", totalUsedCpuMillicore=" + totalUsedCpuMillicore + ", totalCapacityMemoryMB="
					+ totalCapacityMemoryMB + ", totalUsedMemoryMB=" + totalUsedMemoryMB + "]";
		}
		
	}
	
	public static class ResourceAppMetricDTO {
		public static final String DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC = "AppResourceMonitoring";

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

	public ResourceSystemMetricDTO systemMetric;
	public ResourceAppMetricDTO appMetric;
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("ResourceMetricDTO [documentType=" + documentType + ", timestamp=" + timestamp
				+ ", infrastructureProvider=" + infrastructureProvider + ", infrastructureName=" + infrastructureName
				+ ", nodeName=" + nodeName + "[");
		if(this.systemMetric != null) {
			result.append(this.systemMetric);
		}
		result.append(" ");
		if(this.appMetric != null) {
			result.append(this.appMetric);
		}
		result.append("]");
		return result.toString();
	}

	
}

