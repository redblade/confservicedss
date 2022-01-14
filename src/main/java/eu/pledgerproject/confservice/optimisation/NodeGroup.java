package eu.pledgerproject.confservice.optimisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;

public class NodeGroup {
	public static final String NODE_TYPE = "node_type";
	public static final String NODE_FAREDGE = "faredge";
	public static final String NODE_EDGE= "edge";
	public static final String NODE_CLOUD= "cloud";
	
	public class NodeData {
		
		NodeData(Node node, int capacityCpuMillicore, int capacityMemoryMB) {
			this.node = node;
			this.capacityCpuMillicore = capacityCpuMillicore;
			this.capacityMemoryMB = capacityMemoryMB;
		}
		Node node;
		int capacityCpuMillicore;
		int capacityMemoryMB;
	}
	
	public String name;
	public Infrastructure infrastructure;
	public String location;
	public Set<Node> nodes;
	public List<NodeData> nodesData;
	public int capacityMemoryMB;
	public int capacityCpuMillicore;
	public int availabilityMemoryMB;
	public int availabilityCpuMillicore;
	
	public NodeGroup(String name, Collection<Node> nodes, int capacityCpuMillicore, int capacityMemoryMB){
		this.name = name;
		this.nodes = new HashSet<Node>(nodes);
		this.nodesData = new ArrayList<NodeData>();
		for(Node node : nodes) {
			int nodeCapacityCpuMillicore = ConverterJSON.getPropertyInt(node.getTotalResources(), "cpu_millicore", 0);
			int nodeCapacityMemoryMB = ConverterJSON.getPropertyInt(node.getTotalResources(), "memory_mb", 0);
			
			this.nodesData.add(new NodeData(node, nodeCapacityCpuMillicore, nodeCapacityMemoryMB));
		}
		this.infrastructure = nodes != null && nodes.size() > 0 ? nodes.iterator().next().getInfrastructure() : null;
		this.location = "";
		if(nodes != null && nodes.size() > 0) {
			for(Node node : nodes) {
				String nodeType = ConverterJSON.getProperty(node.getProperties(), NODE_TYPE);
				if(nodeType != null) {
					if(this.location == "") {
						this.location = nodeType;
					}
					else if(!this.location.equals(nodeType)) {
						this.location = "hybrid";
					}
				}
			}
			
		}
		this.capacityMemoryMB = capacityMemoryMB;
		this.capacityCpuMillicore = capacityCpuMillicore;
		
		this.availabilityMemoryMB = capacityMemoryMB;
		this.availabilityCpuMillicore = capacityCpuMillicore;
	}
	
	boolean hostResources(int requestCpuMillicore, int requestMemoryMB) {
		boolean result = false;
		
		if(availabilityCpuMillicore >= requestCpuMillicore && availabilityMemoryMB >= requestMemoryMB) {
			int nodeHostIndex = -1;
			for(int i=0; i<nodesData.size() && nodeHostIndex < 0; i++) {
				if(nodesData.get(i).capacityCpuMillicore >= requestCpuMillicore && nodesData.get(i).capacityMemoryMB >= requestMemoryMB) {
					nodeHostIndex = i;
				}
			}
			if(nodeHostIndex >= 0) {
				availabilityCpuMillicore -= requestCpuMillicore;
				availabilityMemoryMB -= requestMemoryMB;
				nodesData.get(nodeHostIndex).capacityCpuMillicore -= requestCpuMillicore;
				nodesData.get(nodeHostIndex).capacityMemoryMB -= requestMemoryMB;
				result = true;
			}
		}
		
		return result;
	}
	
	public String getNodeCSV() {
		StringBuilder result = new StringBuilder();
		for(Node node : nodes) {
			result.append(node.getName()+",");
		}
		if(result.length() > 0) {
			result.setLength(result.length()-1);
		}
		return result.toString();
	}
	
	public String toString() {
		return "NodeGroup " + name + " with availability/capacity cpu["+availabilityCpuMillicore+"/"+capacityCpuMillicore+"] and mem["+availabilityMemoryMB+"/"+capacityMemoryMB+"], nodes [" + getNodeCSV() + "]";
	}
}