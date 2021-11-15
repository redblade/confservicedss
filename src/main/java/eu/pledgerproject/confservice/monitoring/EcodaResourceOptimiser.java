package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;

/*

This optimiser combines ECODA with resource optimisation and is activated by ServiceOptimisation resources_latency
See EcodaOptimiser for ECODA algorithm
*/


@Component
public class EcodaResourceOptimiser {
	public static final String DEFAULT_AUTOSCALE_PERCENTAGE = "10";

	private final Logger log = LoggerFactory.getLogger(EcodaResourceOptimiser.class);
	
	private final ResourceDataReader resourceDataReader;
	private final EcodaHelper ecodaHelper;
	private final ServiceProviderRepository serviceProviderRepository;
	private final SlaViolationRepository slaViolationRepository;
	private final BenchmarkManager benchmarkManager;
	private final ServiceScheduler serviceScheduler;
	private final ServiceRepository serviceRepository;
	private final EventRepository eventRepository;
    
	//Nodes are organised in multiple Sets. This is because the optimisation works offloading among "infrastructures" which are considered as a set of Nodes. 
	//One service can belong to multiple Sets, which means it can be offloaded to multiple "infrastructures".
	//So, the Groups are built as all the possible Node sets for all the services. Then, Group by Group, the optimisation decides where a service could go

	public EcodaResourceOptimiser(ResourceDataReader resourceDataReader, EcodaHelper ecodaHelper, ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, BenchmarkManager benchmarkManager, ServiceScheduler serviceScheduler, ServiceRepository serviceRepository, EventRepository eventRepository) {
		this.resourceDataReader = resourceDataReader;
		this.ecodaHelper = ecodaHelper;
		this.serviceProviderRepository = serviceProviderRepository;
		this.slaViolationRepository = slaViolationRepository;
		this.benchmarkManager = benchmarkManager;
		this.serviceScheduler = serviceScheduler;
		this.serviceRepository = serviceRepository;
		this.eventRepository = eventRepository;
	}
	
	private void saveInfoEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("EcodaResourceOptimiser");
		event.severity(Event.INFO);
		eventRepository.save(event);
	}
	private void saveErrorEvent(ServiceProvider serviceProvider, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(serviceProvider);
		event.setDetails(msg);
		event.setCategory("EcodaResourceOptimiser");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	//Each SP has his own set of Nodes to work on, named "NodeGroup"
	//Each group has its resource capability
	@Scheduled(cron = "0 */1 * * * *")
	public void doOptimise() {
		log.info("EcodaResourceOptimiser started");
		
		//the optimisation is done by SP. We assume the NodeGroup are mostly separated, apart from the cloud which is the worse option possible
		for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
			Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
			int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
			
			Instant stopTime = Instant.now();
			Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
			
			for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SlaViolationStatus.elab_add_more_resources.name(), ServiceOptimisationType.resources_latency.name(), startTime)) {
				slaViolation.setStatus(SlaViolationStatus.closed_critical.toString());
				slaViolationRepository.save(slaViolation);
			}
			for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SlaViolationStatus.elab_keep_same_resources.name(), ServiceOptimisationType.resources_latency.name(), startTime)) {
				slaViolation.setStatus(SlaViolationStatus.closed_not_critical.toString());
				slaViolationRepository.save(slaViolation);
			}
			
			//then we optimise the services
			List<Service> serviceList = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.resources_latency.name());
			doOptimise(serviceProvider, serviceList);
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
				int[] cpuMemServiceResourcePlan = getServiceResourcePlan(service);
				
				ServiceData serviceData = new ServiceData(service, cpuMemServiceResourcePlan[0], cpuMemServiceResourcePlan[1]);
				serviceData.currentNode = resourceDataReader.getCurrentNode(service);
				serviceData.score = ecodaHelper.getOptimisationScore(serviceData, nodeSetOnEdge, totalCpu4SP, totalMem4SP);
				log.info("Service " + service.getName() + " has ECODA score " + serviceData.score);
				serviceDataList.add(serviceData);
 			}
			Collections.sort(serviceDataList);
			
			//the allocation plan, based on the optimised ServiceData list
			Map<ServiceData, NodeGroup> serviceOptimisedAllocationPlan = EcodaHelper.getServiceOptimisedAllocationPlan(serviceDataList, nodeGroupList);
			if(serviceOptimisedAllocationPlan == null) {
				log.error("EcodaResourceOptimiser error: not enough resources on the worse option(cloud)");
				saveErrorEvent(serviceProvider, "EcodaResourceOptimiser error: not enough resources on the worse option(cloud)");
			}
			else { 
				if(serviceOptimisedAllocationPlan.keySet().size() == 0) {
					log.info("EcodaResourceOptimiser: no offloadings needed");
				}
				else {
					for(ServiceData serviceData : serviceOptimisedAllocationPlan.keySet()) {
						NodeGroup nodeGroup = serviceOptimisedAllocationPlan.get(serviceData);
						log.info("EcodaResourceOptimiser: offloading Service " + serviceData.service.getName() + " on nodeGroup " + nodeGroup.getNodeCSV());
						Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(serviceData.service, nodeGroup.nodes);
						serviceScheduler.migrate(serviceData.service, bestNode, serviceData.requestCpuMillicore, serviceData.requestMemoryMB);
					}
				}
			}
		}
	}
	
	private int[] getServiceResourcePlan(Service service) {
		int[] result = new int[] {
			ResourceDataReader.getServiceCpuRequest(service),
			ResourceDataReader.getServiceMemRequest(service)
		};
		
		//get the percentage of autoscale
		String autoscalePercentage = ConverterJSON.getProperty(service.getApp().getServiceProvider().getPreferences(), "autoscale.percentage");
		int autoscalePercentageInt = Integer.parseInt(autoscalePercentage.length() == 0 ? DEFAULT_AUTOSCALE_PERCENTAGE : autoscalePercentage);

		//get the monitoringSlaViolationPeriodSec 
		Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
		int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));

		//if there have been violations then we need to increase resources
		Instant timestampCritical = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		List<SlaViolation> slaViolationListCritical = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SlaViolationStatus.closed_critical.name(), ServiceOptimisationType.resources_latency.name(), timestampCritical);
		if(slaViolationListCritical.size() > 0) {
			result[0] = (int) (result[0] * (100.0 + autoscalePercentageInt)/100.0);
			result[1] = (int) (result[1] * (100.0 + autoscalePercentageInt)/100.0);
			saveInfoEvent(service, "Increased resources for service " + service.getName() + " - cpu/mem: " + result[0] + "/" + result[1]);
		}
		//if, on the other hand, the service has been running and no violations have been received so far, then we need to decrease resources
		else {
			Instant timestampSteady = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec * SteadyServiceOptimiser.STEADY_PERIOD_SEC_FACTOR);
			List<Service> steadyServiceList = serviceRepository.findSteadyServiceListByServiceProviderServiceOptimisationSinceTimestamp(service.getApp().getServiceProvider(), ServiceOptimisationType.resources_latency.name(), timestampSteady);
			if(steadyServiceList.size() > 0) {
				int minCpuRequest = ResourceDataReader.getServiceMinCpuRequest(service);
				int minMemRequest = ResourceDataReader.getServiceMinMemRequest(service);
				
				int cpuRequestTemp = (int) (result[0] * (100.0 - autoscalePercentageInt)/100.0);
				int memRequestTemp = (int) (result[1] * (100.0 - autoscalePercentageInt)/100.0); 
				result[0] = cpuRequestTemp > minCpuRequest ? cpuRequestTemp : minCpuRequest;
				result[1] = memRequestTemp > minMemRequest ? memRequestTemp : minMemRequest;
				saveInfoEvent(service, "Decreased resources for service " + service.getName() + " - cpu/mem: " + result[0] + "/" + result[1]);
			}
		}

		return result;
	}
}
