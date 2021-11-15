package eu.pledgerproject.confservice.monitoring;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

@Component
public class ServiceResourceOptimiser {
    private final Logger log = LoggerFactory.getLogger(ServiceResourceOptimiser.class);
    
    public static final String RESOURCE_USAGE_CATEGORY = "resource-usage";
    public static int SCORE_THRESHOLD = 100;
    
    public static final String SCALING_HORIZONTAL = "horizontal";
    public static final String SCALING_VERTICAL = "vertical";

    
    private final DeploymentOptionsManager deploymentOptionsManager;
    private final ResourceDataReader resourceDataReader;
    private final ServiceScheduler serviceScheduler;
    private final RankingManager rankingManager;
    private final BenchmarkManager benchmarkManager;
    
    
    public ServiceResourceOptimiser(DeploymentOptionsManager deploymentOptionsManager, ResourceDataReader resourceDataReader, ServiceScheduler serviceScheduler, RankingManager rankingManager, BenchmarkManager benchmarkManager) {
        this.deploymentOptionsManager = deploymentOptionsManager;
    	this.resourceDataReader = resourceDataReader;
    	this.serviceScheduler = serviceScheduler;
    	this.rankingManager = rankingManager;
    	this.benchmarkManager = benchmarkManager;
    }
	
	public String optimise(Service service, boolean increaseResources) {
		String message = "Nothing to do, neither horizontal or vertical scaling is configured";
		
		String scaling = ConverterJSON.convertToMap(service.getInitialConfiguration()).get("scaling"); 
		if(SCALING_VERTICAL.equals(scaling)){
			message = verticalScaling(service, increaseResources);
		}
		else if(SCALING_HORIZONTAL.equals(scaling)){
			message = horizontalScaling(service, increaseResources);
		}	
		
		return message;
	}
	
	private String verticalScaling(Service service, boolean increaseResources) {
		//if nothing can be done (no resources), this message will be returned
		String message = "no nodes or resources available";
		
		//get the percentage of autoscale
		Map<String, String> serviceProviderPreferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
		String autoscalePercentage = serviceProviderPreferences.get("autoscale.percentage");
		
		//get max resource requests for the current service
		Integer maxServiceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemSoFar(service);
		Integer maxServiceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuSoFar(service);
		
		if(maxServiceReservedMem != null && maxServiceReservedCpu != null) {
			
			//compute the new resource requests, for scale up/down
			int newMemRequested;
			if(increaseResources) {
				newMemRequested = (int) (maxServiceReservedMem * (1+Integer.parseInt(autoscalePercentage)/100.0));
			}
			else {
				newMemRequested = (int) (maxServiceReservedMem * (1-Integer.parseInt(autoscalePercentage)/100.0));
			}
			int newCpuRequested;
			if(increaseResources) {
				newCpuRequested = (int) (maxServiceReservedCpu * (1+Integer.parseInt(autoscalePercentage)/100.0));
			}
			else {
				newCpuRequested = (int) (maxServiceReservedCpu * (1-Integer.parseInt(autoscalePercentage)/100.0));
			}

			//get the currentRanking...
			int currentRanking = deploymentOptionsManager.getRankingInDeploymentOptions(service.getId());
			
			//..and the possible new deploymentOption
			RankingData bestRankingData = rankingManager.getBestAvailableRankingForRequestedResources(service, newCpuRequested, newMemRequested);

			if(bestRankingData != null) {
				//we found an option with Ranking; if Ranking <> currentRanking then there is an offload to do
				log.info("found a  with different ranking:  is " + bestRankingData.getRanking() + " while current is " + currentRanking);
				boolean isOffload = bestRankingData.getRanking() != currentRanking;
				
				//if offload is possible, we do it
				if(isOffload) {
					log.info("Action is to offload");
					//within the ranking found, we select the best node, and it cannot be null
					Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(service, bestRankingData.getNodeSet());
					
					message = "Found a " + (bestRankingData.getRanking() < currentRanking ? "better":"worse") + " ranking. Offloading to node {" + bestNode.getName() + "}";
					
					//so we can finally offload
					serviceScheduler.migrate(service, bestNode, newCpuRequested, newMemRequested);
				}
				//else we try to scale up/down within the same ranking (same set of node)
				else {
					log.info("Action is to resize");
					if(increaseResources) {
						message = "Scaled up: increased cpu/mem by " + autoscalePercentage + "%";
					}
					else {
						message = "Scaled down: reduced cpu/mem by " + autoscalePercentage + "%";
					}
					
					serviceScheduler.scaleVertically(service, newCpuRequested, newMemRequested, increaseResources);
				}
			}
		}
		return message;
	}
	
	private String horizontalScaling(Service service, boolean increaseResources) {
		String message = "no nodes or resources available";
		
		//get the replicas
		String replicasString = ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");
		int replicas = replicasString == null ? 1 : Integer.parseInt(replicasString);
		
		//get max resource requests for the current service
		Integer maxServiceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemSoFar(service) * replicas;
		Integer maxServiceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuSoFar(service) * replicas;

		int newMemRequested = maxServiceReservedMem*replicas;
		int newCpuRequested = maxServiceReservedCpu*replicas;
		
		//get the currentRanking...
		int currentRanking = deploymentOptionsManager.getRankingInDeploymentOptions(service.getId());
		
		//..and the possible new deploymentOption
		RankingData bestRankingData = rankingManager.getBestAvailableRankingForRequestedResources(service, newCpuRequested, newMemRequested);
		if(bestRankingData != null) {
			log.info("found a  with different ranking:  is " + bestRankingData.getRanking() + " while current is " + currentRanking);
			boolean isOffload = bestRankingData.getRanking() != currentRanking;
			
			int newReplicas = increaseResources ? replicas+1 : replicas == 1 ? 1 : replicas -1;
			
			//if offload is possible, we do it
			if(isOffload) {
				log.info("Action is to offload and scale");
				//within the ranking found, we select the best node, and it cannot be null
				String bestNodesCSV = ResourceDataReader.printNodeSet(bestRankingData.getNodeSet());
				message = "Found a " + (bestRankingData.getRanking() < currentRanking ? "better":"worse") + " ranking. Offloading to nodes {" + bestNodesCSV + "}";

				//TODO add support to scaling out/in considering better/worse rankings		
				if(increaseResources) {
					message = "Scaled out: increased replicas to " + newReplicas;
				}
				else {
					message = "Scaled in: reduced replicas to " + newReplicas;
				}
				serviceScheduler.scaleHorizontally(service, newReplicas, increaseResources);
			}
			//else we try to scale out/in within the same ranking (same set of node)
			else {
				log.info("Action is to scale");
				if(increaseResources) {
					message = "Scaled out: increased replicas to " + newReplicas;
				}
				else {
					message = "Scaled in: reduced replicas to " + newReplicas;
				}
				
				serviceScheduler.scaleHorizontally(service, newReplicas, increaseResources);
			}
		}
		
		return message;
	}
	
}