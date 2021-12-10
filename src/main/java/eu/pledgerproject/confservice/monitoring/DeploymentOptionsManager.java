package eu.pledgerproject.confservice.monitoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.github.sidhant92.boolparser.application.BooleanExpressionEvaluator;

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceConstraint;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.repository.ServiceConstraintRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@Component
public class DeploymentOptionsManager {

	private final AppRepository appRepository; 
	private final ServiceRepository serviceRepository;
	private final ServiceConstraintRepository serviceConstraintRepository;
	private final NodeRepository nodeRepository;
	private final ResourceDataReader resourceDataReader;
	
	public DeploymentOptionsManager(AppRepository appRepository, ServiceRepository serviceRepository, ServiceConstraintRepository serviceConstraintRepository, NodeRepository nodeRepository, ResourceDataReader resourceDataReader) {
		this.appRepository = appRepository;
		this.serviceRepository = serviceRepository;
		this.serviceConstraintRepository = serviceConstraintRepository;
		this.nodeRepository = nodeRepository;
		this.resourceDataReader = resourceDataReader; 
	}
	
	public String getAppDeploymentOptionsString(App app) {
		return getAppDeploymentOptionsText(app);
	}
	
	public String getAppDeploymentOptionsText(App app) {
		StringBuilder result = new StringBuilder();
		result.append("\n app: " + app.getName());
		
		Map<Service, SortedMap<Integer, Set<Node>>> appDeploymentOptions = getAppDeploymentOptions(app.getId());
		for(Service service : appDeploymentOptions.keySet()) {
			result.append("\n\n   service: " + service.getName());
			int counter = 1;
			for(int priority : appDeploymentOptions.get(service).keySet()) {
				result.append("\n   - ranking " + counter++ + " : service deployed on");
				for(Node node : appDeploymentOptions.get(service).get(priority)) {
					String nodeType = ConverterJSON.convertToMap(node.getProperties()).get(NodeGroup.NODE_TYPE);
					
					result.append("\n     -- node#" + node.getId()+ " '" + node.getName() + "' ["+nodeType+"] on infrastructure '" + node.getInfrastructure().getName()+"' with id:" + node.getInfrastructure().getId());
				}
			}
		}
		
		return result.toString(); 
	}
	
	public String getAppDeploymentOptionsJSON(App app) {
		JSONObject result = new JSONObject();
		result.put("app", app.getName());
		result.put("services", new JSONArray());
		
		Map<Service, SortedMap<Integer, Set<Node>>> appDeploymentOptions = getAppDeploymentOptions(app.getId());
		int serviceCount=0;
		for(Service service : appDeploymentOptions.keySet()) {
			JSONObject serviceJSON = new JSONObject();
			result.getJSONArray("services").put(serviceCount++, serviceJSON);
			
			serviceJSON.put("service", service.getName());
			serviceJSON.put("options", new JSONArray());
			
			int rankingCount = 0;
			for(int priority : appDeploymentOptions.get(service).keySet()) {
				JSONObject optionJSON = new JSONObject();
				serviceJSON.getJSONArray("options").put(rankingCount, optionJSON);
				
				optionJSON.put("ranking", ++rankingCount);
				optionJSON.put("nodes", new JSONArray());
				
				int nodeCount = 0;
				for(Node node : appDeploymentOptions.get(service).get(priority)) {
					JSONObject nodeJSON = new JSONObject();
					nodeJSON.put("node", node.getName());
					nodeJSON.put("infrastructure", node.getInfrastructure().getName());
					optionJSON.getJSONArray("nodes").put(nodeCount++, nodeJSON);
				}
			}
		}
		
		return result.toString(); 
	}
	
	
	public Map<App, Map<Service, SortedMap<Integer, Set<Node>>>> getAppsDeploymentOptionsMap() {
		Map<App, Map<Service, SortedMap<Integer, Set<Node>>>> result = new HashMap<App, Map<Service, SortedMap<Integer, Set<Node>>>>();
		
		for(App app : appRepository.findAll()) {
			
			Map<Service, SortedMap<Integer, Set<Node>>> appDeploymentOptionsMap = getAppDeploymentOptions(app.getId());
			result.put(app, appDeploymentOptionsMap);
		}
		return result;
	}

	
	//for a Service, it produces a SortedMap (key is the priority in reverse, 0 is max, 1, 2, etc.) of a List of equivalent nodes where the service can be deployed. 
	public SortedMap<Integer, Set<Node>> getServiceDeploymentOptions(Long serviceId) {
		SortedMap<Integer, Set<Node>> result = new TreeMap<Integer, Set<Node>>();
		
		Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
		if(serviceOptional.isPresent()) {
			Service service = serviceOptional.get();
			result = getAppDeploymentOptions(service.getApp().getId()).get(service);
		}
			
		return result;
	}
	
	public int getRankingInDeploymentOptions(Long serviceId) {
		
		Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
		if(serviceOptional.isPresent()) {
			Node currentNode = resourceDataReader.getCurrentNode(serviceOptional.get());

			if(currentNode != null) {
				SortedMap<Integer, Set<Node>> serviceDeploymentOptions = getServiceDeploymentOptions(serviceId);
				for(int ranking : serviceDeploymentOptions.keySet()) {
					Set<Node> candidateNodeSet = serviceDeploymentOptions.get(ranking);
					if(candidateNodeSet.contains(currentNode)){
						return ranking;
					}
				}
			}
		}
		
		return -1;
	}
	
	public Set<Node> getCurrentNodeSet(Long serviceId) {
		
		Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
		if(serviceOptional.isPresent()) {
			Node currentNode = resourceDataReader.getCurrentNode(serviceOptional.get());

			if(currentNode != null) {
				SortedMap<Integer, Set<Node>> serviceDeploymentOptions = getServiceDeploymentOptions(serviceId);
				for(int ranking : serviceDeploymentOptions.keySet()) {
					Set<Node> candidateNodeSet = serviceDeploymentOptions.get(ranking);
					if(candidateNodeSet.contains(currentNode)){
						return candidateNodeSet;
					}
				}
			}
		}
		
		return null;
	}
	
	
	private Map<Service, SortedMap<Integer, Set<Node>>> getAppDeploymentOptions(Long appId) {
		Map<Service, SortedMap<Integer, Set<Node>>> result = new HashMap<Service, SortedMap<Integer, Set<Node>>>();
		
		for(Service service : serviceRepository.findAllByAppId(appId)) {
			if(!result.containsKey(service)) {
				result.put(service, new TreeMap<Integer, Set<Node>>());
			}
			
			for(ServiceConstraint serviceConstraint : serviceConstraintRepository.findByServiceConstraintListByServiceIdAndRuleCategoryOrderedByPriority(service.getId())) {
				Set<Node> candidateNodeSet = new HashSet<Node>();
				for(Node node : nodeRepository.findAll()) {
					if(isMatching(node, serviceConstraint)) {
						candidateNodeSet.add(node);
					}
				}
				if(!result.get(service).containsKey(serviceConstraint.getPriority())){
					result.get(service).put(serviceConstraint.getPriority(), new HashSet<Node>());
				}
				result.get(service).get(serviceConstraint.getPriority()).addAll(candidateNodeSet);
			}
			
		}
		return result;
	}	
	
	private boolean isMatching(Node node, ServiceConstraint serviceConstraint) {
		BooleanExpressionEvaluator booleanExpressionEvaluator = new BooleanExpressionEvaluator();
		Map<String, Object> data = new HashMap<>();
		
		Map<String, String> infrastructureProperties = ConverterJSON.convertToMap(node.getInfrastructure().getProperties());
		Map<String, String> nodeFeatures = ConverterJSON.convertToMap(node.getFeatures());
		Map<String, String> nodeProperties = ConverterJSON.convertToMap(node.getProperties());
		
		data.putAll(infrastructureProperties);
		data.putAll(nodeFeatures);
		data.putAll(nodeProperties);
		
		Optional<Boolean> result = booleanExpressionEvaluator.evaluate(serviceConstraint.getValue(), data);
		return result.isPresent() && result.get();
	}
}
