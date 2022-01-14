package eu.pledgerproject.confservice.optimisation;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;

@Component
public class RankingManager {
	
    private final DeploymentOptionsManager deploymentOptionsManager;
    private final QuotaMonitoringReader quotaMonitoringReader;
	
    public RankingManager (DeploymentOptionsManager deploymentOptionsManager, QuotaMonitoringReader quotaMonitoringReader) {
    	this.deploymentOptionsManager = deploymentOptionsManager;
    	this.quotaMonitoringReader = quotaMonitoringReader;
    }
    
    
    public SortedMap<Integer, Set<Node>> getAvailableRankingMapForRequestedResources(Service service, int newCpuRequested, int newMemRequested) {
    	SortedMap<Integer, Set<Node>> result = new TreeMap<Integer, Set<Node>>();
    	
    	SortedMap<Integer, Set<Node>> serviceDeploymentOptions = deploymentOptionsManager.getServiceDeploymentOptions(service.getId());
		//check first the options with better ranking values (LOWER is BETTER), then down to the bottom
		for(int ranking : serviceDeploymentOptions.keySet()) {
			Set<Node> temporaryCandidateNodeSet = serviceDeploymentOptions.get(ranking);
			//check if temporaryCandidateNodeSet can manage the service resources
			Set<Node> filteredNodeSet = quotaMonitoringReader.filterNodeSetThatCanHostResourceRequest(service.getApp().getServiceProvider(), temporaryCandidateNodeSet, newCpuRequested, newMemRequested);
			if(filteredNodeSet.size() > 0) {
				result.put(ranking, filteredNodeSet);
			}
		}
		
		return result;
    }
    
	/*
	 * it returns the best ranking possible (and associated set of nodes) which can host the requested resources.
	 * The priority requested by the SP is used to FIRST choose a set of nodes, then they are filtered according to the Performance.
	 * So, the Performance IS NOT across MULTIPLE priorities, it works on the ONE that is the best available 
	 */
	public RankingData getBestAvailableRankingForRequestedResources(Service service, int newCpuRequested, int newMemRequested) {
		//check if there is space in options with different ranking. If we prioritize edge, then:
		//if a better ranking is available, maybe we are in the cloud and go back to the edge;
		//if equivalent ranking is available, either we are in the cloud or edge, we are trying to scale up and stay there
		//if worse ranking is available, maybe we are on the edge and we offload to the cloud
		//An offload should trigger 1) POD-to-POD connectivity (eg. with Istio), 2) a distributed load balancer, 3) switch off of the current running instance

		SortedMap<Integer, Set<Node>> availableRankingForRequestedResources = getAvailableRankingMapForRequestedResources(service, newCpuRequested, newMemRequested);
		if(availableRankingForRequestedResources.keySet().size() > 0) {
			int ranking = availableRankingForRequestedResources.keySet().iterator().next();
			Set<Node> candidateSet = availableRankingForRequestedResources.get(ranking);
			
			return new RankingData(ranking, candidateSet);
		}

		return new RankingData(0, new HashSet<Node>());
	}
	
	
	
}
