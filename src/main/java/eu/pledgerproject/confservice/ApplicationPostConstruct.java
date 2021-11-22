package eu.pledgerproject.confservice;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ControlFlag;
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
	public void cleanEvents() {
		eventRepository.deleteAll();
		Event event = new Event();
		event.setCategory("Init");
		event.setDetails("started");
		eventRepository.save(event);
		if(ControlFlag.READ_ONLY_MODE_ENABLED) {
			log.warn("READ ONLY MODE is ACTIVE");
			Event eventWarning = new Event();
			eventWarning.setSeverity(Event.WARNING);
			eventWarning.setCategory("READ ONLY MODE");
			eventWarning.setDetails("ACTIVE");
			eventRepository.save(eventWarning);
		}
	}
}
