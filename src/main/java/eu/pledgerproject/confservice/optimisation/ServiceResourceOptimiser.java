package eu.pledgerproject.confservice.optimisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

@Component
public class ServiceResourceOptimiser {
    private final Logger log = LoggerFactory.getLogger(ServiceResourceOptimiser.class);
    
    public static final String RESOURCE_USAGE_CATEGORY = "resource-used";
    public static int SCORE_THRESHOLD = 100;
    
    private final DeploymentOptionsManager deploymentOptionsManager;
    private final ServiceScheduler serviceScheduler;
    private final RankingManager rankingManager;
    private final BenchmarkManager benchmarkManager;
    
    
    public ServiceResourceOptimiser(DeploymentOptionsManager deploymentOptionsManager, ServiceScheduler serviceScheduler, RankingManager rankingManager, BenchmarkManager benchmarkManager) {
        this.deploymentOptionsManager = deploymentOptionsManager;
    	this.serviceScheduler = serviceScheduler;
    	this.rankingManager = rankingManager;
    	this.benchmarkManager = benchmarkManager;
    }
    
	public String optimise(Service service, boolean increaseResources) {
		String message = "Nothing to do, neither horizontal or vertical scaling is configured";
		
		String scaling = ConverterJSON.convertToMap(service.getInitialConfiguration()).get(Constants.SCALING); 
		if(Constants.SCALING_VERTICAL.equals(scaling)){
			message = verticalScaling(service, increaseResources);
		}
		else if(Constants.SCALING_HORIZONTAL.equals(scaling)){
			message = horizontalScaling(service, increaseResources);
		}	
		
		return message;
	}
	
	private String verticalScaling(Service service, boolean increaseResources) {
		//if nothing can be done (no resources), this message will be returned
		String message = "no nodes or resources available";
		
		//get the percentage of autoscale
		String autoscalePercentageAdd = ConverterJSON.getProperty(service.getApp().getServiceProvider().getPreferences(), "autoscale.percentage");
		int autoscalePercentageAddInt = Integer.parseInt(autoscalePercentageAdd.length() == 0 ? Constants.DEFAULT_AUTOSCALE_PERCENTAGE : autoscalePercentageAdd);
		String autoscalePercentageDecrease = ConverterJSON.getProperty(service.getApp().getServiceProvider().getPreferences(), "autoscale.percentage.decrease");
		int autoscalePercentageDecreaseInt = autoscalePercentageDecrease.length() == 0 ? autoscalePercentageAddInt : Integer.parseInt(autoscalePercentageDecrease);

		//get max resource requests for the current service
		Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
		Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);
		
		if(cpuRequest != null && memRequest != null) {
			Integer minMemRequest = ResourceDataReader.getServiceMinMemRequest(service);
			Integer minCpuRequest = ResourceDataReader.getServiceMinCpuRequest(service);
			Integer maxMemRequest = ResourceDataReader.getServiceMaxMemRequest(service);
			Integer maxCpuRequest = ResourceDataReader.getServiceMaxCpuRequest(service);

			//compute the new resource requests, for scale up/down
			int newCpuRequested;
			if(increaseResources) {
				newCpuRequested = (int) (cpuRequest * (100.0+autoscalePercentageAddInt)/100.0);
				newCpuRequested = Math.min(newCpuRequested, maxCpuRequest);
			}
			else {
				newCpuRequested = (int) (cpuRequest * (100.0-autoscalePercentageDecreaseInt)/100.0);
				newCpuRequested = Math.max(newCpuRequested, minCpuRequest);
			}
			int newMemRequested;
			if(increaseResources) {
				newMemRequested = (int) (memRequest * (100.0+autoscalePercentageAddInt)/100.0);
				newMemRequested = Math.min(newMemRequested, maxMemRequest);
			}
			else {
				newMemRequested = (int) (memRequest * (100.0-autoscalePercentageDecreaseInt)/100.0);
				newMemRequested = Math.max(newMemRequested, minMemRequest);
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
						message = "Scaled up: increased by " + autoscalePercentageAddInt + "% cpu,mem: " + newCpuRequested + "," + newMemRequested;
					}
					else if(newCpuRequested == minCpuRequest && newMemRequested == minMemRequest){
						message = "Scaled down to min resources: cpu,mem: " + newCpuRequested + "," + newMemRequested;
					}
					else {
						message = "Scaled down: reduced by " + autoscalePercentageDecreaseInt + "% cpu,mem: " + newCpuRequested + "," + newMemRequested;
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
		int replicas = ResourceDataReader.getServiceRuntimeReplicas(service);
		
		//get max resource requests for the current service
		Integer cpuRequest = ResourceDataReader.getServiceRuntimeCpuRequest(service);
		Integer memRequest = ResourceDataReader.getServiceRuntimeMemRequest(service);

		int newCpuRequested = cpuRequest*replicas;
		int newMemRequested = memRequest*replicas;
		
		//get the currentRanking...
		int currentRanking = deploymentOptionsManager.getRankingInDeploymentOptions(service.getId());
		
		//..and the possible new deploymentOption
		RankingData bestRankingData = rankingManager.getBestAvailableRankingForRequestedResources(service, newCpuRequested, newMemRequested);
		if(bestRankingData != null) {
			log.info("found a  with different ranking:  is " + bestRankingData.getRanking() + " while current is " + currentRanking);
			boolean isOffload = bestRankingData.getRanking() != currentRanking;
			
			int newReplicas;
			if(increaseResources) {
				int maxReplicas = ResourceDataReader.getServiceMaxReplicas(service);
				newReplicas = replicas < maxReplicas ? replicas+1 : maxReplicas;
			}
			else {
				newReplicas = replicas == 1 ? 1 : replicas -1;
			}
			
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
