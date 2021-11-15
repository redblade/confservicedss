package eu.pledgerproject.confservice;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.repository.EventRepository;

@Component
public class ApplicationPostConstruct {

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
	}
}
