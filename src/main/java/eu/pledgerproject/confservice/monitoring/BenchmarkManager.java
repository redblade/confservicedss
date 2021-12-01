package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@Component
public class BenchmarkManager {
	private final Logger log = LoggerFactory.getLogger(BenchmarkManager.class);

	public static final String DEFAULT_METRIC = "performance_index";
	public static final int DEFAULT_SEC_CHECK_BENCHMARK_REPORT = 60*60*24*7;
	
	private final BenchmarkReportRepository benchmarkReportRepository;
	private final ResourceDataReader resourceDataReader;
	private final EventRepository eventRepository;
	private final ServiceRepository serviceRepository;
	private final NodeRepository nodeRepository;
	
	
	public BenchmarkManager(BenchmarkReportRepository benchmarkReportRepository, ResourceDataReader resourceDataReader, EventRepository eventRepository, ServiceRepository serviceRepository, NodeRepository nodeRepository) {
		this.benchmarkReportRepository = benchmarkReportRepository;
		this.resourceDataReader = resourceDataReader;
		this.eventRepository = eventRepository;
		this.serviceRepository = serviceRepository;
		this.nodeRepository = nodeRepository;
	}
	
	private void saveInfoEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("BenchmarkManager");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
	//returns the best Nodes to host a service in a set of possible options.
	//for simplicity we assume the returned Nodes are ALL on the SAME INFRASTRUCTURE
	
	/*
	The selection of the best nodes is done as follows:
	
	1) the selection of best nodes is computed ONLY IF there is a Benchmark with BenchmarkReports with metric "performance_index" and that cover ALL the existing Nodes for that specific Service in the last period of time (7 days)
	2) the BenchmarkReport selected are ONLY THOSE either those with matching service.profile == benchmark.name (given by AppProfiler) or, if not found, those with matching service.profile contained into benchmark.category (given by the initial configuration)  
	3) of all the BenchmarkReport with metric performance_index, it is then selected the one with highest "mean". In the future, also "stability_index" will be considered
	
	4) if any of the conditions above fails, the best Node returned is the one with more capacity left
	
	*/
	public Node getBestNodeUsingBenchmark(Service service, Set<Node> nodeSet) {
		
		//here, we check IF service.getProfile() is configured (either manually or by the AppProfiler) with the matching benchmarkName
		String benchmarkName = service.getProfile();
		List<Object> nodeMeanByBenchmarkNameList = benchmarkReportRepository.findNodeMeanFromBenchmarkReportByBenchmarkNameMetricAndTimestampAndNodeSet(benchmarkName, DEFAULT_METRIC, Instant.now().minusSeconds(DEFAULT_SEC_CHECK_BENCHMARK_REPORT), nodeSet);
		if(nodeMeanByBenchmarkNameList.size() > 0) {
			
			double maxFound = 0;
			Node nodeFound = null;
			for(Object nodeMean : nodeMeanByBenchmarkNameList) {
				Object[] nodeMeanArray = (Object[]) nodeMean;
				double value = ((Double)nodeMeanArray[1]);
				if(value > maxFound) {
					nodeFound = (Node) nodeMeanArray[0];
					maxFound = value;
				}
			}
			if(nodeFound != null) {
				if(!ControlFlags.NO_BENCHMARK_FILTER_ENABLED) {
					log.info("Found best node using Benchmarks using benchmarkName by AppProfiler. Node: " + nodeFound.getName());
					saveInfoEvent("Found best node using Benchmarks using benchmarkName by AppProfiler. Node: " + nodeFound.getName());

					return nodeFound;
				}
				else {
					log.warn("Found best node using Benchmarks using benchmarkName by AppProfiler but FILTER is not enabled. Node: " + nodeFound.getName());
				}
			}
		}
		
		//IF NOT, we check IF service.getProfile() is configured (manually) with a category with matches with those in the Benchmark
		String categoryLike = "%\""+service.getProfile()+"\"%";
		List<Object> nodeMeanByBenchmarkCategoryList = benchmarkReportRepository.findNodeMeanFromBenchmarkReportByCategoryMetricAndTimestampAndNodeSet(categoryLike, DEFAULT_METRIC, Instant.now().minusSeconds(DEFAULT_SEC_CHECK_BENCHMARK_REPORT), nodeSet);
		if(nodeMeanByBenchmarkCategoryList.size()  > 0) {
			
			double maxFound = 0;
			Node nodeFound = null;
			for(Object nodeMean : nodeMeanByBenchmarkCategoryList) {
				Object[] nodeMeanArray = (Object[]) nodeMean;
				double value = ((Double)nodeMeanArray[1]);
				if(value > maxFound) {
					nodeFound = (Node) nodeMeanArray[0];
					maxFound = value;
				}
			}
			if(nodeFound != null) {
				if(!ControlFlags.NO_BENCHMARK_FILTER_ENABLED) {
					log.info("Found best node using Benchmarks using labels. Node: " + nodeFound.getName());
					saveInfoEvent("Found best node using Benchmarks using labels. Node: " + nodeFound.getName());

					return nodeFound;
				}
				else {
					log.warn("Found best node using Benchmarks using labels but FILTER is not enabled. Node: " + nodeFound.getName());
				}
			}
		}

		//IF NOT, we return the Node with more capacity left
		Node nodeFound = resourceDataReader.getNodeWithMoreCapacityLeft(nodeSet);
		
		log.info("Found best node using remaining capacity. Node: " + nodeFound.getName());
		
		return nodeFound;
	}
	
	//this is exposed via REST, for testing purposes
	public Optional<String> getBestNodeUsingBenchmark(long serviceID, long infrastructureID) {
		String result = null;
		Optional<Service> service = serviceRepository.findById(serviceID);
		List<Node> nodeList = nodeRepository.findAllNodesByInfrastructureId(infrastructureID);
		if(service.isPresent()) {
			result = getBestNodeUsingBenchmark(service.get(), new HashSet<Node>(nodeList)).getName(); 
		}
		
		return Optional.of(result);
	}
}
