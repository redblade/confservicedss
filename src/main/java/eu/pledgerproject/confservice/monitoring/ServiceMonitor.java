package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.domain.enumeration.DeployType;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.scheduler.TokenKubernetes;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;

@Component
public class ServiceMonitor {

	private final Logger log = LoggerFactory.getLogger(ServiceMonitor.class);
	public static String NUMERIC = "numeric";
	
	public static String NUMBER = "number";
	public static String STARTUP_TIME = "startup-time";
	public static String KEY = "sec";
	

	private final AppRepository appRepository;
	private final ServiceRepository serviceRepository;
	private final ServiceReportRepository serviceReportRepository;
	private final ProjectRepository projectRepository;
	private final EventRepository eventRepository;
	private final ResourceDataReader resourceDataReader;
	private final NodeRepository nodeRepository;
	private final TokenKubernetes tokenKubernetes;
	
	public ServiceMonitor(AppRepository appRepository, ServiceRepository serviceRepository, ServiceReportRepository serviceReportRepository, ProjectRepository projectRepository, EventRepository eventRepository, ResourceDataReader resourceDataReader, NodeRepository nodeRepository, TokenKubernetes tokenKubernetes) {
		this.appRepository = appRepository;
		this.serviceRepository = serviceRepository;
		this.serviceReportRepository = serviceReportRepository;
		this.projectRepository = projectRepository;
		this.eventRepository = eventRepository;
		this.resourceDataReader = resourceDataReader;
		this.nodeRepository = nodeRepository;
		this.tokenKubernetes = tokenKubernetes;
	}
	
	private void saveErrorEvent(Service service, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(service.getApp().getServiceProvider());
		event.setDetails(msg);
		event.setCategory("ServiceMonitor");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			Event event = new Event();
			event.setCategory("ServiceMonitor");
			event.setDetails("monitoring started");
			eventRepository.save(event);
			Instant timestamp = Instant.now().minusSeconds(60);
			for(Service service : serviceRepository.findAllOldRunning(timestamp)) {
				if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {
					if(service.getDeployType().equals(DeployType.KUBERNETES)){
						Node currentNode = resourceDataReader.getCurrentNode(service);
						
						if(currentNode != null) {
							Infrastructure infrastructure = currentNode.getInfrastructure();
							
							updateServiceRuntimeData(infrastructure, service);
							updateStartupTimes(infrastructure, service);
						}
					}
				}
			}
		}
	}
	
	private void updateServiceRuntimeData(Infrastructure infrastructure, Service service) {
		Node nodeHostingService = null;
		String servicePhase = null;
		
		ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);

		if(client != null) {
			log.info("ServiceMonitoring got a K8S client");

			Configuration.setDefaultApiClient(client);
			CoreV1Api coreV1Api = new CoreV1Api(client);
			//and here is the list of pods of this NS belonging to a given deployment
			for(Project project : projectRepository.getProjectListByServiceProviderId(service.getApp().getServiceProvider().getId())) {
				String namespace = null;
				try {namespace = ConverterJSON.convertToMap(project.getProperties()).get("namespace");}catch(Exception e) {}

				if(namespace != null) {
					try {
						V1PodList podList = coreV1Api.listNamespacedPod(namespace, null, null, null, null, "app="+service.getName(), null, null, null, null, null);
						for(V1Pod pod : podList.getItems()) {
							String candidateNodeName = pod.getSpec().getNodeName();
							servicePhase = pod.getStatus().getPhase();
							Optional<Node> nodeDB = nodeRepository.findByName(candidateNodeName);
							if(nodeDB.isPresent()) {
								nodeHostingService = nodeDB.get();
								break;
							}
						}
					}catch(Exception e) {
						log.error("ServiceMonitoring got an error: " + e.getMessage());
						saveErrorEvent(service, e.getMessage());
					}
				}
				if(nodeHostingService != null) {
					break;
				}
			}
		}
		/* removed, as it works only for K8S
		if(nodeHostingService != null) {
			Map<String, String> runtimeConfiguration = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
			runtimeConfiguration.put(ServiceScheduler.NODE_HOSTING, nodeHostingService.getName());
			service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfiguration));
		}
		*/
		if(servicePhase == null) {
			service.setStatus(ExecStatus.STOPPED);
			service.getApp().setStatus(ExecStatus.STOPPED);
			appRepository.save(service.getApp());
		}
		else if(servicePhase.equals("Running")) {
			service.setStatus(ExecStatus.RUNNING);
		}
		else {
			log.warn("ServiceMonitor: service " + service.getName() + " status is ERROR. Data: " + servicePhase);
			service.setStatus(ExecStatus.ERROR);
			service.getApp().setStatus(ExecStatus.ERROR);
			appRepository.save(service.getApp());
		}
		serviceRepository.save(service);
	}

	private void updateStartupTimes(Infrastructure infrastructure, Service service) {

		ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);

		if(client != null) {
			log.info("ServiceMonitor got a K8S client");

			Configuration.setDefaultApiClient(client);
			
			CoreV1Api coreV1Api = new CoreV1Api(client);

			Set<String> namespaceSet = new HashSet<String>();
			Optional<Service> serviceDB = serviceRepository.findById(service.getId());
			if(serviceDB.isPresent()) {
				Map<String, ServiceProvider> namespaceMap = new HashMap<String, ServiceProvider>();
				for(Project project : projectRepository.getProjectListByServiceProviderId(serviceDB.get().getApp().getServiceProvider().getId())) {
					ServiceProvider serviceProvider = project.getServiceProvider();
					if(project.getProperties() != null) {
		    			JSONObject jsonObject = new JSONObject(project.getProperties());
		    			String namespace = jsonObject.getString("namespace");
						if(namespace != null) {
							namespaceSet.add(namespace);
							namespaceMap.put(namespace, serviceProvider);
						}
					}
				}
				
				for(String namespace : namespaceSet) {	
					try {
						V1PodList podList = coreV1Api.listNamespacedPod(namespace, null, null, null, null, "app="+serviceDB.get().getName(), null, null, null, null, null);
						Instant minTimeScheduled = null;
						Instant maxTimeReady = null;
						
						for(V1Pod pod : podList.getItems()) {
							Instant podTimeScheduled = null;
							Instant podTimeReady = null;
							
							for(V1PodCondition condition : pod.getStatus().getConditions()) {
								if(condition.getStatus().equals("True")) {
									if(condition.getType().equals("PodScheduled")) {
										podTimeScheduled = condition.getLastTransitionTime().toInstant();
									}
									else if(condition.getType().equals("Ready")) {
										podTimeReady = condition.getLastTransitionTime().toInstant();
									} 
								}
							}
							if(podTimeScheduled != null && podTimeReady != null && podTimeScheduled.getEpochSecond() < podTimeReady.getEpochSecond()) {
								if(minTimeScheduled == null || podTimeScheduled.getEpochSecond() < minTimeScheduled.getEpochSecond()) {
									minTimeScheduled = podTimeScheduled;
								}
								if(maxTimeReady == null || podTimeReady.getEpochSecond() > maxTimeReady.getEpochSecond()) {
									maxTimeReady = podTimeReady;
								}
							}
						}
						if(minTimeScheduled != null && maxTimeReady != null) {
							String appGroup = namespaceMap.get(namespace).getName() + " on " + infrastructure.getName();
							
							ServiceReport serviceReport = new ServiceReport();
							serviceReport.setService(serviceDB.get());
							serviceReport.setCategory(STARTUP_TIME);
							serviceReport.setGroup(appGroup);
							serviceReport.setKey(KEY);
							serviceReport.setTimestamp(Instant.now());
							serviceReport.setValue(new Double(maxTimeReady.getEpochSecond()-minTimeScheduled.getEpochSecond()));
							saveOrUpdate(serviceReport);
						}
							
					}catch(Exception e) {
						log.error("ServiceMonitor", e);
						saveErrorEvent(service, "ServiceMonitor error on " + service.getName() + " " + e.getClass() + " " + e.getMessage());
					}
				}
			}
		}
	}
	
	private void saveOrUpdate(ServiceReport serviceReport) {
		List<ServiceReport> serviceReportDB = serviceReportRepository.findLastByServiceIdCategoryKey(serviceReport.getService().getId(), STARTUP_TIME, KEY);
		if(serviceReportDB.size() > 0) {
			serviceReportDB.get(0).setTimestamp(serviceReport.getTimestamp());
			serviceReportDB.get(0).setValue(serviceReport.getValue());
			serviceReportRepository.save(serviceReportDB.get(0));
		}
		else {
			serviceReportRepository.save(serviceReport);
		}
		
	}
}
