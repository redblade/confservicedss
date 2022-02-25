package eu.pledgerproject.confservice.message;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.message.dto.AppProfilerDTO;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class ConsumerAppProfilerDTO { 
    private static final Logger log = LoggerFactory.getLogger(ConsumerAppProfilerDTO.class);
    private final ServiceRepository serviceRepository;
    private final BenchmarkRepository benchmarkRepository;
    private final EventRepository eventRepository;

    public ConsumerAppProfilerDTO(ServiceRepository serviceRepository, BenchmarkRepository benchmarkRepository, EventRepository eventRepository) {
    	this.serviceRepository = serviceRepository;
    	this.benchmarkRepository = benchmarkRepository;
    	this.eventRepository = eventRepository;
    }
    
    private void saveInfoEvent(String msg) {
    	if(log.isInfoEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("ConsumerAppProfilerDTO");
			event.severity(Event.INFO);
			eventRepository.save(event);
    	}
	}
    private void saveErrorEvent(String msg) {
    	if(log.isErrorEnabled()) {
	    	Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("ConsumerAppProfilerDTO");
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}
    
    @KafkaListener(topics = "app_profiler", groupId = "id", containerFactory = "appProfilerDTOListener") 
    public void consume(AppProfilerDTO message) { 
    	log.info("New AppProfilerDTO received: " + message); 
    	
    	Optional<Service> serviceOpt = serviceRepository.findById(message.service_id);
    	if(serviceOpt.isPresent()) {
    		Service serviceDB = serviceOpt.get();

    		String serviceProviderName = serviceDB.getApp().getServiceProvider().getName();
    		Optional<Benchmark> benchmarkOpt = benchmarkRepository.findByBenchmarkNameAndServiceProviderName(message.benchmark_name, serviceProviderName);
    		if(benchmarkOpt.isPresent()) {
	    		serviceDB.setProfile(benchmarkOpt.get().getName());
	    		serviceRepository.save(serviceDB);
	    		saveInfoEvent("AppProfiler sent a Service->Benchmark match: " + serviceDB.getName() + " is best represented by Benchmark " + benchmarkOpt.get().getName() );
    		}
    		else {
            	log.warn("AppProfiler sent a wrong Service(id)->Benchmark(name) match: " + message.service_id + "->" + message.benchmark_name + "; serviceProvider " + serviceProviderName + " has no benchmark with name "+ message.benchmark_name); 
            	saveErrorEvent("AppProfiler sent a wrong Service(id)->Benchmark(name) match: " + message.service_id + "->" + message.benchmark_name + "; serviceProvider " + serviceProviderName + " has no benchmark with name "+ message.benchmark_name);
    		}
    	}
    	else {
        	log.warn("AppProfiler sent a wrong Service(id)->Benchmark(name) match: " + message.service_id + "->" + message.benchmark_name + "; service_id does not exist"); 
        	saveErrorEvent("AppProfiler sent a wrong Service(id)->Benchmark(name) match: " + message.service_id + "->" + message.benchmark_name + "; service_id does not exist");
    	}
    } 
    
} 