package eu.pledgerproject.confservice.scheduler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.domain.enumeration.DeployType;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;
import eu.pledgerproject.confservice.message.PublisherOrchestrationUpdate;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.DeploymentOptionsManager;
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.monitoring.RankingManager;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.monitoring.SlaViolationStatus;
import eu.pledgerproject.confservice.repository.CriticalServiceRepository;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.repository.SteadyServiceRepository;

@org.springframework.stereotype.Service
public class ServiceScheduler {
	public static String RUNTIME_NODE_SELECTED = "nodes_selected";
	public static String RUNTIME_INFRASTRUCTURE_SELECTED = "infrastructure_id";
	public static String RUNTIME_NAMESPACE = "namespace";
	
	private static final Map<String, String> PLACEHOLDER_MAP = new HashMap<String, String>();
	static {
		PLACEHOLDER_MAP.put(RUNTIME_NODE_SELECTED, "PLACEHOLDER_HOSTNAME");
		PLACEHOLDER_MAP.put("replicas", "PLACEHOLDER_REPLICAS");
		PLACEHOLDER_MAP.put("namespace", "PLACEHOLDER_NAMESPACE");
		PLACEHOLDER_MAP.put("cpu_millicore", "PLACEHOLDER_CPU_MILLICORE");
		PLACEHOLDER_MAP.put("memory_mb", "PLACEHOLDER_MEMORY_MB");
	}
	
	public static String NUMERIC = "numeric";
	public static String NUMBER = "number";
	public static int RESOURCE_REQUEST_PRIORITY = 1000;

	private final Logger log = LoggerFactory.getLogger(ServiceScheduler.class);

	private final DeploymentOptionsManager deploymentOptionsManager;
	private final ProjectRepository projectRepository;
	private final ServiceRepository serviceRepository;
	private final OrchestratorKubernetes orchestratorKubernetes;
	private final OrchestratorDocker orchestratorDocker;
	private final ResourceDataReader resourceDataReader;
	private final ServiceReportRepository serviceReportRepository;
	private final SteadyServiceRepository steadyServiceRepository;
	private final CriticalServiceRepository criticalServiceRepository;
	private final RankingManager rankingManager;
	private final PublisherOrchestrationUpdate publisherOrchestrationUpdate;
	private final BenchmarkManager benchmarkManager;
	private final SlaViolationRepository slaViolationRepository;
	
	public ServiceScheduler(DeploymentOptionsManager deploymentOptionsManager, ProjectRepository projectRepository, ServiceRepository serviceRepository, OrchestratorKubernetes orchestratorKubernetes, OrchestratorDocker orchestratorDocker, ResourceDataReader resourceDataReader, ServiceReportRepository serviceReportRepository, SteadyServiceRepository steadyServiceRepository, CriticalServiceRepository criticalServiceRepository, RankingManager rankingManager, PublisherOrchestrationUpdate publisherOrchestrationUpdate, BenchmarkManager benchmarkManager, SlaViolationRepository slaViolationRepository) {
		this.deploymentOptionsManager = deploymentOptionsManager;
		this.projectRepository = projectRepository;
		this.serviceRepository = serviceRepository;
		this.orchestratorKubernetes = orchestratorKubernetes;
		this.orchestratorDocker = orchestratorDocker;
		this.resourceDataReader = resourceDataReader;
		this.serviceReportRepository = serviceReportRepository;
		this.steadyServiceRepository = steadyServiceRepository;
		this.criticalServiceRepository = criticalServiceRepository;
		this.rankingManager = rankingManager;
		this.publisherOrchestrationUpdate = publisherOrchestrationUpdate;
		this.benchmarkManager = benchmarkManager;
		this.slaViolationRepository = slaViolationRepository;

	}
	
	private void saveOrUpdateServiceRequest(Service service, String appGroup, String constraintName, String constraintCategory, int priority, String valueType, String value) {
		//to produce historical reports in Grafana
		ServiceReport serviceReport = new ServiceReport();
		serviceReport.setService(service);
		serviceReport.setTimestamp(Instant.now());
		serviceReport.setCategory(constraintCategory);
		serviceReport.setGroup(appGroup);
		serviceReport.setKey(constraintName);
		serviceReport.setValue(Double.parseDouble(value));
		serviceReportRepository.save(serviceReport);
	}
	
	private Map<String, String> getMessageParameters(Service service, Infrastructure infrastructure){
		Map<String, String> result = new HashMap<String, String>();
		result.put("target_infra_id", ""+infrastructure.getId());
		result.put("prometheus_endpoint", ConverterJSON.convertToMap(infrastructure.getMonitoringPlugin()).get("prometheus_endpoint"));
		return result;
	}
	
	private JSONArray getMessagePlaceholders(Service service){
		JSONArray result = new JSONArray();

		Map<String, String> runtimeConfiguration = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
		
		for(String key : runtimeConfiguration.keySet()) {
			if(PLACEHOLDER_MAP.keySet().contains(key)) {
				JSONObject placeholderElem = new JSONObject();
				placeholderElem.put("placeholder", PLACEHOLDER_MAP.get(key));
				String placeholderValue = runtimeConfiguration.get(key);
				//this is to manage vectors in K8S descriptors
				if(key.equals(RUNTIME_NODE_SELECTED)) {
					placeholderValue = DescriptorParserKubernetes.getHostnameVectorString(placeholderValue);
				}
				placeholderElem.put("value", placeholderValue);
				result.put(placeholderElem);
			}
		}
		
		return result;
	}
	
	public boolean start(Service service, Node node, int requestCpu, int requestMem) {
		boolean result = false;
		
		log.info("request to have Service started " + service.getName());
		Infrastructure infrastructure = node.getInfrastructure();

		Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
		if(project.isPresent()) {
			Map<String, String> runtimeConfigurationMap = new HashMap<String, String>();
			
			String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
			runtimeConfigurationMap.put(RUNTIME_NAMESPACE, namespace);
			runtimeConfigurationMap.put(RUNTIME_NODE_SELECTED, node.getName());
			runtimeConfigurationMap.put(RUNTIME_INFRASTRUCTURE_SELECTED, ""+infrastructure.getId());

			runtimeConfigurationMap.put(MonitoringService.CPU_LABEL, ""+requestCpu);
			runtimeConfigurationMap.put(MonitoringService.MEMORY_LABEL, ""+requestMem);

			String replicasFromInitialConfiguration = (String) ConverterJSON.convertToMap(service.getInitialConfiguration()).get("replicas");
			if(replicasFromInitialConfiguration == null || replicasFromInitialConfiguration.isEmpty()) {
				replicasFromInitialConfiguration = "1";
			}
			runtimeConfigurationMap.put("replicas", replicasFromInitialConfiguration);
					
			service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
			
			//update the Service
			serviceRepository.save(service);

			String appGroup = project.get().getServiceProvider().getName() + " on " + project.get().getInfrastructure().getName();
			saveOrUpdateServiceRequest(service, appGroup, MonitoringService.CPU_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestCpu);
			saveOrUpdateServiceRequest(service, appGroup, MonitoringService.MEMORY_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestMem);
					
			if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {
				
				String deploymentName = service.getName();
				
				String deploymentDescriptor = service.getDeployDescriptor();
				deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, ""+requestCpu, ""+requestMem, node.getName(), replicasFromInitialConfiguration);
					
				if(service.getDeployType().equals(DeployType.KUBERNETES)) {	
					orchestratorKubernetes.start(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
					result = true;
				}
				else if (service.getDeployType().equals(DeployType.DOCKER)) {
					if(!ControlFlags.EXPERIMENTAL_FEATURES_ENABLED) {
						throw new RuntimeException("start not supported for DeployType " + service.getDeployType());
					}
					orchestratorDocker.start(namespace, deploymentName, deploymentDescriptor, infrastructure);
				}
				service.setLastChangedStatus(Instant.now());
				service.setStatus(ExecStatus.RUNNING);
				serviceRepository.save(service);

			}
	
			else if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
				publisherOrchestrationUpdate.publish(service.getId(), "service", "start", getMessageParameters(service, infrastructure), getMessagePlaceholders(service));
				service.setLastChangedStatus(Instant.now());
				service.setStatus(ExecStatus.RUNNING);
				serviceRepository.save(service);
				result = true;
				
				log.info("Delegated: service " + service.getName() + " start");
			}
		}
		
		return result;
	}
	
	public void stop(Service service) {
		log.info("Service stopped " + service.getName());

		Node currentNode = resourceDataReader.getCurrentNode(service);
		if(currentNode != null) {
			Infrastructure infrastructure = currentNode.getInfrastructure();
				
				Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
				if(project.isPresent()) {
					String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");

			    	Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
			    	int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
					Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);

					
					//save resources and replicas in initConfiguration
					Map<String, String> initialConfigurationMap = ConverterJSON.convertToMap(service.getInitialConfiguration());
					Integer serviceMaxResourceReservedCpu = resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(service, timestamp);
					if(serviceMaxResourceReservedCpu != null) {
						initialConfigurationMap.put(MonitoringService.INITIAL_CPU_MILLICORE, ""+serviceMaxResourceReservedCpu);
					}
					Integer serviceMaxResourceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp);
					if(serviceMaxResourceReservedMem != null) {
						initialConfigurationMap.put(MonitoringService.INITIAL_MEMORY_MB, ""+serviceMaxResourceReservedMem);
					}
					
					String replicasFromRuntimeConfiguration = (String) ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");
					initialConfigurationMap.put("replicas", replicasFromRuntimeConfiguration);
					service.setInitialConfiguration(ConverterJSON.convertToJSON(initialConfigurationMap));

					//clear the runtimeConfiguration
					Map<String, String> runtimeConfigurationMap = new HashMap<String, String>();
					service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
					
					//update the Service
					serviceRepository.save(service);
					
					if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {

						String deploymentName = service.getName();
						
						String deploymentDescriptor = service.getDeployDescriptor();
						deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, currentNode.getName(), replicasFromRuntimeConfiguration);

						if(service.getDeployType().equals(DeployType.KUBERNETES)) {
							orchestratorKubernetes.stop(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
						}
						else if (service.getDeployType().equals(DeployType.DOCKER)) {
							if(!ControlFlags.EXPERIMENTAL_FEATURES_ENABLED) {
								throw new RuntimeException("stop not supported for DeployType " + service.getDeployType());
							}
							orchestratorDocker.stop(namespace, deploymentName, deploymentDescriptor, infrastructure);
						}
						
						service.setLastChangedStatus(Instant.now());
						service.setStatus(ExecStatus.STOPPED);
						serviceRepository.save(service);
					}
					else if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
						publisherOrchestrationUpdate.publish(service.getId(), "service", "stop", getMessageParameters(service, infrastructure), getMessagePlaceholders(service));
						service.setLastChangedStatus(Instant.now());
						service.setStatus(ExecStatus.STOPPED);
						serviceRepository.save(service);
						
						log.info("Delegated: service " + service.getName() + " start");
					}
					
					Optional<SteadyService> steadyServiceDB = steadyServiceRepository.getByServiceID(service.getId());
					if(steadyServiceDB.isPresent()) {
						steadyServiceRepository.delete(steadyServiceDB.get());
					}
					Optional<CriticalService> criticalServiceDB = criticalServiceRepository.getByServiceID(service.getId());
					if(criticalServiceDB.isPresent()) {
						criticalServiceRepository.delete(criticalServiceDB.get());
					}
					for(SlaViolation slaViolation : slaViolationRepository.findAllNotClosed(service)) {
						slaViolation.setStatus(SlaViolationStatus.closed_app_stop.name());
						slaViolationRepository.save(slaViolation);
					}
				}
		}
	}
	
	
	public void scaleHorizontally(Service service, int newReplicas, boolean increaseResources) {
		log.info("Service scaled horizontally " + service.getName());
		service.setLastChangedStatus(Instant.now());
		service.setStatus(ExecStatus.RUNNING);
		serviceRepository.save(service);
		
		String runtimeConfiguration = service.getRuntimeConfiguration();
		
		if(runtimeConfiguration != null) {
			Node currentNode = resourceDataReader.getCurrentNode(service);
			if(currentNode != null ) {
				Infrastructure infrastructure = currentNode.getInfrastructure();
				
				Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
				if(project.isPresent()) {
					
					Map<String, String> runtimeConfigurationMap = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
					runtimeConfigurationMap.put("replicas", newReplicas+"");
					service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
					serviceRepository.save(service);
						
					if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {
						String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");

						if(service.getDeployType().equals(DeployType.KUBERNETES)) {
							orchestratorKubernetes.scale(namespace, service.getName(), newReplicas, project.get().getInfrastructure());
						}
						else {
							throw new RuntimeException("Scale horiz. not supported for DeployType " + service.getDeployType());
						}
					}
					else if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
						publisherOrchestrationUpdate.publish(service.getId(), "service", increaseResources ? "scaleout" : "scalein", getMessageParameters(service, infrastructure), getMessagePlaceholders(service));
						
						log.info("Delegated: service " + service.getName() + " " + (increaseResources ? "scaleout" : "scalein"));
					}
					
				}
			}
		}
	}
	
	public void scaleVertically(Service service, Integer requestCpu, Integer requestMem, boolean increaseResources) {
		scaleVertically(service, requestCpu==null?null:(""+requestCpu), requestMem==null?null:(""+requestMem), increaseResources);
	}

	public void scaleVertically(Service service, String requestCpu, String requestMem, boolean increaseResources) {
		log.info("Service scaled vertically " + service.getName());
		service.setLastChangedStatus(Instant.now());
		service.setStatus(ExecStatus.RUNNING);
		serviceRepository.save(service);

		if(service.getRuntimeConfiguration() != null) {
			Map<String, String> runtimeConfigurationMap = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
			
			Node currentNode = resourceDataReader.getCurrentNode(service);
			if(currentNode != null) {
				Infrastructure infrastructure = currentNode.getInfrastructure();
				if(infrastructure != null) {
					
					Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
					if(project.isPresent()) {
						
						runtimeConfigurationMap.put(MonitoringService.CPU_LABEL, ""+requestCpu);
						runtimeConfigurationMap.put(MonitoringService.MEMORY_LABEL, ""+requestMem);
						service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
						serviceRepository.save(service);
						
						String appGroup = project.get().getServiceProvider().getName() + " on " + project.get().getInfrastructure().getName();
						saveOrUpdateServiceRequest(service, appGroup, MonitoringService.CPU_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestCpu);
						saveOrUpdateServiceRequest(service, appGroup, MonitoringService.MEMORY_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestMem);
										
						if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {
								
							String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
							String deploymentDescriptor = service.getDeployDescriptor();
							String replicasFromRuntimeConfiguration = (String) ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");

							deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, requestCpu, requestMem, currentNode.getName(), replicasFromRuntimeConfiguration);

							if(service.getDeployType().equals(DeployType.KUBERNETES)) {
								orchestratorKubernetes.replace(namespace, service.getName(), deploymentDescriptor, project.get().getInfrastructure());
							}
							else {
								throw new RuntimeException("Scale vert. not supported for DeployType " + service.getDeployType());
							}
						}
						else if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
							publisherOrchestrationUpdate.publish(service.getId(), "service", increaseResources ? "scaleup":"scaledown", getMessageParameters(service, infrastructure), getMessagePlaceholders(service));
							
							log.info("Delegated: service " + service.getName() + " " + (increaseResources ? "scaleup" : "scaledown"));
						} 
					}
				}
			}
		}
	}
	
	public Node migrateToRanking(Service service, boolean isBetterRanking) {
		Node bestNode = null;
		
    	Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
    	int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
		Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);
		
		//get max resource requests for the current service
		Integer maxRequestMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp);
		Integer maxRequestCpu = resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(service, timestamp);

		//compute the currentRanking
		SortedMap<Integer, Set<Node>> rankingMap = rankingManager.getAvailableRankingMapForRequestedResources(service, maxRequestCpu, maxRequestMem);
		int currentRanking = deploymentOptionsManager.getRankingInDeploymentOptions(service.getId());

		//do the offload to the next better/worse ranking IF it exists (eg. currentRanking is 4: better means 3, if no priority like that exists, it does nothing
		if(isBetterRanking && rankingMap.containsKey(currentRanking-1)) {
			Set<Node> nodeSet = rankingMap.get(currentRanking-1);
			bestNode = benchmarkManager.getBestNodeUsingBenchmark(service, nodeSet);
			migrate(service, bestNode, maxRequestCpu, maxRequestMem);
		}
		else if(!isBetterRanking && rankingMap.containsKey(currentRanking+1)) {
			Set<Node> nodeSet = rankingMap.get(currentRanking+1);
			bestNode = benchmarkManager.getBestNodeUsingBenchmark(service, nodeSet);
			migrate(service, bestNode, maxRequestCpu, maxRequestMem);
		}
		
		return bestNode;
	}
	
	public void migrate(Service service, Node bestNode, Integer requestCpu, Integer requestMem) {
		log.info("Service migrated " + service.getName());
		service.setLastChangedStatus(Instant.now());
		service.setStatus(ExecStatus.RUNNING);
		serviceRepository.save(service);
		
		if(service.getApp().getManagementType().equals(ManagementType.MANAGED)) {
			if(service.getDeployType().equals(DeployType.KUBERNETES) || service.getDeployType().equals(DeployType.DOCKER)) {

				String runtimeConfiguration = service.getRuntimeConfiguration();
				if(runtimeConfiguration != null && !runtimeConfiguration.isEmpty()) {
					Node currentNode = resourceDataReader.getCurrentNode(service);
					if(currentNode != null) {
						Infrastructure infrastructure = currentNode.getInfrastructure();
						
						if(infrastructure != null) {
							Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
							if(project.isPresent()) {
		
						    	Map<String, String> preferences = ConverterJSON.convertToMap(service.getApp().getServiceProvider().getPreferences());
						    	int monitoringSlaViolationPeriodSec = Integer.valueOf(preferences.get("monitoring.slaViolation.periodSec"));
								Instant timestamp = Instant.now().minusSeconds(monitoringSlaViolationPeriodSec);

								//save resources and replicas in initConfiguration
								Map<String, String> initialConfigurationMap = ConverterJSON.convertToMap(service.getInitialConfiguration());
								initialConfigurationMap.put(MonitoringService.INITIAL_CPU_MILLICORE, ""+resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(service, timestamp));
								initialConfigurationMap.put(MonitoringService.INITIAL_MEMORY_MB, ""+resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp));
								
								String replicasFromRuntimeConfiguration = (String) ConverterJSON.convertToMap(service.getRuntimeConfiguration()).get("replicas");
								initialConfigurationMap.put("replicas", replicasFromRuntimeConfiguration);
								service.setInitialConfiguration(ConverterJSON.convertToJSON(initialConfigurationMap));

								//clear the runtimeConfiguration
								Map<String, String> runtimeConfigurationMap = new HashMap<String, String>();
								service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
								
								//update the Service
								serviceRepository.save(service);

								
								String deploymentName = service.getName();
								String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
								String deploymentDescriptor = service.getDeployDescriptor();
								deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, ""+requestCpu, ""+requestMem, bestNode.getName(), replicasFromRuntimeConfiguration);

								if(service.getDeployType().equals(DeployType.KUBERNETES)) {
									orchestratorKubernetes.stop(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
								}
								else if(service.getDeployType().equals(DeployType.DOCKER)) {
									if(!ControlFlags.EXPERIMENTAL_FEATURES_ENABLED) {
										throw new RuntimeException("migrate not supported for DeployType " + service.getDeployType());
									}
									orchestratorDocker.stop(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
								}

								Optional<SteadyService> steadyServiceDB = steadyServiceRepository.getByServiceID(service.getId());
								if(steadyServiceDB.isPresent()) {
									steadyServiceRepository.delete(steadyServiceDB.get());
								}
								Optional<CriticalService> criticalServiceDB = criticalServiceRepository.getByServiceID(service.getId());
								if(criticalServiceDB.isPresent()) {
									criticalServiceRepository.delete(criticalServiceDB.get());
								}
							}
						}
					}
				}
			}
			
			//start()
			if(bestNode != null) {
				//get the first node to retrieve the infrastructure
				//so we assume all the selected Nodes are on the SAME infrastructure
				Infrastructure infrastructure = bestNode.getInfrastructure();
			
				Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
				if(project.isPresent()) {
					Map<String, String> runtimeConfigurationMap = new HashMap<String, String>();
					
					String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
					runtimeConfigurationMap.put(RUNTIME_NAMESPACE, namespace);
					runtimeConfigurationMap.put(RUNTIME_NODE_SELECTED, bestNode.getName());
					runtimeConfigurationMap.put(RUNTIME_INFRASTRUCTURE_SELECTED, ""+infrastructure.getId());
					
					runtimeConfigurationMap.put(MonitoringService.CPU_LABEL, ""+requestCpu);
					runtimeConfigurationMap.put(MonitoringService.MEMORY_LABEL, ""+requestMem);

					
					String replicasFromInitialConfiguration = (String) ConverterJSON.convertToMap(service.getInitialConfiguration()).get("replicas");
					if(replicasFromInitialConfiguration == null || replicasFromInitialConfiguration.isEmpty()) {
						replicasFromInitialConfiguration = "1";
					}
					runtimeConfigurationMap.put("replicas", replicasFromInitialConfiguration);
					
					service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
					
					//update the Service
					serviceRepository.save(service);

					String appGroup = project.get().getServiceProvider().getName() + " on " + project.get().getInfrastructure().getName();
					saveOrUpdateServiceRequest(service, appGroup, MonitoringService.CPU_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestCpu);
					saveOrUpdateServiceRequest(service, appGroup, MonitoringService.MEMORY_LABEL, ResourceDataReader.MAX_REQUEST_LABEL, RESOURCE_REQUEST_PRIORITY, NUMERIC, ""+requestMem);
							
					String deploymentName = service.getName();

					String deploymentDescriptor = service.getDeployDescriptor();
					deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, ""+requestCpu, ""+requestMem, bestNode.getName(), replicasFromInitialConfiguration);
						
					if(service.getDeployType().equals(DeployType.KUBERNETES)) {
						orchestratorKubernetes.start(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
					}
					else if(service.getDeployType().equals(DeployType.DOCKER)) {
						if(!ControlFlags.EXPERIMENTAL_FEATURES_ENABLED) {
							throw new RuntimeException("migrate not supported for DeployType " + service.getDeployType());
						}
						orchestratorDocker.start(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
					}
					
				}
			}
		}
		else if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
				
			Infrastructure infrastructure = bestNode.getInfrastructure();
			String runtimeConfiguration = service.getRuntimeConfiguration();
			if(runtimeConfiguration != null && !runtimeConfiguration.isEmpty()) {
			
				Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
				if(project.isPresent()) {
					Map<String, String> runtimeConfigurationMap = ConverterJSON.convertToMap(runtimeConfiguration);
					String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
					
					runtimeConfigurationMap.put(RUNTIME_NAMESPACE, namespace);
					runtimeConfigurationMap.put(RUNTIME_NODE_SELECTED, bestNode.getName());
					runtimeConfigurationMap.put(RUNTIME_INFRASTRUCTURE_SELECTED, ""+infrastructure.getId());

					service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
					serviceRepository.save(service);
			
					publisherOrchestrationUpdate.publish(service.getId(), "service", "placement", getMessageParameters(service, infrastructure), getMessagePlaceholders(service));
					
					log.info("Delegated: service " + service.getName() + " placement on nodes [" + bestNode.getName() + "]");
				}
			}
		}
	}

}
