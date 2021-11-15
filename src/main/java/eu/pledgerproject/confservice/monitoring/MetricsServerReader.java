package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.InfrastructureReportRepository;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.scheduler.TokenKubernetes;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.ContainerMetrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.NodeMetricsList;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;

/*
    This class computes and stores the following metrics InfrastructureReport, NodeReport and ServiceReport cpu/mem usage
    
    
*/


@Component
public class MetricsServerReader {
	public static final String HEADER = "metrics-server";

	public static final String GROUP_ALL = "*";
	public static final String RESOURCE_USED = "resource-used";

	private final Logger log = LoggerFactory.getLogger(MetricsServerReader.class);

	public static String NUMBER = "number";

	private final InfrastructureReportRepository infrastructureReportRepository;

	private final TokenKubernetes tokenKubernetes;
	private final NodeRepository nodeRepository;
	private final NodeReportRepository nodeReportRepository;

	private final ServiceRepository serviceRepository;
	private final ServiceReportRepository serviceReportRepository;

	private final ProjectRepository projectRepository;
	private final EventRepository eventRepository;

	public MetricsServerReader(TokenKubernetes tokenKubernetes, InfrastructureReportRepository infrastructureReportRepository,  NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, ServiceRepository serviceRepository, ServiceReportRepository serviceReportRepository, ProjectRepository projectRepository, EventRepository eventRepository) {
		this.tokenKubernetes = tokenKubernetes;
		this.infrastructureReportRepository = infrastructureReportRepository;

		this.nodeRepository = nodeRepository;
		this.nodeReportRepository = nodeReportRepository;

		this.serviceRepository = serviceRepository;
		this.serviceReportRepository = serviceReportRepository;

		this.projectRepository = projectRepository;
		this.eventRepository = eventRepository;

	}

	private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("MetricsServerReader");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	private static Map<String, Double> getFormattedMetric(Map<String, Double> rawMetrics) {
		Map<String, Double> formattedMetrics = new HashMap<String, Double>();
		for(String key : rawMetrics.keySet()) {

			if(key.equals(MonitoringService.CPU)) {
				double value = (int)((1000 * rawMetrics.get(key).doubleValue()));
				formattedMetrics.put(MonitoringService.CPU_LABEL, value);
			}
			else if(key.equals(MonitoringService.MEMORY)) {
				double value = (int)((rawMetrics.get(key).doubleValue()/(1024*1024)));
				formattedMetrics.put(MonitoringService.MEMORY_LABEL, value);
			}
		}
		return formattedMetrics;
	}

	public void storeMetrics(Infrastructure infrastructure, Instant timestamp) {
		try {
			ApiClient client = tokenKubernetes.getKubernetesApiClient(infrastructure);
	
			if(client != null) {
				log.info("MetricsServerReader got a K8S client");
	
				Configuration.setDefaultApiClient(client);
	
				List<Node> nodeList = nodeRepository.findAllNodesByInfrastructureId(infrastructure.getId());
	
				Metrics metrics = new Metrics(client);
				
				//let's start with node metrics
				NodeMetricsList list = metrics.getNodeMetrics();
	
				//let's prepare the infrastructureMetrics Map
				Map<String, Double> infrastructureMetrics = new HashMap<String, Double>();
				
				if(list == null) {
					log.error("Unable to get MetricsServer metrics");
					saveErrorEvent("Unable to get MetricsServer metrics");
				}
				else {
					//then let's cycle through the metrics
					for (NodeMetrics item : list.getItems()) {
	
						//each item is a metric node, so let's check if there is a Node in the configuration that matches
						String nodeName = item.getMetadata().getName();
						Node node = null;
						for(Node nodeDB : nodeList) {
							if(nodeDB.getName().equals(nodeName)) {
								node = nodeDB;
								break;
							}
						}
						//if the metric node has a Node configured, let's proceed
						if(node != null) {
							log.info("MetricsServerReader found a node " + node.getName());
	
							//let's compute the node metrics and I add to the infrastructureMetrics Map
							Map<String, Double> nodeMetrics = new HashMap<String, Double>();
			
							for (String key : item.getUsage().keySet()) {
								Double value = item.getUsage().get(key).getNumber().doubleValue();
								nodeMetrics.put(key, value);
								if(!infrastructureMetrics.containsKey(key)) {
									infrastructureMetrics.put(key, value);
								}
								else {
									infrastructureMetrics.put(key, infrastructureMetrics.get(key) + value);
								}
							}
							//then let's format the metrics
							Map<String, Double> formattedMetricNode = getFormattedMetric(nodeMetrics);
						
							//and store a NodeReport for the current Node
							for(String key : formattedMetricNode.keySet()) {
								NodeReport nodeReport = new NodeReport();
								nodeReport.setTimestamp(timestamp);
								nodeReport.setNode(node);
								nodeReport.setCategory(RESOURCE_USED);
								nodeReport.setKey(key);
								nodeReport.setValue(formattedMetricNode.get(key));
								nodeReportRepository.save(nodeReport);
							}
						}
					}
					
					//then let's store an InfrastructureReport for each Infrastructure
					Map<String, Double> formattedMetricInfrastructure = getFormattedMetric(infrastructureMetrics);
					for(String key : formattedMetricInfrastructure.keySet()) {
						InfrastructureReport infrastructureReport = new InfrastructureReport();
						infrastructureReport.setTimestamp(timestamp);
						infrastructureReport.setInfrastructure(infrastructure);
						infrastructureReport.setCategory(RESOURCE_USED);
						infrastructureReport.setGroup(GROUP_ALL);
						infrastructureReport.setKey(key);
						infrastructureReport.setValue(formattedMetricInfrastructure.get(key));
						infrastructureReportRepository.save(infrastructureReport);
					}
		
					//now let's go with the App metrics.
					//First, we build a namespace->SP map (1 ns has 1 SP). Reversing the map could create a problem with SP in case 1 SP has many NS
					Map<String, ServiceProvider> namespaceMap = new HashMap<String, ServiceProvider>();
		
					for(Project project : projectRepository.getProjectListByInfrastructureId(infrastructure.getId())) {
						ServiceProvider serviceProvider = project.getServiceProvider();
						String namespace = null;
						try {namespace = ConverterJSON.convertToMap(project.getProperties()).get("namespace");}catch(Exception e) {}
	
						if(namespace != null) {
							log.info("MetricsServerReader found a namespace " + namespace);
							namespaceMap.put(namespace, serviceProvider);
						}
					}
		
					AppsV1Api appsV1Api = new AppsV1Api(client);
					CoreV1Api coreV1Api = new CoreV1Api(client);
		
					//Now let's build a Map to store SP -> (deployment name -> (metric name -> metric value))
					//ServiceProvider deployment consumption: serviceProviderName=k->deployName=x->cpu=y,memory=z
					Map<ServiceProvider, Map<String, Map<String, Double>>> serviceProviderDeploymentConsumptionMap = new HashMap<ServiceProvider, Map<String, Map<String, Double>>>();
		
					//let's cycle namespaces...
					for(String namespace : namespaceMap.keySet()) {
						ServiceProvider serviceProvider = namespaceMap.get(namespace);
						//.. and store an empty Map for each SP
						serviceProviderDeploymentConsumptionMap.put(serviceProvider, new HashMap<String, Map<String, Double>>());
		
						//for the current NS, let's build a pod -> deployment map
						Map<String, String> podDeploymentNameMap = new HashMap<String, String>();
						
						//let's first cycle the deployments in this NS
						for(V1Deployment deployment : appsV1Api.listNamespacedDeployment(namespace, null, null, null, null, null, null, null, null, null, null).getItems()) {
							String deploymentName = deployment.getMetadata().getName();
		
							//and here is the list of pods of this NS belonging to a given deployment 
							V1PodList podList = coreV1Api.listNamespacedPod(namespace, null, null, null, null, "app="+deploymentName, null, null, null, null, null);
							
							//so here is the pod -> deployment map
							for(V1Pod pod : podList.getItems()) {
								String podName = pod.getMetadata().getName();
								podDeploymentNameMap.put(podName, deploymentName);
							}
						}
		
						//We assume one K8S Deployment per "Configuration" service and each service can have multiple Pod.
						//This means a "Configuration" service is the atomic part of the App that can be placed or scaled
						//So, Pod metrics are stored per Deployment
						
						//let's fill deploymentConsumptionMap using Pod metrics
						for (PodMetrics podMetric : metrics.getPodMetrics(namespace).getItems()) {
							String podName = podMetric.getMetadata().getName();
		
							String deploymentName = podDeploymentNameMap.get(podName);
							if(deploymentName != null) {
								//let's create an empty container for the metrics
								if(!serviceProviderDeploymentConsumptionMap.get(serviceProvider).containsKey(deploymentName)) {
									serviceProviderDeploymentConsumptionMap.get(serviceProvider).put(deploymentName, new HashMap<String, Double>());
								}
		
								Map<String, Double> deploymentConsumption = serviceProviderDeploymentConsumptionMap.get(serviceProvider).get(deploymentName);
			
								//finally let's cycle the containers in the pod
								for (ContainerMetrics container : podMetric.getContainers()) {
									for (String key : container.getUsage().keySet()) {
										Double value = container.getUsage().get(key).getNumber().doubleValue();
			
										if(!deploymentConsumption.containsKey(key)) {
											deploymentConsumption.put(key, value);
										}
										else {
											deploymentConsumption.put(key, deploymentConsumption.get(key) + value);
										}
									}								
								}
							}
						}
					}
		
		
					//time to build some report: InfrastructureReport tied to ServiceProvider (how many resources a SP is usiing), then AppReport (about the App)
					Map<ServiceProvider, Map<String, Double>> serviceProviderConsumptionMap = new HashMap<ServiceProvider, Map<String, Double>>();
					for(ServiceProvider serviceProvider : serviceProviderDeploymentConsumptionMap.keySet()) {
						Map<String, Double> serviceProviderConsumption = new HashMap<String, Double>();
						serviceProviderConsumptionMap.put(serviceProvider, serviceProviderConsumption);
		
						Map<String, Map<String, Double>> deploymentConsumptionMap = serviceProviderDeploymentConsumptionMap.get(serviceProvider);
						for(String deploymentName : deploymentConsumptionMap.keySet()) {
		
							//Let's find the Service matching a given Deployment
							Service serviceFound = null;
							for(Service service : serviceRepository.getServiceListByServiceProviderId(serviceProvider.getId())) {
								String serviceName = service.getName();
								if(serviceName.equals(deploymentName)) {
									log.info("MetricsServerReader found a Service " + serviceName + " matching a Deployment");
		
									serviceFound = service;
									break;
								}
							}
							
							//Now le'ts create a report ONLY if the Service is running
							if(serviceFound != null && serviceFound.getStatus().equals(ExecStatus.RUNNING)) {
								String appGroup = serviceProvider.getName() + " on " + infrastructure.getName();
		
								Map<String, Double> deploymentConsumption = deploymentConsumptionMap.get(deploymentName);
								for(String key : deploymentConsumption.keySet()) {
									Double value = deploymentConsumption.get(key);
									if(!serviceProviderConsumption.containsKey(key)) {
										serviceProviderConsumption.put(key, value);
									}
									else {
										serviceProviderConsumption.put(key, serviceProviderConsumption.get(key) + value);
									}
								}
								Map<String, Double> formattedMetricApp = getFormattedMetric(deploymentConsumption);
								for(String key : formattedMetricApp.keySet()) {
									double value = formattedMetricApp.get(key);
									
									if(value <= 1.0) {
										value = 1.0;
									}
									ServiceReport serviceReport = new ServiceReport();
									serviceReport.setTimestamp(timestamp);
									serviceReport.setService(serviceFound);
									serviceReport.setKey(key);
									serviceReport.setCategory("resource-usage");
									serviceReport.setGroup(appGroup);
									serviceReport.setValue(formattedMetricApp.get(key));
									serviceReportRepository.save(serviceReport);
		
								}
							}
						}
						Map<String, Double> formattedMetricServiceProvider = getFormattedMetric(serviceProviderConsumption);
						for(String key : formattedMetricServiceProvider.keySet()) {
							InfrastructureReport infrastructureReport4SP = new InfrastructureReport();
							infrastructureReport4SP.setTimestamp(timestamp);
							infrastructureReport4SP.setInfrastructure(infrastructure);
							infrastructureReport4SP.setCategory(RESOURCE_USED);
							infrastructureReport4SP.setGroup(serviceProvider.getName());
							infrastructureReport4SP.setKey(key);
							
							infrastructureReport4SP.setValue(formattedMetricServiceProvider.get(key));
							infrastructureReportRepository.save(infrastructureReport4SP);
						}
					}
				}
			}
		}catch(Exception e) {
			log.error("MetricsServerReader", e);
			saveErrorEvent("MetricsServerReader error " + e.getClass() + " " + e.getMessage());
		}
	}
}
