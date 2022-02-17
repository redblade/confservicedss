package eu.pledgerproject.confservice;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.repository.EventRepository;

@Component
public class ApplicationPostConstruct {
	private final Logger log = LoggerFactory.getLogger(ApplicationPostConstruct.class);

	private final EventRepository eventRepository;
	
	public ApplicationPostConstruct(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	@PostConstruct
	@Transactional
	public void cleanEventsAndCheckControlFlags() {
		eventRepository.deleteAll();
		Event event = new Event();
		event.setCategory("Init");
		event.setDetails("Main controllers started");
		eventRepository.save(event);
		
		if(ControlFlags.READ_ONLY_MODE_ENABLED) {
			log.error("READ_ONLY_MODE_ENABLED is TRUE");
			Event eventAlert = new Event();
			eventAlert.setSeverity(Event.ERROR);
			eventAlert.setCategory("READ_ONLY_MODE_ENABLED");
			eventAlert.setDetails("TRUE");
			eventRepository.save(eventAlert);
		}
		if(ControlFlags.BENCHMARK_DSS_DISABLED) {
			log.error("BENCHMARK_DSS_DISABLED is TRUE");
			Event eventAlert = new Event();
			eventAlert.setSeverity(Event.ERROR);
			eventAlert.setCategory("BENCHMARK_DSS_DISABLED");
			eventAlert.setDetails("TRUE");
			eventRepository.save(eventAlert);
		}

	}
}
