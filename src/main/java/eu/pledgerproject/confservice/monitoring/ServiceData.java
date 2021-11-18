package eu.pledgerproject.confservice.monitoring;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;

public class ServiceData implements Comparable<ServiceData>{
	public static final double ROUND = 100;
	
	Service service;
	int priority;
	Double score;

	Node currentNode;
	int requestMemoryMB;
	int requestCpuMillicore;
	
	boolean requestsAreChanged;

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
		return (int) (this.score * ROUND) - (int) (other.score * ROUND);
	}
	
	public String toString() {
		return "Service " + service.getName() + " currently on Node " + currentNode.getName() + " with score " + score;
	}
}