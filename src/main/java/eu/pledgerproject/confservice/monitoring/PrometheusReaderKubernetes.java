package eu.pledgerproject.confservice.monitoring;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import eu.pledgerproject.confservice.util.DoubleFormatter;


/*
 
This class computes and stores the following metrics InfrastructureReport, NodeReport and ServiceReport for cpu/mem usage
  
It requires a monitoring_plugin configured in Infrastructure as 

{ 
  
  'monitoring_type': 'prometheus',
  'prometheus_endpoint': 'http://192.168.111.52:30754' (ENG)
  
  or
  'monitoring_type': 'prometheus',
  'prometheus_endpoint': 'http://172.16.10.10:30090' (i2CAT)
  
}	   

*/


@Component
public class PrometheusReaderKubernetes {
	public static final String HEADER = "prometheus";

	public static final String GROUP_ALL = "*";
	public static final String RESOURCE_USED = "resource-used";

	public static final String QUERY = "/api/v1/query?query=";
	public static final String METRICS = "/metrics";
	public static final String QUERY_CPU_INFRA = "sum(node_namespace_pod_container:container_cpu_usage_seconds_total:sum_rate)";
	public static final String QUERY_MEM_INFRA = "sum(container_memory_working_set_bytes)";
	public static final String QUERY_CPU_NODE = "sum(node_namespace_pod_container:container_cpu_usage_seconds_total:sum_rate) by (node)";
	public static final String QUERY_MEM_NODE = "sum(container_memory_working_set_bytes) by (node)";
	public static final String QUERY_CPU_POD = "sum(node_namespace_pod_container:container_cpu_usage_seconds_total:sum_rate{namespace='NAMESPACE'}) by (pod)";
	public static final String QUERY_MEM_POD = "sum(container_memory_working_set_bytes{namespace='NAMESPACE'}) by (pod)";

	private final Logger log = LoggerFactory.getLogger(PrometheusReaderKubernetes.class);

	public static String NUMBER = "number";

	private final InfrastructureReportRepository infrastructureReportRepository;

	private final NodeRepository nodeRepository;
	private final NodeReportRepository nodeReportRepository;

	private final ServiceRepository serviceRepository;
	private final ServiceReportRepository serviceReportRepository;

	private final ProjectRepository projectRepository;
	private final EventRepository eventRepository;


	public PrometheusReaderKubernetes(InfrastructureReportRepository infrastructureReportRepository,  NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, ServiceRepository serviceRepository, ServiceReportRepository serviceReportRepository, ProjectRepository projectRepository, EventRepository eventRepository) {
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
		event.setCategory("PublisherConfigurationUpdate");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	private static Map<String, BigDecimal> getQueryMetricsMap(String endpoint, String query) throws IOException, JSONException{
		Map<String, BigDecimal>  result = new HashMap<String, BigDecimal> ();
		try(java.io.InputStream in = new URL(endpoint + QUERY + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())).openStream()){
			String response = IOUtils.toString(in, Charset.defaultCharset());
			
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
			JSONObject jsonObjectElem = jsonArray.getJSONObject(0);
			BigDecimal value = new BigDecimal((jsonObjectElem.getJSONArray("value").get(1)).toString());
			result.put("infrastructure", value);
		}
		return result;
		
	}
	
	private static Map<String, BigDecimal> getQueryMetricsMap(String endpoint, String query, String discriminator) throws IOException, JSONException{
		Map<String, BigDecimal>  result = new HashMap<String, BigDecimal> ();
		try(java.io.InputStream in = new URL(endpoint + QUERY + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())).openStream()){
			String response = IOUtils.toString(in, Charset.defaultCharset());
			
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
			for(int i=0; i<jsonArray.length(); i++) {
				JSONObject jsonObjectElem = jsonArray.getJSONObject(i); 
				String key = jsonObjectElem.getJSONObject("metric").getString(discriminator);
				BigDecimal value = new BigDecimal((jsonObjectElem.getJSONArray("value").get(1)).toString());
				
				result.put(key, value);
			}
		}
		return result;
		
	}
	
	private static Map<String, Double> getFormattedMetric(Map<String, BigDecimal> rawMetrics) {
		Map<String, Double> formattedMetrics = new HashMap<String, Double>();
		for(String key : rawMetrics.keySet()) {

			if(key.equals(MonitoringService.CPU)) {
				double value = 1000 * rawMetrics.get(key).doubleValue();
				formattedMetrics.put(MonitoringService.CPU_LABEL, value);
			}
			else if(key.equals(MonitoringService.MEMORY)) {
				double value = (rawMetrics.get(key).doubleValue()/(1024*1024));
				formattedMetrics.put(MonitoringService.MEMORY_LABEL, value);
			}
		}
		return formattedMetrics;
	}

	private static Map<String, Double> getFormattedMetricCPU(Map<String, BigDecimal> rawMetrics) {
		Map<String, Double> formattedMetrics = new HashMap<String, Double>();
		for(String key : rawMetrics.keySet()) {

			double value = DoubleFormatter.format((1000 * rawMetrics.get(key).doubleValue()));
			formattedMetrics.put(key, value);
		}
		return formattedMetrics;
	}
	
	private static Map<String, Double> getFormattedMetricMEM(Map<String, BigDecimal> rawMetrics) {
		Map<String, Double> formattedMetrics = new HashMap<String, Double>();
		for(String key : rawMetrics.keySet()) {

			double value = DoubleFormatter.format((rawMetrics.get(key).doubleValue()/(1024*1024)));
			formattedMetrics.put(key, value);
		}
		return formattedMetrics;
	}

	public void storeMetrics(Infrastructure infrastructure, String endpoint, Instant timestamp) {
		try {
			if(endpoint != null) {
				log.info("Prometheus got a K8S client");
	
				//INFRA CPU - just 1key->1value
				Map<String, Double> infrastructureMetricsCpu = getFormattedMetricCPU(getQueryMetricsMap(endpoint, QUERY_CPU_INFRA));
				for(String key : infrastructureMetricsCpu.keySet()) {
					InfrastructureReport infrastructureReport = new InfrastructureReport();
					infrastructureReport.setTimestamp(timestamp);
					infrastructureReport.setInfrastructure(infrastructure);
					infrastructureReport.setKey(MonitoringService.CPU_LABEL);
					infrastructureReport.setGroup(GROUP_ALL);
					infrastructureReport.setValue(infrastructureMetricsCpu.get(key));
					infrastructureReportRepository.save(infrastructureReport);
				}
				
				//INFRA MEM - just 1key->1value
				Map<String, Double> infrastructureMetricsMem = getFormattedMetricMEM(getQueryMetricsMap(endpoint, QUERY_MEM_INFRA));
				for(String key : infrastructureMetricsMem.keySet()) {
					InfrastructureReport infrastructureReport = new InfrastructureReport();
					infrastructureReport.setTimestamp(timestamp);
					infrastructureReport.setInfrastructure(infrastructure);
					infrastructureReport.setKey(MonitoringService.MEMORY_LABEL);
					infrastructureReport.setValue(infrastructureMetricsMem.get(key));
					infrastructureReportRepository.save(infrastructureReport);
				}
				
				List<Node> nodeList = nodeRepository.findAllNodesByInfrastructureId(infrastructure.getId());
	
				
				//INFRA CPU per NODE
				Map<String, Double> nodeMetricsCpu = getFormattedMetricCPU(getQueryMetricsMap(endpoint, QUERY_CPU_NODE, "node"));
				for(String nodeName : nodeMetricsCpu.keySet()) {
					
					Node nodeFound = null;
					for(Node nodeDB : nodeList) {
						if(nodeDB.getName().equals(nodeName)) {
							nodeFound = nodeDB;
							break;
						}
					}
					if(nodeFound != null) {
						log.info("Prometheus found a node " + nodeFound.getName());
						NodeReport nodeReport = new NodeReport();
						nodeReport.setTimestamp(timestamp);
						nodeReport.setNode(nodeFound);
						nodeReport.setCategory(RESOURCE_USED);
						nodeReport.setKey(MonitoringService.CPU_LABEL);
						nodeReport.setValue(nodeMetricsCpu.get(nodeName));
						nodeReportRepository.save(nodeReport);
					}
				}
	
				//INFRA MEM per NODE
				Map<String, Double> nodeMetricsMem = getFormattedMetricMEM(getQueryMetricsMap(endpoint, QUERY_MEM_NODE, "node"));
				for(String nodeName : nodeMetricsMem.keySet()) {
					
					Node nodeFound = null;
					for(Node nodeDB : nodeList) {
						if(nodeDB.getName().equals(nodeName)) {
							nodeFound = nodeDB;
							break;
						}
					}
					if(nodeFound != null) {
						log.info("Prometheus found a node " + nodeFound.getName());
						NodeReport nodeReport = new NodeReport();
						nodeReport.setTimestamp(timestamp);
						nodeReport.setNode(nodeFound);
						nodeReport.setCategory(RESOURCE_USED);
						nodeReport.setKey(MonitoringService.MEMORY_LABEL);
						nodeReport.setValue(nodeMetricsMem.get(nodeName));
						nodeReportRepository.save(nodeReport);
					}
				}
				
				
				Map<String, ServiceProvider> namespaceMap = new HashMap<String, ServiceProvider>();
	
				for(Project project : projectRepository.getProjectListByInfrastructureId(infrastructure.getId())) {
					ServiceProvider serviceProvider = project.getServiceProvider();
					
					String namespace = null;
					if(project.getProperties() != null) {
		    			JSONObject jsonObject = new JSONObject(project.getProperties());
		    			namespace = jsonObject.getString("namespace");
					}
					if(namespace != null) {
						log.info("Prometheus found a namespace " + namespace);
						namespaceMap.put(namespace, serviceProvider);
					}
				}
	
				//ServiceProvider deployment consumption: serviceProviderName=k->deployName=x->cpu=y,memory=z
				Map<ServiceProvider, Map<String, Map<String, BigDecimal>>> serviceProviderDeploymentConsumptionMap = new HashMap<ServiceProvider, Map<String, Map<String, BigDecimal>>>();
	
				for(String namespace : namespaceMap.keySet()) {
					ServiceProvider serviceProvider = namespaceMap.get(namespace);
					Map<String, Map<String, BigDecimal>> deploymentConsumptionMap = new HashMap<String, Map<String, BigDecimal>>();
					serviceProviderDeploymentConsumptionMap.put(serviceProvider, deploymentConsumptionMap);
	
					String queryCPU = QUERY_CPU_POD.replace("NAMESPACE", namespace);
					Map<String, BigDecimal> nodeMetricsPodCPU = getQueryMetricsMap(endpoint, queryCPU, "pod");
					String queryMEM = QUERY_MEM_POD.replace("NAMESPACE", namespace);
					Map<String, BigDecimal> nodeMetricsPodMEM = getQueryMetricsMap(endpoint, queryMEM, "pod");
					
					for(Service service : serviceRepository.getServiceListByServiceProviderId(serviceProvider.getId())) {
						String serviceName = service.getName();					
						for(String pod : nodeMetricsPodCPU.keySet()) {
							if(pod.startsWith(serviceName + "-")) {
								if(!deploymentConsumptionMap.containsKey(serviceName)) {
									deploymentConsumptionMap.put(serviceName, new HashMap<String, BigDecimal>());
								}
								Map<String, BigDecimal> deploymentConsumption = deploymentConsumptionMap.get(serviceName);
								
								if(!deploymentConsumption.containsKey(MonitoringService.CPU)) {
									deploymentConsumption.put(MonitoringService.CPU, nodeMetricsPodCPU.get(pod));
								}
								else {
									deploymentConsumption.get(MonitoringService.CPU).add(nodeMetricsPodCPU.get(pod));
								}
							}
						}
						
						
						for(String pod : nodeMetricsPodMEM.keySet()) {
							if(pod.startsWith(serviceName + "-")) {
								if(!deploymentConsumptionMap.containsKey(serviceName)) {
									deploymentConsumptionMap.put(serviceName, new HashMap<String, BigDecimal>());
								}
								Map<String, BigDecimal> deploymentConsumption = deploymentConsumptionMap.get(serviceName);
								
								if(!deploymentConsumption.containsKey(MonitoringService.MEMORY)) {
									deploymentConsumption.put(MonitoringService.MEMORY, nodeMetricsPodMEM.get(pod));
								}
								else {
									deploymentConsumption.get(MonitoringService.MEMORY).add(nodeMetricsPodMEM.get(pod));
								}
							}
						}
					}
				}
	
	
				//time to build some report: InfrastructureReport tied to ServiceProvider and ServiceReport
				Map<ServiceProvider, Map<String, BigDecimal>> serviceProviderConsumptionMap = new HashMap<ServiceProvider, Map<String, BigDecimal>>();
				for(ServiceProvider serviceProvider : serviceProviderDeploymentConsumptionMap.keySet()) {
					Map<String, BigDecimal> serviceProviderConsumption = new HashMap<String, BigDecimal>();
					serviceProviderConsumptionMap.put(serviceProvider, serviceProviderConsumption);
	
					Map<String, Map<String, BigDecimal>> deploymentConsumptionMap = serviceProviderDeploymentConsumptionMap.get(serviceProvider);
					for(String deploymentName : deploymentConsumptionMap.keySet()) {
	
						Service serviceFound = null;
						for(Service service : serviceRepository.getServiceListByServiceProviderId(serviceProvider.getId())) {
							String serviceName = service.getName();
							if(serviceName.equals(deploymentName)) {
								log.info("Prometheus found a Service " + serviceName + " matching a Deployment");
	
								serviceFound = service;
								break;
							}
						}
						if(serviceFound != null && serviceFound.getStatus().equals(ExecStatus.RUNNING)) {
							String appGroup = serviceProvider.getName() + " on " + infrastructure.getName();
	
							Map<String, BigDecimal> deploymentConsumption = deploymentConsumptionMap.get(deploymentName);
							for(String key : deploymentConsumption.keySet()) {
								BigDecimal value = deploymentConsumption.get(key);
								if(!serviceProviderConsumption.containsKey(key)) {
									serviceProviderConsumption.put(key, value);
								}
								else {
									serviceProviderConsumption.get(key).add(value);
								}
							}
							Map<String, Double> formattedMetricApp = getFormattedMetric(deploymentConsumption);
							for(String key : formattedMetricApp.keySet()) {
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
		}catch(Exception e) {
			log.error("PrometheusReader", e);
			saveErrorEvent("PrometheusReader error " + e.getClass() + " " + e.getMessage());
		}

	}
}
