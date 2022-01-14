package eu.pledgerproject.confservice.optimisation;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;

public class ServiceData implements Comparable<ServiceData>{
	public static final double ROUND = 100;
	
	public Service service;
	public int priority;
	public Double score;

	public Node currentNode;
	public int requestMemoryMB;
	public int requestCpuMillicore;
	
	public boolean requestsAreChanged;

	public ServiceData(Service service, int requestCpuMillicoreNew, int requestMemoryMBNew){
		this.service = service;
		this.priority = service.getPriority();
		
		int requestCpuMillicoreOriginal = ResourceDataReader.getServiceRuntimeCpuRequest(service);
		int requestMemoryMBOriginal = ResourceDataReader.getServiceRuntimeMemRequest(service);

		this.requestsAreChanged = requestCpuMillicoreOriginal != requestCpuMillicoreNew || requestMemoryMBOriginal != requestMemoryMBNew;
		
		this.requestCpuMillicore = requestCpuMillicoreNew;
		this.requestMemoryMB = requestMemoryMBNew;
	}
	
	public int compareTo(ServiceData other) {
		return this.score == other.score ? 0 : this.score > other.score ? 1 : -1;
	}
	
	public String toString() {
		return "Service " + service.getName() + " currently on Node " + currentNode.getName() + " with score " + score;
	}
}