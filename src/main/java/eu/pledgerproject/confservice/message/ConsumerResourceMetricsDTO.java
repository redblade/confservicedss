package eu.pledgerproject.confservice.message;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.message.dto.ResourceMetricDTO;
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.repository.EventRepository;
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
    private final EventRepository eventRepository;
    
    public ConsumerResourceMetricsDTO(ServiceRepository serviceRepository, NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, ServiceReportRepository serviceReportRepository, EventRepository eventRepository) {
    	this.serviceRepository = serviceRepository;
    	this.nodeRepository = nodeRepository;
    	this.nodeReportRepository = nodeReportRepository;
    	this.serviceReportRepository = serviceReportRepository;
    	this.eventRepository = eventRepository;
    }
    
    private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("ConsumerResourceMetricsDTO");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
    
    @KafkaListener(topics = "UC-metrics", groupId = "id", containerFactory = "resourceMetricDTOListener") 
    public void consume(ResourceMetricDTO message) { 
    	log.info("New ResourceMetricDTO received: " + message);
    	
    	if(message.documentType != null && message.documentType.equals(ResourceMetricDTO.ResourceSystemMetricDTO.DOCUMENT_TYPE_SYSTEM_METRIC)) {
    		Optional<Node> node = nodeRepository.findByName(message.nodeName);
    		if(node.isPresent()) {
	    		NodeReport nodeReportCPU = new NodeReport();
	    		nodeReportCPU.setTimestamp(message.timestamp);
	    		nodeReportCPU.setCategory(RESOURCE_USED);
	    		nodeReportCPU.setKey(MonitoringService.CPU_LABEL);
	    		nodeReportCPU.setValue(message.systemMetric.totalUsedCpuMillicore);
	    		nodeReportCPU.setNode(node.get());
	    		nodeReportRepository.save(nodeReportCPU);
	    		NodeReport nodeReportMem = new NodeReport();
	    		nodeReportMem.setTimestamp(message.timestamp);
	    		nodeReportMem.setCategory(RESOURCE_USED);
	    		nodeReportMem.setKey(MonitoringService.MEMORY_LABEL);
	    		nodeReportMem.setValue(message.systemMetric.totalUsedMemoryMB);
	    		nodeReportMem.setNode(node.get());
	    		nodeReportRepository.save(nodeReportMem);
    		}
    		else {
    			String errorMessage = "Received a system metric on a Node which is not configured with name " + message.nodeName;
    			saveErrorEvent(errorMessage);
    			log.error(errorMessage);
    		}
    	}
    	else if(message.documentType != null && message.documentType.equals(ResourceMetricDTO.ResourceAppMetricDTO.DOCUMENT_TYPE_APPLICATION_RESOURCE_METRIC)) {
    		
    		Optional<Service> service = serviceRepository.findByName(message.appMetric.serviceName);
    		if(service.isPresent()) {
	    		ServiceReport serviceReportCPU = new ServiceReport();
	    		serviceReportCPU.setTimestamp(message.timestamp);
	    		serviceReportCPU.setCategory(RESOURCE_USED);
	    		serviceReportCPU.setKey(MonitoringService.CPU_LABEL);
	    		serviceReportCPU.setValue(message.appMetric.serviceUsedCpuMillicore);
	    		serviceReportCPU.setGroup(MONITORING_TYPE);
	    		serviceReportCPU.setService(service.get());
	    		serviceReportRepository.save(serviceReportCPU);
	    		
	    		ServiceReport serviceReportMem = new ServiceReport();
	    		serviceReportMem.setTimestamp(message.timestamp);
	    		serviceReportMem.setCategory(RESOURCE_USED);
	    		serviceReportMem.setKey(MonitoringService.MEMORY_LABEL);
	    		serviceReportMem.setValue(message.appMetric.serviceUsedMemoryMB);
	    		serviceReportMem.setGroup(MONITORING_TYPE);
	    		serviceReportMem.setService(service.get());

	    		serviceReportRepository.save(serviceReportMem);
    		}
    		else {
    			String errorMessage = "Received an application metric on a Service which is not configured with name " + message.appMetric.serviceName;
    			saveErrorEvent(errorMessage);
    			log.error(errorMessage);
    		}
    	}
    }
    
} 