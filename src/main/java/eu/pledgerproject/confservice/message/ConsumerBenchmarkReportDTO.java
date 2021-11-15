package eu.pledgerproject.confservice.message;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.message.dto.BenchmarkReportDTO;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;

@Service
public class ConsumerBenchmarkReportDTO { 
    private static final Logger log = LoggerFactory.getLogger(ConsumerBenchmarkReportDTO.class);
    private ServiceProviderRepository serviceProviderRepository;
    private InfrastructureRepository infrastructureRepository;
    private NodeRepository nodeRepository;
    private BenchmarkRepository benchmarkRepository;
    private BenchmarkReportRepository benchmarkReportRepository;
    
    public ConsumerBenchmarkReportDTO(ServiceProviderRepository serviceProviderRepository, InfrastructureRepository infrastructureRepository, NodeRepository nodeRepository, BenchmarkRepository benchmarkRepository, BenchmarkReportRepository benchmarkReportRepository) {
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.infrastructureRepository = infrastructureRepository;
    	this.nodeRepository = nodeRepository;
    	this.benchmarkRepository = benchmarkRepository;
    	this.benchmarkReportRepository = benchmarkReportRepository;
    }
    
    @KafkaListener(topics = "benchmarking", groupId = "id", containerFactory = "benchmarkReportDTOListener") 
    public void consume(BenchmarkReportDTO message) { 
    	log.info("New BenchmarkReportDTO received: " + message); 
    	BenchmarkReport benchmarkReport = new BenchmarkReport();
    	
    	Optional<ServiceProvider> serviceProviderDB = serviceProviderRepository.findById(message.pledgerServiceProvider);
    	Optional<Infrastructure> infrastructureDB = infrastructureRepository.findById(message.pledgerInfrastructure);
    	Optional<Node> nodeDB = message.pledgerNode == null ? null : nodeRepository.findById(message.pledgerNode);

    	Optional<Benchmark> benchmarkDB = benchmarkRepository.findByName(message.workload);
    	if(!benchmarkDB.isPresent()) {
    		Benchmark benchmark = new Benchmark();
    		benchmark.setName(message.workload);
    		benchmark.setCategory(message.category);
    		if(serviceProviderDB.isPresent()) {
    			benchmark.setServiceProvider(serviceProviderDB.get());
    		}
    		if(infrastructureDB.isPresent()) {
    			benchmark.setInfrastructure(infrastructureDB.get());
    		}
    		
    		benchmarkRepository.save(benchmark);
    		benchmarkReport.setBenchmark(benchmark);
    	}
    	else {
    		benchmarkReport.setBenchmark(benchmarkDB.get());
    	}
    	benchmarkReport.setTime(Instant.now());
    	if(nodeDB != null && nodeDB.isPresent()) {
    		benchmarkReport.setNode(nodeDB.get());
		}
    	benchmarkReport.setInterval(message.interval);
    	benchmarkReport.setMean(message.mean);
    	benchmarkReport.setMetric(message.metric);
    	benchmarkReport.setStabilityIndex(message.stabilityIndex);
    	benchmarkReport.setTool(message.tool);
    	benchmarkReportRepository.save(benchmarkReport);
    	
    } 
} 