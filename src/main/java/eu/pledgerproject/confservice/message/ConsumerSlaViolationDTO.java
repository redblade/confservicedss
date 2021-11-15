package eu.pledgerproject.confservice.message;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.Sla;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.domain.enumeration.SlaViolationType;
import eu.pledgerproject.confservice.message.dto.SlaViolationDTO;
import eu.pledgerproject.confservice.monitoring.SLAOptimisationType;
import eu.pledgerproject.confservice.monitoring.SlaViolationStatus;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.GuaranteeRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;

@Repository
public class ConsumerSlaViolationDTO { 
	public static final int MINS_TO_SUSPEND = 5;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final Logger log = LoggerFactory.getLogger(ConsumerSlaViolationDTO.class);
    
    private final GuaranteeRepository guaranteeRepository;
    private final SlaViolationRepository slaViolationRepository;
    private final EventRepository eventRepository;

    
    public ConsumerSlaViolationDTO(GuaranteeRepository guaranteeRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository) {
    	this.guaranteeRepository = guaranteeRepository;
    	this.slaViolationRepository = slaViolationRepository;
    	this.eventRepository = eventRepository;
    }
    
    private void saveInfoEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("ConsumerSlaViolation");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
    
    @KafkaListener(topics = "sla_violation", groupId = "id", containerFactory = "slaViolationDTOListener") 
    public void consume(SlaViolationDTO message) { 
    	log.info("New SlaViolationDTO received: " + message); 
    	
    	boolean severityFormatIsCorrect = false;
    	try {
    		SlaViolationType.valueOf(message.importance_name);
    		severityFormatIsCorrect = true;
    	}catch(Exception e) {
    		log.warn("Wrong severity format ('"+message.importance_name+"'), skipping SLAViolation");
    	}
    	
    	if(severityFormatIsCorrect) {
    		SlaViolation slaViolation = new SlaViolation();
    		Optional<Guarantee> guarantee = guaranteeRepository.findById(message.guarantee_id);
    	
	    	if(guarantee.isPresent()) {
	    		Sla sla = guarantee.get().getSla();
	    		slaViolation.setSla(sla);
	    		slaViolation.setSeverityType(SlaViolationType.valueOf(message.importance_name));

	    		//we only process violations which belong to SLA with type 'active' AND in case the service has got any others with type 'suspend' within the last X min
	    		if(SLAOptimisationType.suspend.toString().equals(sla.getType())){
	    			saveInfoEvent("DSS received a SLA violation of type 'suspend', so future violations on service " + sla.getService().getName() + " will be ignored");
	    			slaViolation.setStatus(SlaViolationStatus.closed_suspend.toString());
	    		}
	    		else if(isResourceManagementSuspendedOnService(sla.getService())) {
	    			saveInfoEvent("DSS received a SLA violation that will be ignored because a recent SLA violation of type 'suspend' was received");
	    			slaViolation.setStatus(SlaViolationStatus.closed_skip.toString());
	    		}
	    		else if(SLAOptimisationType.ignore.toString().equals(sla.getType())){
	    			slaViolation.setStatus(SlaViolationStatus.closed_ignored.toString());
	    		}
	    		else if(SLAOptimisationType.active.toString().equals(sla.getType())){
	    			slaViolation.setStatus(SlaViolationStatus.open.toString());
	    		}

	    		slaViolation.setViolationName(guarantee.get().getName());
	    		slaViolation.setDescription(message.description);
	    		Instant timestamp = Instant.now();
	    		try {
	    			timestamp = Instant.ofEpochMilli(sdf.parse("message.datetime").getTime());
	    		}catch(Exception e ) {
	    			log.warn("Wrong timestamp format, using now(): got " + "message.datetime");
	    		}
	    		slaViolation.setTimestamp(timestamp);
	    		slaViolationRepository.save(slaViolation);
	    	}
    	}
    }
    
    private boolean isResourceManagementSuspendedOnService(Service service) {
    	Instant timestamp = Instant.now().minusSeconds(60 * MINS_TO_SUSPEND);
    	List<SlaViolation> slaViolationList = slaViolationRepository.findAllByServiceAndStatusSinceTimestampRegardlessOfOptimisationType(service, SlaViolationStatus.closed_suspend.name(), timestamp);
    	return slaViolationList.size() > 0;
    }
    
} 