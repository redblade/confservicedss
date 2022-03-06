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

import eu.pledgerproject.confservice.domain.AppConstraint;
import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.domain.Event;
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
import eu.pledgerproject.confservice.monitoring.MonitoringService;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.optimisation.Constants;
import eu.pledgerproject.confservice.optimisation.DeploymentOptionsManager;
import eu.pledgerproject.confservice.optimisation.RankingManager;
import eu.pledgerproject.confservice.optimisation.SLAViolationStatus;
import eu.pledgerproject.confservice.repository.AppConstraintRepository;
import eu.pledgerproject.confservice.repository.CriticalServiceRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
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
		PLACEHOLDER_MAP.put(Constants.REPLICAS, "PLACEHOLDER_REPLICAS");
		PLACEHOLDER_MAP.put(MonitoringService.NAMESPACE, "PLACEHOLDER_NAMESPACE");
		PLACEHOLDER_MAP.put(MonitoringService.CPU_LABEL, "PLACEHOLDER_CPU_MILLICORE");
		PLACEHOLDER_MAP.put(MonitoringService.MEMORY_LABEL, "PLACEHOLDER_MEMORY_MB");
	}
	
	public static String NUMERIC = "numeric";
	public static String NUMBER = "number";
	public static int RESOURCE_REQUEST_PRIORITY = 1000;

	private final Logger log = LoggerFactory.getLogger(ServiceScheduler.class);

	private final EventRepository eventRepository;
	private final AppConstraintRepository appConstraintRepository;

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
	
	public ServiceScheduler(EventRepository eventRepository, AppConstraintRepository appConstraintRepository, DeploymentOptionsManager deploymentOptionsManager, ProjectRepository projectRepository, ServiceRepository serviceRepository, OrchestratorKubernetes orchestratorKubernetes, OrchestratorDocker orchestratorDocker, ResourceDataReader resourceDataReader, ServiceReportRepository serviceReportRepository, SteadyServiceRepository steadyServiceRepository, CriticalServiceRepository criticalServiceRepository, RankingManager rankingManager, PublisherOrchestrationUpdate publisherOrchestrationUpdate, BenchmarkManager benchmarkManager, SlaViolationRepository slaViolationRepository) {
		this.eventRepository = eventRepository;
		this.appConstraintRepository = appConstraintRepository;
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
	
	private boolean isServiceValid(Service service) {
		Map<String, String> serviceInitialConfigurationProperties = ConverterJSON.convertToMap(service.getInitialConfiguration());
		if(!serviceInitialConfigurationProperties.containsKey(Constants.INITIAL_CPU_MILLICORE)) {
			return false;
		}
		if(!serviceInitialConfigurationProperties.containsKey(Constants.INITIAL_MEMORY_MB)) {
			return false;
		}
		String scaling = serviceInitialConfigurationProperties.get(Constants.SCALING);
		
		if(Constants.SCALING_VERTICAL.equals(scaling)) {
			if(!serviceInitialConfigurationProperties.containsKey(Constants.MIN_CPU_MILLICORE)) {
				return false;
			}
			if(!serviceInitialConfigurationProperties.containsKey(Constants.MIN_MEMORY_MB)) {
				return false;
			}
			if(!serviceInitialConfigurationProperties.containsKey(Constants.MAX_CPU_MILLICORE)) {
				return false;
			}
			if(!serviceInitialConfigurationProperties.containsKey(Constants.MAX_MEMORY_MB)) {
				return false;
			}
		}
		if(Constants.SCALING_HORIZONTAL.equals(scaling)) {
			if(!serviceInitialConfigurationProperties.containsKey(Constants.REPLICAS)) {
				return false;
			}
			if(!serviceInitialConfigurationProperties.containsKey(Constants.MAX_REPLICAS)) {
				return false;
			}
		}

		
		return true;
	}
	
	private void saveWarningEvent(Service service, String msg) {
    	if(log.isWarnEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setServiceProvider(service.getApp().getServiceProvider());
			event.setDetails(msg);
			event.setCategory("ServiceScheduler");
			event.severity(Event.WARNING);
			eventRepository.save(event);
    	}
	}
	
	public boolean start(Service service, Node node, int requestCpu, int requestMem) {
		boolean result = false;
		if(!isServiceValid(service)) {
			log.warn("Service " + service.getName() + " is missing mandatory initial parameters about min/max resources and max replicas, using default values");
			saveWarningEvent(service, "Service " + service.getName() + " is missing mandatory initial parameters about min/max resources and max replicas, using default values");
		}
			
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

			String replicasFromInitialConfiguration = ""+ResourceDataReader.getServiceReplicas(service);
			runtimeConfigurationMap.put(Constants.REPLICAS, replicasFromInitialConfiguration);
					
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
					if(ControlFlags.DOCKER_ENABLED) {
						orchestratorDocker.start(namespace, deploymentName, deploymentDescriptor, infrastructure);
					}
					else {
						throw new RuntimeException("start not supported for DeployType " + service.getDeployType());
					}
					
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
						initialConfigurationMap.put(Constants.INITIAL_CPU_MILLICORE, ""+serviceMaxResourceReservedCpu);
					}
					Integer serviceMaxResourceReservedMem = resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp);
					if(serviceMaxResourceReservedMem != null) {
						initialConfigurationMap.put(Constants.INITIAL_MEMORY_MB, ""+serviceMaxResourceReservedMem);
					}
					
					String replicasFromRuntimeConfiguration = ""+ResourceDataReader.getServiceReplicas(service);
					initialConfigurationMap.put(Constants.REPLICAS, replicasFromRuntimeConfiguration);
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
							if(ControlFlags.DOCKER_ENABLED) {
								orchestratorDocker.stop(namespace, deploymentName, deploymentDescriptor, infrastructure);
							}
							else {
								throw new RuntimeException("stop not supported for DeployType " + service.getDeployType());
							}
							
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
						slaViolation.setStatus(SLAViolationStatus.closed_app_stop.name());
						slaViolationRepository.save(slaViolation);
					}
				}
		}
	}
	
	
	public void scaleHorizontally(Service service, int newReplicas, boolean increaseResources) {
		log.info("Service scaled horizontally " + (increaseResources?"OUT":"IN") + " " + service.getName());
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
					
					//save replicas in initConfiguration
					Map<String, String> initialConfigurationMap = ConverterJSON.convertToMap(service.getInitialConfiguration());
					
					initialConfigurationMap.put(Constants.REPLICAS, newReplicas+"");
					service.setInitialConfiguration(ConverterJSON.convertToJSON(initialConfigurationMap));

					Map<String, String> runtimeConfigurationMap = ConverterJSON.convertToMap(service.getRuntimeConfiguration());
					runtimeConfigurationMap.put(Constants.REPLICAS, newReplicas+"");
					service.setRuntimeConfiguration(ConverterJSON.convertToJSON(runtimeConfigurationMap));
					
					//update the Service
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
		log.info("Service scaled vertically " + (increaseResources?"UP":"DOWN") + " " + service.getName());
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
							String replicasFromRuntimeConfiguration = ""+ResourceDataReader.getServiceReplicas(service);

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
	
	public void exposeSkupper(Service service, String port) {
		if(service.getApp().getManagementType().equals(ManagementType.MANAGED) && service.getDeployType().equals(DeployType.KUBERNETES)) {
			Infrastructure infrastructure = resourceDataReader.getCurrentNode(service).getInfrastructure();
			String deploymentName = service.getName();

			Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
			if(project.isPresent()) {
				String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
				orchestratorKubernetes.exposeSkupper(namespace, deploymentName, port, infrastructure);
			}
		}
	}
	public void unexposeSkupper(Service service, String port) {
		if(service.getApp().getManagementType().equals(ManagementType.MANAGED) && service.getDeployType().equals(DeployType.KUBERNETES)) {
			Infrastructure infrastructure = resourceDataReader.getCurrentNode(service).getInfrastructure();
			String deploymentName = service.getName();

			Optional<Project> project = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(service.getApp().getServiceProvider().getId(), infrastructure.getId());
			if(project.isPresent()) {
				String namespace = (String) ConverterJSON.convertToMap(project.get().getProperties()).get("namespace");
				orchestratorKubernetes.unexposeSkupper(namespace, deploymentName, port, infrastructure);
			}
		}
	}
	

	private void manageMulticlusterConnections(Long serviceDstID) {
		Optional<Service> serviceDstDB = serviceRepository.findById(serviceDstID);
		if(serviceDstDB.isPresent()) {
			Service serviceDst = serviceDstDB.get();
		
			if(serviceDst.getApp().getManagementType().equals(ManagementType.MANAGED) && serviceDst.getDeployType().equals(DeployType.KUBERNETES)) {
				
				for(AppConstraint appConstraint : appConstraintRepository.findByServiceDstCategoryAndValueType(serviceDst.getId(), OrchestratorKubernetes.SKUPPER_CATEGORY, OrchestratorKubernetes.SKUPPER_VALUE_TYPE)) {
					Service serviceSrc = appConstraint.getServiceSource();
					if(serviceDst.getApp().getManagementType().equals(ManagementType.MANAGED) && serviceDst.getDeployType().equals(DeployType.KUBERNETES)) {
						
						Infrastructure infrastructureSrc = resourceDataReader.getCurrentNode(serviceSrc).getInfrastructure();
						Infrastructure infrastructureDst = resourceDataReader.getCurrentNode(serviceDst).getInfrastructure();
						
						String deploymentNameDst = serviceDst.getName();
	
						Optional<Project> projectDst = projectRepository.getProjectByServiceProviderIdAndInfrastructureId(serviceDst.getApp().getServiceProvider().getId(), infrastructureDst.getId());
						if(projectDst.isPresent()) {
							String namespaceDst = (String) ConverterJSON.convertToMap(projectDst.get().getProperties()).get("namespace");
							
							//if infrastructures are different create link
							if(infrastructureSrc.getId() != infrastructureDst.getId()){
								orchestratorKubernetes.exposeSkupper(namespaceDst, deploymentNameDst, appConstraint.getValue(), infrastructureDst);
							}
							//else if infrastructures are equal remove link
							else {
								orchestratorKubernetes.unexposeSkupper(namespaceDst, deploymentNameDst, appConstraint.getValue(), infrastructureDst);
							}
						}
					}
				}
			}		
		}
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
								initialConfigurationMap.put(Constants.INITIAL_CPU_MILLICORE, ""+resourceDataReader.getServiceMaxResourceReservedCpuInPeriod(service, timestamp));
								initialConfigurationMap.put(Constants.INITIAL_MEMORY_MB, ""+resourceDataReader.getServiceMaxResourceReservedMemInPeriod(service, timestamp));
								
								String replicasFromRuntimeConfiguration = ""+ResourceDataReader.getServiceReplicas(service);
								initialConfigurationMap.put(Constants.REPLICAS, replicasFromRuntimeConfiguration);
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
									if(ControlFlags.DOCKER_ENABLED) {
										orchestratorDocker.stop(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
									}
									else {
										throw new RuntimeException("migrate not supported for DeployType " + service.getDeployType());
									}
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

					
					String replicasFromInitialConfiguration = ""+ResourceDataReader.getServiceReplicas(service);
					runtimeConfigurationMap.put(Constants.REPLICAS, replicasFromInitialConfiguration);
					
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
						if(ControlFlags.DOCKER_ENABLED) {
							orchestratorDocker.start(namespace, deploymentName, deploymentDescriptor, project.get().getInfrastructure());
						}
						else {
							throw new RuntimeException("migrate not supported for DeployType " + service.getDeployType());
						}
					}
					
				}
			}
			if(ControlFlags.MULTICLOUD_ENABLED) {
				manageMulticlusterConnections(service.getId());
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
