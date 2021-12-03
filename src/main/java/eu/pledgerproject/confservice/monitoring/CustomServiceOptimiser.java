package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceOptimisationRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Component
public class CustomServiceOptimiser {
	public static final int TIMEOUT_SEC = 10;
	
    private final Logger log = LoggerFactory.getLogger(CustomServiceOptimiser.class);
    
    private final ServiceOptimisationRepository serviceOptimisationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final SlaViolationRepository slaViolationRepository;
    private final EventRepository eventRepository;

    public CustomServiceOptimiser(ServiceOptimisationRepository serviceOptimisationRepository, ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, EventRepository eventRepository) {
    	this.serviceOptimisationRepository = serviceOptimisationRepository;
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.slaViolationRepository = slaViolationRepository;
    	this.eventRepository = eventRepository;
    }
	
    private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("CustomServiceOptimiser");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
    
    private void saveInfoEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("CustomServiceOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
    
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findServiceOptimisationOnRunningServices(ServiceOptimisationType.webhook.name());
			
			if(serviceOptimisationList.size() > 0) {
				//an event to track activities
				Event event = new Event();
				event.setCategory("CustomServiceOptimiser");
				event.setDetails("started");
				eventRepository.save(event);
			}
			
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
				int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
				
				Instant stopTime = Instant.now();
				Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
				
				for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SlaViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.webhook.name(), startTime)) {
					slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
					slaViolationRepository.save(slaViolation);
					doOptimise(serviceProvider, slaViolation.getSla().getService());
				}
				
			}
		}
	}
	
	private void doOptimise(ServiceProvider serviceProvider, Service service) {

		try {
			String url = service.getServiceOptimisation().getParameters();
			if(url != null && url.endsWith("/")) {
				url = url.substring(0, url.length()-1);
			}
			
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder()
					  .url(url+"?serviceID="+service.getId())
					  .method("GET", null)
					  .build();
			client.newCall(request).execute();
			
			log.info("CustomServiceOptimiser launched custom optimisation on Service " + service.getId());
			saveInfoEvent("Custom webhook invoked for service " + service.getName());
		}catch(Exception e) {
			log.error("CustomServiceOptimiser", e);
			saveErrorEvent("CustomServiceOptimiser error " + e.getClass() + " " + e.getMessage());
		}
	}
	
}
