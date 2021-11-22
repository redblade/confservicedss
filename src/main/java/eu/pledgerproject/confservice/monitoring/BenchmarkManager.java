package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.repository.EventRepository;

@Component
public class BenchmarkManager {
	public static final String DEFAULT_METRIC = "performance_index";
	private int MAX_NODES_TO_SELECT_AMONG_BEST = 3;
	private final BenchmarkReportRepository benchmarkReportRepository;
	private final ResourceDataReader resourceDataReader;
	private final EventRepository eventRepository;
	
	
	public BenchmarkManager(BenchmarkReportRepository benchmarkReportRepository, ResourceDataReader resourceDataReader, EventRepository eventRepository) {
		this.benchmarkReportRepository = benchmarkReportRepository;
		this.resourceDataReader = resourceDataReader;
		this.eventRepository = eventRepository;
	}
	
	private void saveInfoEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("BenchmarkManager");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
	public Node getBestNodeUsingBenchmark(Service service, Set<Node> nodeSet) {
		Node result = null;
		Set<Node> tempResult = getBestNodeSetUsingBenchmark(service, nodeSet);
		if(tempResult.size() > 0) {
			result = resourceDataReader.getNodeWithMoreCapacityLeft(nodeSet);
		}
		
		return result;
	}
	
	//returns the best Nodes to host a service in a set of possible options.
	//for simplicity we assume the returned Nodes are ALL on the SAME INFRASTRUCTURE
	
	/*
	
	
	
	The selection of the best nodes is done as follows:
	
	1) the selection of best nodes is computed ONLY IF there is a Benchmark with BenchmarkReports with metric "performance_index" and that cover ALL the existing Nodes for that specific Service
	2) the BenchmarkReport selected are ONLY THOSE either those with matching service.profile == benchmark.name (given by AppProfiler) or, if not found, those with matching service.profile contained into benchmark.category (given by the initial configuration)  
	3) of all the BenchmarkReport with metric performance_index, it is then selected the top N with highest "mean". In the future, also "stability_index" will be considered
	4) once selected, such BenchmarkReports identify the Nodes to be returned
	
	*/
	public Set<Node> getBestNodeSetUsingBenchmark(Service service, Set<Node> nodeSet) {
		Set<Node> result = new LinkedHashSet<Node>();
		
		//TODO implement selection based on AppProfiler and on Benchmarking - provide a JUnit test class
		result.addAll(nodeSet);
		
		
		//TODO 
		/*
		List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findBenchmarkReportByCategoryMetricAndNodeSet(service.getProfile(), DEFAULT_METRIC, new ArrayList<Node>(nodeSet));
		if(benchmarkReportList.size() > 0) {
			for(int i=0; i<MAX_NODES_TO_SELECT_AMONG_BEST && i<benchmarkReportList.size(); i++) {
				result.add(benchmarkReportList.get(i).getNode());
			}
		}
		*/
		

		return result;
	}
	
}
