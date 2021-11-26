package eu.pledgerproject.confservice.message;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO.ResourceAppMetricDTO;
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@Repository
public class ConsumerResourceMetricsDTO { 
	public static final String RESOURCE_USED = "resource-used";
	public static final String MONITORING_TYPE = "kafka";

    private static final Logger log = LoggerFactory.getLogger(ConsumerResourceMetricsDTO.class);

    private final ServiceRepository serviceRepository;
    private final NodeRepository nodeRepository;
    private final NodeReportRepository nodeReportRepository;
    private final ServiceReportRepository serviceReportRepository;
    
    public ConsumerResourceMetricsDTO(ServiceRepository serviceRepository, NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, ServiceReportRepository serviceReportRepository) {
    	this.serviceRepository = serviceRepository;
    	this.nodeRepository = nodeRepository;
    	this.nodeReportRepository = nodeReportRepository;
    	this.serviceReportRepository = serviceReportRepository;
    }
    @KafkaListener(topics = "UC-metrics", groupId = "id", containerFactory = "resourceMetricDTOListener") 
    public void consume(ResourceMetricDTO message) { 
    	log.info("New ResourceMetricDTO received: " + message);
    	
    	if(message.documentType != null && message.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC)) {
    		Optional<Node> node = nodeRepository.findByName(message.nodeName);
    		if(node.isPresent()) {
	    		NodeReport nodeReportCPU = new NodeReport();
	    		nodeReportCPU.setTimestamp(message.timestamp);
	    		nodeReportCPU.setCategory(RESOURCE_USED);
	    		nodeReportCPU.setKey(MonitoringService.CPU_LABEL);
	    		nodeReportCPU.setValue(message.totalUsedCpuMillicore);
	    		nodeReportCPU.setNode(node.get());
	    		nodeReportRepository.save(nodeReportCPU);
	    		NodeReport nodeReportMem = new NodeReport();
	    		nodeReportMem.setTimestamp(message.timestamp);
	    		nodeReportMem.setCategory(RESOURCE_USED);
	    		nodeReportMem.setKey(MonitoringService.MEMORY_LABEL);
	    		nodeReportMem.setValue(message.totalUsedMemoryMB);
	    		nodeReportMem.setNode(node.get());
	    		nodeReportRepository.save(nodeReportMem);
    		}
    	}
    	else if(message.documentType != null && message.documentType.equals(ResourceMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)) {
    		for(ResourceAppMetricDTO appMetric : message.appMetrics) {
    		
	    		Optional<Service> service = serviceRepository.findByName(appMetric.serviceName);
	    		if(service.isPresent()) {
		    		ServiceReport serviceReportCPU = new ServiceReport();
		    		serviceReportCPU.setTimestamp(message.timestamp);
		    		serviceReportCPU.setCategory(RESOURCE_USED);
		    		serviceReportCPU.setKey(MonitoringService.CPU_LABEL);
		    		serviceReportCPU.setValue(appMetric.serviceUsedCpuMillicore);
		    		serviceReportCPU.setGroup(MONITORING_TYPE);
		    		serviceReportCPU.setService(service.get());
		    		serviceReportRepository.save(serviceReportCPU);
		    		
		    		ServiceReport serviceReportMem = new ServiceReport();
		    		serviceReportMem.setTimestamp(message.timestamp);
		    		serviceReportMem.setCategory(RESOURCE_USED);
		    		serviceReportMem.setKey(MonitoringService.MEMORY_LABEL);
		    		serviceReportMem.setValue(appMetric.serviceUsedMemoryMB);
		    		serviceReportMem.setGroup(MONITORING_TYPE);
		    		serviceReportMem.setService(service.get());

		    		serviceReportRepository.save(serviceReportMem);
	    		}
    		}
    	}
    }
    
} 