package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.repository.EventRepository;

@Component
public class LogCleanerService {
	public static final int REPORT_CLEAN_HOUR = 24;
	
	private final Logger log = LoggerFactory.getLogger(LogCleanerService.class);
	private final EventRepository eventRepository;
	
	public LogCleanerService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	@Scheduled(cron = "0 0 */1 * * *")
	@Transactional
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("LogCleaner removed events older than " + REPORT_CLEAN_HOUR + " h");
			
			Event event = new Event();
			event.setCategory("LogCleaner");
			event.setDetails("monitoring started");
			eventRepository.save(event);
			
			eventRepository.deleteOld(Instant.now().minusSeconds(REPORT_CLEAN_HOUR*60*60));
		}
	}

}
