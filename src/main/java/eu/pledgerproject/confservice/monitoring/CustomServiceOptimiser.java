package eu.pledgerproject.confservice.monitoring;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceOptimisationRepository;

@Component
public class CustomServiceOptimiser {
	public static final int TIMEOUT_SEC = 10;
	
    private final Logger log = LoggerFactory.getLogger(CustomServiceOptimiser.class);
    
    private final ServiceOptimisationRepository serviceOptimisationRepository;
    private final EventRepository eventRepository;

    public CustomServiceOptimiser(ServiceOptimisationRepository serviceOptimisationRepository, EventRepository eventRepository) {
    	this.serviceOptimisationRepository = serviceOptimisationRepository;
    	this.eventRepository = eventRepository;
    }
	
    private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("PublisherOrchestrationUpdate");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
    
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		
		List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findServiceOptimisationOnRunningServices(ServiceOptimisationType.webhook.name());
		
		if(serviceOptimisationList.size() > 0) {
			//an event to track activities
			Event event = new Event();
			event.setCategory("CustomServiceOptimiser");
			event.setDetails("started");
			eventRepository.save(event);
		}
		
		for(ServiceOptimisation serviceOptimisation : serviceOptimisationList) {
			Service service = serviceOptimisation.getService();
			try {
				String url = serviceOptimisation.getParameters()+"?serviceID="+service.getId();
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setReadTimeout(TIMEOUT_SEC * 1000);

				connection.getInputStream().close();
				log.info("CustomServiceOptimiser launched custom optimisation on Service " + service.getId());
			}catch(Exception e) {
				log.error("CustomServiceOptimiser", e);
				saveErrorEvent("CustomServiceOptimiser error " + e.getClass() + " " + e.getMessage());
			}
		}
	}
	
}
