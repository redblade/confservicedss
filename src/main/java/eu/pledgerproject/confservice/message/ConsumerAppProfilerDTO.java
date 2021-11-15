package eu.pledgerproject.confservice.message;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.message.dto.AppProfilerDTO;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class ConsumerAppProfilerDTO { 
    private static final Logger log = LoggerFactory.getLogger(ConsumerAppProfilerDTO.class);
    private ServiceRepository serviceRepository;
    private BenchmarkRepository benchmarkRepository;

    public ConsumerAppProfilerDTO(ServiceRepository serviceRepository, BenchmarkRepository benchmarkRepository) {
    	this.serviceRepository = serviceRepository;
    	this.benchmarkRepository = benchmarkRepository;
    }
    
    @KafkaListener(topics = "app_profiler", groupId = "id", containerFactory = "appProfilerDTOListener") 
    public void consume(AppProfilerDTO message) { 
    	log.info("New AppProfilerDTO received: " + message); 
    	
    	Optional<Service> serviceOpt = serviceRepository.findById(message.service_id);
    	Optional<Benchmark> benchmarkOpt = benchmarkRepository.findById(message.benchmark_id);
    	if(serviceOpt.isPresent() && benchmarkOpt.isPresent()) {
    		Service serviceDB = serviceOpt.get();
    		serviceDB.setProfile(benchmarkOpt.get().getName());
    		serviceRepository.save(serviceDB);
    	}
    	else {
        	log.warn("New AppProfilerDTO received is referring not existing Service or Benchmark: " + message); 
    	}
    } 
    
} 