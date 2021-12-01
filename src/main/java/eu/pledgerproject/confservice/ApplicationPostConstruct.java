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
		event.setDetails("started");
		eventRepository.save(event);
		
		if(ControlFlags.READ_ONLY_MODE_ENABLED) {
			log.error("READ_ONLY_MODE is ACTIVE");
			Event eventAlert = new Event();
			eventAlert.setSeverity(Event.ERROR);
			eventAlert.setCategory("READ ONLY MODE");
			eventAlert.setDetails("ACTIVE");
			eventRepository.save(eventAlert);
		}
		if(ControlFlags.NO_BENCHMARK_FILTER_ENABLED) {
			log.error("NO_BENCHMARK_FILTER is ACTIVE");
			Event eventAlert = new Event();
			eventAlert.setSeverity(Event.ERROR);
			eventAlert.setCategory("NO_BENCHMARK_FILTER");
			eventAlert.setDetails("ACTIVE");
			eventRepository.save(eventAlert);
		}

	}
}
