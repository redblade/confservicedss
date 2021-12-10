package eu.pledgerproject.confservice.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.NodeGroup;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.service.NetworkService;

/**
 * Service Implementation for managing {@link App}.
 */
@Service
@Transactional
public class NetworkServiceImpl implements NetworkService {

	private static final String NODE = "{\"id\": \"NODE_NAME\", \"group\": NODE_GROUP_ID}";
	private static final String LINK_INFRA_INFRA = "{\"source\": \"INFRASTRUCTURE_SOURCE\", \"target\": \"INFRASTRUCTURE_DEST\", \"value\": 30}";
	private static final String LINK_NODE_INFRA = "{\"source\": \"NODE_SOURCE\", \"target\": \"INFRASTRUCTURE_DEST\", \"value\": 1}";
	
	private InfrastructureRepository infrastructureRepository;
	private NodeRepository nodeRepository;
	
	public NetworkServiceImpl(InfrastructureRepository infrastructureRepository, NodeRepository nodeRepository) {
		this.infrastructureRepository = infrastructureRepository;
		this.nodeRepository = nodeRepository;
	}
	
	private boolean isNodeCloud(Node node) {
		Map<String, String> properties = ConverterJSON.convertToMap(node.getProperties());
		String node_type = properties.get(NodeGroup.NODE_TYPE);
		return node_type != null && node_type.equals(NodeGroup.NODE_CLOUD);
	}
	
	public String getAllInfrastructuresJSON() {
		StringBuilder result = new StringBuilder();
		result.append("{\"nodes\":[");
		
		//1: add all nodes
		for(Node node : nodeRepository.findAll()) {
			result.append(NODE.replace("NODE_NAME", node.getName()).replace("NODE_GROUP_ID",  ""+node.getInfrastructure().getId()) + ",");
		}
		
		//2: add all infrastructures
		for(Infrastructure infrastructure : infrastructureRepository.findAll()) {
			result.append(NODE.replace("NODE_NAME", infrastructure.getName()).replace("NODE_GROUP_ID",  "-1") + ",");
		}
		
		//3: select which infrastructures are "cloud"
		Set<Infrastructure> infrastructureCloudSet = new HashSet<Infrastructure>();
		for(Node node : nodeRepository.findAll()) {
			if(isNodeCloud(node)) {
				infrastructureCloudSet.add(node.getInfrastructure());
			}
		}
		
		result.setLength(result.length()-1);
		result.append(("],\"links\":["));
		
		
		//all infrastructures "clouds" connect to each other
		List<Infrastructure> infrastructureCloudList = new ArrayList<Infrastructure>(infrastructureCloudSet);
		for(int i=0; i<infrastructureCloudList.size(); i++) {
			for(int j=i+1; j<infrastructureCloudList.size(); j++) {
				result.append(
					LINK_INFRA_INFRA
					.replace("INFRASTRUCTURE_SOURCE", infrastructureCloudList.get(i).getName())
					.replace("INFRASTRUCTURE_DEST", infrastructureCloudList.get(j).getName())
				+",");
			}
		}
		//all infrastructures "edge" connect to all "cloud"
		List<Infrastructure> infrastructureEdgeList = new ArrayList<Infrastructure>(infrastructureRepository.findAll());
		infrastructureEdgeList.removeAll(infrastructureCloudList);
		for(Infrastructure infrastructureEdge : infrastructureEdgeList) {
			for(Infrastructure infrastructureCloud : infrastructureCloudList) {
				result.append(
					LINK_INFRA_INFRA
					.replace("INFRASTRUCTURE_SOURCE", infrastructureEdge.getName())
					.replace("INFRASTRUCTURE_DEST", infrastructureCloud.getName())
				+",");
			}
		}
		
		//all nodes connect to its own infrastructure
		for(Node node : nodeRepository.findAll()) {
			result.append(
					LINK_NODE_INFRA
					.replace("NODE_SOURCE", node.getName())
					.replace("INFRASTRUCTURE_DEST", node.getInfrastructure().getName())
				+",");
		}
		
		//all nodes connect to other cloud infrastructures
		for(Node node : nodeRepository.findAll()) {
			if(!isNodeCloud(node)) {
				for(Infrastructure infrastructure : infrastructureCloudSet) {
					if(infrastructure.getId() != node.getInfrastructure().getId() ) {
						result.append(
								LINK_NODE_INFRA
								.replace("NODE_SOURCE", node.getName())
								.replace("INFRASTRUCTURE_DEST", node.getInfrastructure().getName())
							+",");
					}
				}
			}
		}

		
		
		result.setLength(result.length()-1);
		result.append("]}");
		return result.toString();
        		
    }


}
