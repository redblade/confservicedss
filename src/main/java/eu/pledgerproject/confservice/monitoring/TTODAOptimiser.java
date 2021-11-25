package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

/*

This optimiser combines TTODA with resource optimisation and is activated by ServiceOptimisation latency_faredge

R & M are percentage or 0..1 

rN = percentage of CPU requested wrt total capacity (expressed as 0..1)
mN = percentage of MEM requested wrt total capacity (expressed as 0..1)
tN = rN + mN

wN = tN / service priority

instantiationN = time (ms) to load service image (0 if images are pre-loaded)
latencyN = time (ms) to communicate from edge to cloud nodes
loadingN = time (ms) to have a service ready

#for CPU/MEM constrained services

from function (26) in ECODA in https://www.techrxiv.org/articles/preprint/An_Optimization_Framework_for_Edge-to-Cloud_Offloading_of_Kubernetes_Pods_in_V2X_Scenarios/16725643/1

for all service[i] compute score(service[i]) = wN * (instantiationN + (latencyN + loadingN) * (1 - 2/tN)	

all infrastructures are organized as "node groups", ordered by position: far-edge(1), edge(2), finally cloud(3).
for all nodegroups, check if all services fit: 
- if (resources > capability max threshold), use score(i) with Max score to choose which service to offload to the node_group with worse position (eg., from 1->2)
- else if (resources < capability min threshold) use (score(i) with Min score to choose which service to offloaded to the node_group with better position (eg., from 2->1)


"capability max threshold" and "capability min threshold" are used to avoid too many back and forth. For example, 70% and 30%

Implementation details:
service deployment options are ignored, so nodes are organised in nodegroups with far-edge, edge, cloud (possibly more)
services are sorted using the score: when the thresholds are not met, those with higher priority are kept on the far-edge (according to the thresholds) and so on

*/


@Component
public class TTODAOptimiser {
	public static final int LATENCY_CHECK_PERIOD_SEC = 7 * 24 * 60 * 60; //7d 

	public static final int MAX_PERC_THRESHOLD_BEFORE_OFFLOAD_TO_WORSE = 80;
	public static final int MIN_PERC_THRESHOLD_BEFORE_OFFLOAD_TO_BETTER = 60;
	
	private final Logger log = LoggerFactory.getLogger(TTODAOptimiser.class);

	
	private final ResourceDataReader resourceDataReader;
	private final EcodaHelper ecodaHelper;
	private final ServiceRepository serviceRepository;
	private final ServiceProviderRepository serviceProviderRepository;
	private final BenchmarkManager benchmarkManager;
	private final ServiceScheduler serviceScheduler;
    private final EventRepository eventRepository;

	//Nodes are organised in multiple Sets. This is because the optimisation works offloading among "infrastructures" which are considered as a set of Nodes. 
	//One service can belong to multiple Sets, which means it can be offloaded to multiple "infrastructures".
	//So, the Groups are built as all the possible Node sets for all the services. Then, Group by Group, the optimisation decides where a service could go

	public TTODAOptimiser(ResourceDataReader resourceDataReader, EcodaHelper ecodaHelper, ServiceRepository serviceRepository, ServiceProviderRepository serviceProviderRepository, BenchmarkManager benchmarkManager, ServiceScheduler serviceScheduler, EventRepository eventRepository) {
		this.resourceDataReader = resourceDataReader;
		this.ecodaHelper = ecodaHelper;
		this.serviceRepository = serviceRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.benchmarkManager = benchmarkManager;
		this.serviceScheduler = serviceScheduler;
		this.eventRepository = eventRepository;
	}
	
	private void saveInfoEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("EcodaOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
	private void saveErrorEvent(ServiceProvider serviceProvider, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(serviceProvider);
		event.setDetails(msg);
		event.setCategory("EcodaOptimiser");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	
	@Scheduled(cron = "30 */1 * * * *")
	public void doOptimise() {
		if(!ControlFlag.READ_ONLY_MODE_ENABLED){
			log.info("EcodaOptimiser started");
	
			//the optimisation is done by SP. We assume the NodeGroup are mostly separated, apart from the cloud which is the worse option possible
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				
				//by SP: the list of Services to optimise and the Node set where they could be allocated
				List<Service> serviceList = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.latency.name());
				doOptimise(serviceProvider, serviceList);
			}
		}
	}
	
	private void doOptimise(ServiceProvider serviceProvider, List<Service> serviceList) {
		List<NodeGroup> nodeGroupList = ecodaHelper.getNodeGroupListForSPWithTotalCapacity(serviceProvider);
		int totalCpu4SP = 0;
		int totalMem4SP = 0;
		
		for(NodeGroup nodeGroup : nodeGroupList) {
			Integer[] total4SP = ecodaHelper.getTotalCapacityForSPOnNodeSet(serviceProvider, nodeGroup.nodes);
			totalCpu4SP += total4SP[0];
			totalMem4SP += total4SP[1];
		}
			
		if(nodeGroupList.size() > 0 && totalCpu4SP > 0 && totalMem4SP > 0) {
			Set<Node> nodeSetOnEdge = nodeGroupList.get(0).nodes;
			
			List<ServiceData> serviceDataList = new ArrayList<ServiceData>();
			for(Service service: serviceList) {

				//Here we want to desired resource amount, not the actual request! no SLAViolation=>reduce, SLAViolation=>increase
				int requestCpuMillicore = ResourceDataReader.getServiceRuntimeCpuRequest(service);
				int requestMemoryMB = ResourceDataReader.getServiceRuntimeMemRequest(service);
				
				ServiceData serviceData = new ServiceData(service, requestCpuMillicore, requestMemoryMB);
				serviceData.currentNode = resourceDataReader.getCurrentNode(service);
				serviceData.score = ecodaHelper.getOptimisationScore(serviceData, nodeSetOnEdge, totalCpu4SP, totalMem4SP);
				log.info("Service " + service.getName() + " has ECODA score " + serviceData.score);
				serviceDataList.add(serviceData);
 			}
			Collections.sort(serviceDataList);
			
			//the allocation plan, based on the optimised ServiceData list
			Map<ServiceData, NodeGroup> serviceOptimisedAllocationPlan = EcodaHelper.getServiceOptimisedAllocationPlan(serviceDataList, nodeGroupList);
			if(serviceOptimisedAllocationPlan == null) {
				log.error("EcodaOptimiser error: not enough resources on the worse option(cloud)");
				saveErrorEvent(serviceProvider, "Not enough resources on the worse option(cloud)");
			}
			else { 
				if(serviceOptimisedAllocationPlan.keySet().size() == 0) {
					log.info("EcodaOptimiser: no offloadings needed");
				}
				else {
					for(ServiceData serviceData : serviceOptimisedAllocationPlan.keySet()) {
						NodeGroup nodeGroup = serviceOptimisedAllocationPlan.get(serviceData);
						log.info("EcodaResourceOptimiser: offloading Service " + serviceData.service.getName() + " on nodeGroup " + nodeGroup.getNodeCSV());
						Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(serviceData.service, nodeGroup.nodes);

						serviceScheduler.migrate(serviceData.service, bestNode, serviceData.requestCpuMillicore, serviceData.requestMemoryMB);
						saveInfoEvent(serviceData.service, "Service " + serviceData.service.getName() + " migrated to node " + bestNode);
					}
				}
			}
		}
	}
	
	
}
