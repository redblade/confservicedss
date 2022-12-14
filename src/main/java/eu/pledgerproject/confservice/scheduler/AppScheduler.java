package eu.pledgerproject.confservice.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.optimisation.ECODAHelper;
import eu.pledgerproject.confservice.optimisation.NodeGroup;
import eu.pledgerproject.confservice.optimisation.Constants;
import eu.pledgerproject.confservice.optimisation.RankingData;
import eu.pledgerproject.confservice.optimisation.RankingManager;
import eu.pledgerproject.confservice.optimisation.ServiceOptimisationType;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class AppScheduler {
	private final Logger log = LoggerFactory.getLogger(AppScheduler.class);

	private final ServiceScheduler serviceScheduler;
	private final AppRepository appRepository;
	private final ServiceRepository serviceRepository;
	private final RankingManager rankingManager;
	private final ECODAHelper ecodaHelper;
	private final BenchmarkManager benchmarkManager;
	private final EventRepository eventRepository;

	public AppScheduler(ServiceScheduler serviceScheduler, AppRepository appRepository, ServiceRepository serviceRepository, RankingManager rankingManager, ECODAHelper ecodaHelper, BenchmarkManager benchmarkManager, EventRepository eventRepository) {
		this.serviceScheduler = serviceScheduler;
		this.appRepository = appRepository;
		this.serviceRepository = serviceRepository;
		this.rankingManager = rankingManager;
		this.ecodaHelper = ecodaHelper;
		this.benchmarkManager = benchmarkManager;
		this.eventRepository = eventRepository;
	}
	
	private void saveWarningEvent(App app, String msg) {
    	if(log.isWarnEnabled()) {
			Event event = new Event();
			event.setServiceProvider(app.getServiceProvider());
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("AppScheduler");
			event.severity(Event.WARNING);
			eventRepository.save(event);
    	}
	}

	@Async
	public void start(App app) {
		log.info("App started " + app.getName());

		boolean started = false;
		
		List<Service> serviceList = serviceRepository.findAllByAppId(app.getId());
		for(Service service : serviceList) {
			
			int initialRequestCpu = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(Constants.INITIAL_CPU_MILLICORE));
			int initialRequestMem = Integer.parseInt(ConverterJSON.convertToMap(service.getInitialConfiguration()).get(Constants.INITIAL_MEMORY_MB));

			if(service.getServiceOptimisation() != null &&
					(
						ServiceOptimisationType.latency.name().equals(service.getServiceOptimisation().getOptimisation())
							||
						ServiceOptimisationType.resources_latency.name().equals(service.getServiceOptimisation().getOptimisation())
					)
			) {
				List<NodeGroup> nodeGroupList = ecodaHelper.getNodeGroupListForSPWithRemainingCapacityThatCanHostRequestsAndFilterByServiceContraints(service.getApp().getServiceProvider(), service, initialRequestCpu, initialRequestMem);
				if(nodeGroupList.size() > 0) {
					Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(service, nodeGroupList.get(0).nodes);
					
					started = serviceScheduler.start(service, bestNode, initialRequestCpu, initialRequestMem);
				}
				else {
					started = false;
					String warningMessage = "App " + app.getName() + " has no Nodes configured, please add ServiceConstrants and verify the DeploymentOptions"; 
					saveWarningEvent(app, warningMessage);
					log.warn("AppScheduler.start warning: " + warningMessage);
				}
			}
			else {
				RankingData rankingData = rankingManager.getBestAvailableRankingForRequestedResources(service, initialRequestCpu, initialRequestMem);
				if(rankingData.getNodeSet().size() > 0) {
					Node bestNode = benchmarkManager.getBestNodeUsingBenchmark(service, rankingData.getNodeSet());
					
					started = serviceScheduler.start(service, bestNode, initialRequestCpu, initialRequestMem);
				}
				else {
					started = false;
					String warningMessage = "App " + app.getName() + " has no DeploymentOptions found, or selected options have not enough resources to host it"; 
					saveWarningEvent(app, warningMessage);
					log.warn("AppScheduler.start warning: " + warningMessage);
				}
			}
			
		}
		if(started) {
			app.setStatus(ExecStatus.RUNNING);
		}
		else {
			app.setStatus(ExecStatus.STOPPED);
		}
		appRepository.save(app);
		
	}

	@Async
	public void stop(App app) {
		log.info("App stopped " + app.getName());

		List<Service> serviceList = serviceRepository.findAllByAppId(app.getId());
		for(Service service : serviceList) {
			serviceScheduler.stop(service);
		}
		app.setStatus(ExecStatus.STOPPED);
		appRepository.save(app);
	}
	
	public void forceStop(App app) {
		log.info("App force stopped " + app.getName());

		List<Service> serviceList = serviceRepository.findAllByAppId(app.getId());
		for(Service service : serviceList) {
			service.setStatus(ExecStatus.STOPPED);
			serviceRepository.save(service);
		}
		app.setStatus(ExecStatus.STOPPED);
		appRepository.save(app);
	}

}
