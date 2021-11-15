package eu.pledgerproject.confservice.monitoring;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;

@Component
public class BenchmarkManager {
	private int MAX_NODES_TO_SELECT_AMONG_BEST = 3;
	private final BenchmarkReportRepository benchmarkReportRepository;
	private final ResourceDataReader resourceDataReader;
	
	public BenchmarkManager(BenchmarkReportRepository benchmarkReportRepository, ResourceDataReader resourceDataReader) {
		this.benchmarkReportRepository = benchmarkReportRepository;
		this.resourceDataReader = resourceDataReader;
	}
	
	//returns the best Nodes to host a service in a set of possible options.
	//In case of offload, if the Node are in the cloud, latency is checked. Benchmarking will be used too in the future
	//for simplicity we assume the returned Nodes are ALL on the SAME INFRASTRUCTURE
	public Set<Node> getBestNodeSetUsingBenchmark(Service service, Set<Node> nodeSet) {
		Set<Node> result = new LinkedHashSet<Node>();
		
		List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findBenchmarkReportByNodeSet(service.getProfile(), new ArrayList<Node>(nodeSet));
		if(benchmarkReportList.size() > 0) {
			for(int i=0; i<MAX_NODES_TO_SELECT_AMONG_BEST && i<benchmarkReportList.size(); i++) {
				result.add(benchmarkReportList.get(i).getNode());
			}
		}
		else if (nodeSet.size() > 0){
			result.addAll(nodeSet);
		}

		return result;
	}
	
	public Node getBestNodeUsingBenchmark(Service service, Set<Node> nodeSet) {
		Node result = null;
		Set<Node> tempResult = getBestNodeSetUsingBenchmark(service, nodeSet);
		if(tempResult.size() > 0) {
			result = resourceDataReader.getNodeWithMoreCapacityLeft(nodeSet);
		}
		
		return result;
	}
	
	
	
}
