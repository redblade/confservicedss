package eu.pledgerproject.confservice.optimisation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.scheduler.ServiceScheduler;
import eu.pledgerproject.confservice.util.DoubleFormatter;

/**
this optimiser implements "resources_latency" Optimisation which uses ECODA algorithm combined with "resources" optimisation
See ECODAHelper.class for ECODA algorithm details

 */



@Component
public class ECODAResourceOptimiser {
	public static final String DEFAULT_AUTOSCALE_PERCENTAGE = "10";

	private final Logger log = LoggerFactory.getLogger(ECODAResourceOptimiser.class);
	
	private final ResourceDataReader resourceDataReader;
	private final ECODAHelper ecodaHelper;
	private final ServiceProviderRepository serviceProviderRepository;
	private final SlaViolationRepository slaViolationRepository;
	private final BenchmarkManager benchmarkManager;
	private final ServiceScheduler serviceScheduler;
	private final ServiceRepository serviceRepository;
	private final EventRepository eventRepository;
    
	//Nodes are organised in multiple Sets. This is because the optimisation works offloading among "infrastructures" which are considered as a set of Nodes. 
	//One service can belong to multiple Sets, which means it can be offloaded to multiple "infrastructures".
	//So, the Groups are built as all the possible Node sets for all the services. Then, Group by Group, the optimisation decides where a service could go

	public ECODAResourceOptimiser(ResourceDataReader resourceDataReader, ECODAHelper ecodaHelper, ServiceProviderRepository serviceProviderRepository, SlaViolationRepository slaViolationRepository, BenchmarkManager benchmarkManager, ServiceScheduler serviceScheduler, ServiceRepository serviceRepository, EventRepository eventRepository) {
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
	@Scheduled(cron = "30 */1 * * * *")
	public void doOptimise() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("ECODAResourceOptimiser started");
			
			//the optimisation is done by SP. We assume the NodeGroup are mostly separated, apart from the cloud which is the worse option possible
			for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
				Map<String, String> preferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
				int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
				
				Instant stopTime = Instant.now();
				Instant startTime = stopTime.minus(monitoringSlaViolationPeriodSec, ChronoUnit.SECONDS);
				
				for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SLAViolationStatus.elab_resources_needed.name(), ServiceOptimisationType.resources_latency.name(), startTime)) {
					slaViolation.setStatus(SLAViolationStatus.closed_critical.toString());
					slaViolationRepository.save(slaViolation);
				}
				for(SlaViolation slaViolation : slaViolationRepository.findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(serviceProvider.getName(), SLAViolationStatus.elab_no_action_needed.name(), ServiceOptimisationType.resources_latency.name(), startTime)) {
					slaViolation.setStatus(SLAViolationStatus.closed_not_critical.toString());
					slaViolationRepository.save(slaViolation);
				}
				
				//then we optimise the services
				//HERE we assume a SP DOES one ECODA on ALL the services which are labelled with ECODA.
				//Nodes preferences in ServiceConstraints are considered for ALL the services (in AND)
				List<Service> serviceList = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.resources_latency.name());
				doOptimise(serviceProvider, serviceList);
			}
		}
	}
	
	public List<ServiceData> getNewOrderedServiceDataList(ServiceProvider serviceProvider, List<Service> serviceList, boolean saveServiceResourcePlanInEvent) {
		List<NodeGroup> nodeGroupList = ecodaHelper.getNodeGroupListForSPWithTotalCapacityAndFilterByServiceContraints(serviceProvider, serviceList);

		int totalEdgeCpu4SP = 0;
		int totalEdgeMem4SP = 0;
		
		NodeGroup nodeSetOnFarEdge = nodeGroupList.get(0);
		if(nodeSetOnFarEdge.location.equals(NodeGroup.NODE_EDGE)) {
			Integer[] total4SP = ecodaHelper.getTotalCapacityForSPOnNodeSet(serviceProvider, nodeSetOnFarEdge.nodes);
			totalEdgeCpu4SP += total4SP[0];
			totalEdgeMem4SP += total4SP[1];
		}
			
		NodeGroup nodeSetOnEdge = nodeGroupList.get(0);
		if(nodeSetOnEdge.location.equals(NodeGroup.NODE_EDGE) && totalEdgeCpu4SP > 0 && totalEdgeMem4SP > 0) {
			
			List<ServiceData> serviceDataList = new ArrayList<ServiceData>();
			for(Service service: serviceList) {

				//Here we want to desired resource amount, not the actual request! no SLAViolation=>reduce, SLAViolation=>increase
				ServiceResourcePlan serviceResourcePlan = getServiceResourcePlan(service);
				if(serviceResourcePlan != null) {
					if(saveServiceResourcePlanInEvent) {
						saveInfoEvent(service, serviceResourcePlan.msg);
					}
					int requestCpuMillicore = serviceResourcePlan.cpu;
					int requestMemoryMB = serviceResourcePlan.mem;
				
					ServiceData serviceData = new ServiceData(service, requestCpuMillicore, requestMemoryMB);
					serviceData.currentNode = resourceDataReader.getCurrentNode(service);
					serviceData.score = ecodaHelper.getOptimisationScore(serviceData, nodeSetOnEdge.nodes, totalEdgeCpu4SP, totalEdgeMem4SP);
					serviceDataList.add(serviceData);
				}
 			}
			Collections.sort(serviceDataList);
			return serviceDataList;
		}
		return null;
	}
	
	private void doOptimise(ServiceProvider serviceProvider, List<Service> serviceList) {
		if(serviceList.size() > 0) {
			List<NodeGroup> nodeGroupList = ecodaHelper.getNodeGroupListForSPWithTotalCapacityAndFilterByServiceContraints(serviceProvider, serviceList);
			
			List<ServiceData> serviceDataList = getNewOrderedServiceDataList(serviceProvider, serviceList, true);
			if(serviceDataList != null) {

				//the allocation plan, based on the optimised ServiceData list
				Map<ServiceData, NodeGroup> serviceOptimisedAllocationPlan = ECODAHelper.getServiceOptimisedAllocationPlan(serviceDataList, nodeGroupList);
				if(serviceOptimisedAllocationPlan == null) {
					log.error("ECODAResourceOptimiser error: not enough resources on the worse option(cloud)");
					saveErrorEvent(serviceProvider, "Not enough resources on the worse option(cloud)");
				}
				else { 
					if(serviceOptimisedAllocationPlan.keySet().size() == 0) {
						log.info("ECODAResourceOptimiser: no offloadings needed");
					}
					else {
						for(ServiceData serviceData : serviceOptimisedAllocationPlan.keySet()) {
							log.info("ECODAOptimiser: Service " + serviceData.service.getName() + " has ECODA score " + DoubleFormatter.format(serviceData.score));

							NodeGroup nodeGroup = serviceOptimisedAllocationPlan.get(serviceData);
							log.info("ECODAResourceOptimiser: offloading Service " + serviceData.service.getName() + " on nodeGroup " + nodeGroup.getNodeCSV());
							Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(serviceData.service, nodeGroup.nodes);
							
							serviceScheduler.migrate(serviceData.service, bestNode, serviceData.requestCpuMillicore, serviceData.requestMemoryMB);
							saveInfoEvent(serviceData.service, "Service " + serviceData.service.getName() + " migrated to node " + bestNode);
						}
					}
				}
			}
		}
	}
	
	private ServiceResourcePlan getServiceResourcePlan(Service service) {
		int[] result = new int[] {
			ResourceDataReader.getServiceRuntimeCpuRequest(service),
			ResourceDataReader.getServiceRuntimeMemRequest(service)
		};
		
		//get the percentage of autoscale
		String autoscalePercentage = ConverterJSON.getProperty(service.getApp().getServiceProvider().getPreferences(), "autoscale.percentage");
		int autoscalePercentageInt = Integer.parseInt(autoscalePercentage.length() == 0 ? DEFAULT_AUTOSCALE_PERCENTAGE : autoscalePercentage);

		//get the monitoringSlaViolationPeriodSec 
		Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
		int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));

		//if there have been violations then we need to increase resources
		Instant timestampCritical = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		List<SlaViolation> slaViolationListCritical = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.closed_critical.name(), ServiceOptimisationType.resources_latency.name(), timestampCritical);
		if(slaViolationListCritical.size() > 0) {
			result[0] = (int) (result[0] * (100.0 + autoscalePercentageInt)/100.0);
			result[1] = (int) (result[1] * (100.0 + autoscalePercentageInt)/100.0);
			return new ServiceResourcePlan(result, "Increased resources for service " + service.getName() + " ### cpu/mem: " + result[0] + "/" + result[1]);
		}
		//if, on the other hand, the service has been running and no violations have been received so far, then we need to decrease resources
		else {
			Instant timestampSteady = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);

			List<SlaViolation> slaViolationCriticalList = slaViolationRepository.findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(service, SLAViolationStatus.closed_critical.name(), ServiceOptimisationType.resources_latency.name(), timestampSteady);
			if(service.getLastChangedStatus().isBefore(timestampSteady) && slaViolationCriticalList.size() == 0) {
				int minCpuRequest = ResourceDataReader.getServiceMinCpuRequest(service);
				int minMemRequest = ResourceDataReader.getServiceMinMemRequest(service);
				
				int cpuRequestTemp = (int) (result[0] * (100.0 - autoscalePercentageInt)/100.0);
				int memRequestTemp = (int) (result[1] * (100.0 - autoscalePercentageInt)/100.0); 
				result[0] = cpuRequestTemp > minCpuRequest ? cpuRequestTemp : minCpuRequest;
				result[1] = memRequestTemp > minMemRequest ? memRequestTemp : minMemRequest;
				if(result[0] != minCpuRequest || result[1] != minMemRequest) {
					return new ServiceResourcePlan(result, "Decreased resources for service " + service.getName() + " ### cpu/mem: " + result[0] + "/" + result[1]);
				}
				else {
					return new ServiceResourcePlan(result, "Min resources for service " + service.getName() + " ### cpu/mem: " + result[0] + "/" + result[1]);
				}
			}
		}

		return null;
	}
}
