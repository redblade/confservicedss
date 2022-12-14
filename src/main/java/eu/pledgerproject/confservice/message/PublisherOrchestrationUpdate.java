package eu.pledgerproject.confservice.message;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.config.KafkaProperties;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.repository.EventRepository;

@Service
@Transactional
public class PublisherOrchestrationUpdate {
	private static final String TOPIC = "deployment";
	private static final String KEY = "update";

	private final Logger log = LoggerFactory.getLogger(PublisherOrchestrationUpdate.class);
    private KafkaProducer<String, String> producer;
    private final EventRepository eventRepository;
    
	public PublisherOrchestrationUpdate(KafkaProperties kafkaProperties, EventRepository eventRepository) {
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
        this.eventRepository = eventRepository;
	}
	
	private JSONObject getJsonMessage(long id, String entity, String operation) {
		JSONObject result = new JSONObject();
		result.put("id", id);
		result.put("entity", entity);
		result.put("operation", operation);
		
		return result;
	}
	
	private void saveErrorEvent(String msg) {
    	if(log.isErrorEnabled()) {
    		Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("PublisherOrchestrationUpdate");
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}
	
	@Async
	public void publish(String message) {
		try {
			producer.send(new ProducerRecord<>(TOPIC, KEY, message)).get();
			log.info("PublisherOrchestrationUpdate: update sent. TOPIC: " + TOPIC + "  MSG:"+message);
		} catch (InterruptedException | ExecutionException e) {
			log.error("PublisherConfigurationUpdate", e);
			saveErrorEvent(e.getMessage());
		}
	}
	
	@Async
	public void publish(Long id, String entity, String operation) {
		try {
			String message = getJsonMessage(id, entity, operation).toString();
			producer.send(new ProducerRecord<>(TOPIC, KEY, message)).get();
			log.info("PublisherOrchestrationUpdate: update sent for entity " + entity + ". TOPIC: " + TOPIC + "  MSG:"+message);
		} catch (InterruptedException | ExecutionException e) {
			log.error("PublisherConfigurationUpdate", e);
			saveErrorEvent(e.getMessage());
		}
	}
	
	@Async
	public void publish(Long id, String entity, String operation, Map<String, String> parameters, JSONArray placeholders) {
		try {
			JSONObject message = new JSONObject();
			message.put("id", id);
			message.put("entity", entity);
			message.put("operation", operation);
			for(String key : parameters.keySet()) {
				message.put(key, parameters.get(key));
			}
			message.put("placeholders", placeholders);
			producer.send(new ProducerRecord<>(TOPIC, KEY, message.toString())).get();
			log.info("PublisherOrchestrationUpdate: update sent for entity " + entity + ". TOPIC: " + TOPIC + "  MSG:"+message);
		} catch (InterruptedException | ExecutionException e) {
			log.error("PublisherConfigurationUpdate", e);
			saveErrorEvent(e.getMessage());
		}
	}

}
