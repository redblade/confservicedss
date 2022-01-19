package eu.pledgerproject.confservice.optimisation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;
import eu.pledgerproject.confservice.util.DoubleFormatter;


/**
this optimiser implements "latency_faredge" Optimisation and implements TTODA algorithm 
See TTODAHelper.class for TTODA algorithm details

 */


@Component
public class TTODAOptimiser {
	public static final int LATENCY_CHECK_PERIOD_SEC = 7 * 24 * 60 * 60; //7d 

	public static final int MAX_PERC_THRESHOLD_BEFORE_OFFLOAD_TO_WORSE = 80;
	public static final int MIN_PERC_THRESHOLD_BEFORE_OFFLOAD_TO_BETTER = 60;
	
	private final Logger log = LoggerFactory.getLogger(TTODAOptimiser.class);

	
	private final TTODAHelper ttodaHelper;
	private final ServiceRepository serviceRepository;
	private final ServiceProviderRepository serviceProviderRepository;
	private final BenchmarkManager benchmarkManager;
	private final ServiceScheduler serviceScheduler;
    private final EventRepository eventRepository;

	//Nodes are organised in multiple Sets. This is because the optimisation works offloading among "infrastructures" which are considered as a set of Nodes. 
	//One service can belong to multiple Sets, which means it can be offloaded to multiple "infrastructures".
	//So, the Groups are built as all the possible Node sets for all the services. Then, Group by Group, the optimisation decides where a service could go

	public TTODAOptimiser(TTODAHelper ttodaHelper, ServiceRepository serviceRepository, ServiceProviderRepository serviceProviderRepository, BenchmarkManager benchmarkManager, ServiceScheduler serviceScheduler, EventRepository eventRepository) {
		this.ttodaHelper = ttodaHelper;
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
		event.setCategory("TTODAOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	
	private void saveErrorEvent(ServiceProvider serviceProvider, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(serviceProvider);
		event.setDetails(msg);
		event.setCategory("TTODAOptimiser");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	
	@Scheduled(cron = "30 */1 * * * *")
	public void doOptimise() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED) {
			log.info("TTODAOptimiser started");
	
			//the optimisation is done by SP. We assume the NodeGroup are mostly separated, apart from the cloud which is the worse option possible
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				
				//by SP: the list of Services to optimise and the Node set where they could be allocated
				//HERE we assume a SP DOES one TTODA on ALL the services which are labelled with TTODA.
				//Nodes preferences in ServiceConstraints are considered for ALL the services (in AND)
				List<Service> serviceList = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.latency_faredge.name());
				doOptimise(serviceProvider, serviceList);
			}
		}
	}
	
	
	
	private void doOptimise(ServiceProvider serviceProvider, List<Service> serviceList) {
		
		List<ServiceData> orderedServiceDataList = ttodaHelper.getNewOrderedServiceDataList(serviceProvider, serviceList);
		if(orderedServiceDataList != null) {
			List<NodeGroup> nodeGroupList = ttodaHelper.getNodeGroupListForSPWithTotalCapacityAndFilterByServiceContraints(serviceProvider, serviceList);
					
			//the allocation plan, based on the optimised ServiceData list
			Map<ServiceData, NodeGroup> serviceOptimisedAllocationPlan = TTODAHelper.getServiceOptimisedAllocationPlan(orderedServiceDataList, nodeGroupList);
			if(serviceOptimisedAllocationPlan == null) {
				log.error("TTODAOptimiser error: not enough resources on the worse option(cloud)");
				saveErrorEvent(serviceProvider, "Not enough resources on the worse option(cloud)");
			}
			else { 
				if(serviceOptimisedAllocationPlan.keySet().size() == 0) {
					log.info("TTODAOptimiser: no offloadings needed");
				}
				else {
					for(ServiceData serviceData : serviceOptimisedAllocationPlan.keySet()) {
						log.info("TTODAOptimiser: Service " + serviceData.service.getName() + " has TTODA score " + DoubleFormatter.format(serviceData.score));

						NodeGroup nodeGroup = serviceOptimisedAllocationPlan.get(serviceData);
						log.info("TTODAOptimiser: offloading Service " + serviceData.service.getName() + " on nodeGroup " + nodeGroup.getNodeCSV());
						Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(serviceData.service, nodeGroup.nodes);
	
						serviceScheduler.migrate(serviceData.service, bestNode, serviceData.requestCpuMillicore, serviceData.requestMemoryMB);
						saveInfoEvent(serviceData.service, "Service " + serviceData.service.getName() + " migrated to node " + bestNode);
					}
				}
			}
		}
	}
	
	
}
