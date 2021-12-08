package eu.pledgerproject.confservice.monitoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.service.UserService;

/**
 * Integration tests for {@link UserService}.
 */

public class EcodaHelperIT {
    private static final Logger log = LoggerFactory.getLogger(EcodaHelperIT.class);
	
    @Test
    public void testServiceOptimisedAllocationPlanScenario1() {
        List<ServiceData> serviceDataList = new ArrayList<ServiceData>();
        
        Node cluster1_worker = new Node(); cluster1_worker.setName("cluster1-worker");  cluster1_worker.setTotalResources("{'cpu_millicore':  '6000','memory_mb': '6000'}");
        Node cluster1_worker2 = new Node();cluster1_worker2.setName("cluster1-worker2");cluster1_worker2.setTotalResources("{'cpu_millicore':  '300','memory_mb':  '300'}");
        Node cluster1_worker3 = new Node();cluster1_worker3.setName("cluster1-worker3");cluster1_worker3.setTotalResources("{'cpu_millicore':  '300','memory_mb':  '300'}");
        
        Service service1 = new Service();service1.setName("service1");
        ServiceData serviceData1 = new ServiceData(service1, 250, 250);
        serviceData1.currentNode = cluster1_worker2;
        serviceDataList.add(serviceData1);
        
        Service service2 = new Service();service2.setName("service2");
        ServiceData serviceData2 = new ServiceData(service2, 300, 300);
        serviceData2.currentNode = cluster1_worker3;
        serviceDataList.add(serviceData2);
        
        Service service3 = new Service();service3.setName("service3");
        ServiceData serviceData3 = new ServiceData(service3, 300, 300);
        serviceData3.currentNode = cluster1_worker;
        serviceDataList.add(serviceData3);
        
        Service service4 = new Service();service4.setName("service4");
        ServiceData serviceData4 = new ServiceData(service4, 200, 200);
        serviceData4.currentNode = cluster1_worker;
        serviceDataList.add(serviceData4);
        
        List<NodeGroup> nodeGroupList = new ArrayList<NodeGroup>();
        
        Set<Node> nodeclusterEdge = new HashSet<Node>();
        nodeclusterEdge.add(cluster1_worker2);
        nodeclusterEdge.add(cluster1_worker3);
        
        Set<Node> nodeclusterCloud = new HashSet<Node>();
        nodeclusterCloud.add(cluster1_worker);
        
        NodeGroup nodeGroupEdge = new NodeGroup("edge", nodeclusterEdge, 600, 600);
        NodeGroup nodeGroupCloud = new NodeGroup("cloud", nodeclusterCloud, 5000, 5000);

        nodeGroupList.add(nodeGroupEdge);
        nodeGroupList.add(nodeGroupCloud);
        
        Map<ServiceData, NodeGroup> serviceOptimisationPlan = null;
        serviceData1.score = 1.0;
        serviceData2.score = 2.0;
        serviceData3.score = 3.0;
        serviceData4.score = 4.0;
        log("Initial status", serviceDataList);
        
        //now we change the deployment, lower score are placed on the edge
        serviceData1.score = 2.0;
        serviceData2.score = 3.0;
        serviceData3.score = 1.0;
        serviceData4.score = 4.0;
        Collections.sort(serviceDataList);
        
        serviceOptimisationPlan = ECODAHelper.getServiceOptimisedAllocationPlan(serviceDataList, nodeGroupList);
        assertNotNull(serviceOptimisationPlan);
        assertTrue(serviceOptimisationPlan.size() > 0);
        
        log("deployment plan", serviceOptimisationPlan);
    }
    
    @Test
    public void testServiceOptimisedAllocationPlanScenario2() {
        List<ServiceData> serviceDataList = new ArrayList<ServiceData>();
        
        Node cluster1_worker = new Node(); cluster1_worker.setName("cluster1-worker");  cluster1_worker.setTotalResources("{'cpu_millicore':  '6000','memory_mb': '6000'}");
        Node cluster1_worker2 = new Node();cluster1_worker2.setName("cluster1-worker2");cluster1_worker2.setTotalResources("{'cpu_millicore':  '300','memory_mb':  '300'}");
        Node cluster1_worker3 = new Node();cluster1_worker3.setName("cluster1-worker3");cluster1_worker3.setTotalResources("{'cpu_millicore':  '300','memory_mb':  '300'}");
        
        Service service1 = new Service();service1.setName("service1");
        ServiceData serviceData1 = new ServiceData(service1, 250, 250);
        serviceData1.currentNode = cluster1_worker2;
        serviceDataList.add(serviceData1);
        
        Service service2 = new Service();service2.setName("service2");
        ServiceData serviceData2 = new ServiceData(service2, 330, 330);
        serviceData2.currentNode = cluster1_worker;
        serviceDataList.add(serviceData2);
        
        Service service3 = new Service();service3.setName("service3");
        ServiceData serviceData3 = new ServiceData(service3, 300, 300);
        serviceData3.currentNode = cluster1_worker3;
        serviceDataList.add(serviceData3);
        
        Service service4 = new Service();service4.setName("service4");
        ServiceData serviceData4 = new ServiceData(service4, 200, 200);
        serviceData4.currentNode = cluster1_worker;
        serviceDataList.add(serviceData4);
        
        List<NodeGroup> nodeGroupList = new ArrayList<NodeGroup>();
        
        Set<Node> nodeclusterEdge = new HashSet<Node>();
        nodeclusterEdge.add(cluster1_worker2);
        nodeclusterEdge.add(cluster1_worker3);
        
        Set<Node> nodeclusterCloud = new HashSet<Node>();
        nodeclusterCloud.add(cluster1_worker);
        
        NodeGroup nodeGroupEdge = new NodeGroup("edge", nodeclusterEdge, 600, 600);
        NodeGroup nodeGroupCloud = new NodeGroup("cloud", nodeclusterCloud, 5000, 5000);

        nodeGroupList.add(nodeGroupEdge);
        nodeGroupList.add(nodeGroupCloud);
        
        Map<ServiceData, NodeGroup> serviceOptimisationPlan = null;
        serviceData1.score = 1.0;
        serviceData3.score = 2.0;
        serviceData2.score = 3.0;
        serviceData4.score = 4.0;

        log("Initial status", serviceDataList);
        
        //now we change the deployment, lower score are placed on the edge
        serviceData1.score = 1.0;
        serviceData2.score = 2.0;
        serviceData4.score = 3.0;
        serviceData3.score = 4.0;
        Collections.sort(serviceDataList);
        
        serviceOptimisationPlan = ECODAHelper.getServiceOptimisedAllocationPlan(serviceDataList, nodeGroupList);
        assertNotNull(serviceOptimisationPlan);
        assertTrue(serviceOptimisationPlan.size() > 0);
        
        log("deployment plan", serviceOptimisationPlan);
    }
     
	private void log(String message, List<ServiceData> serviceDataList) {
	 	log.info(message);
	 	for(ServiceData serviceData : serviceDataList) {
	 		log.info("Service " + serviceData.service.getName()+ "["+serviceData.requestCpuMillicore+"#"+serviceData.requestMemoryMB+"] is on " + serviceData.currentNode.getName());
	 	}
	}
	 
	private void log(String message, Map<ServiceData, NodeGroup> plan) {
	 	log.info(message);
	 	for(ServiceData serviceData : plan.keySet()) {
	 		log.info("Service " + serviceData.service.getName()+ "["+serviceData.requestCpuMillicore+"#"+serviceData.requestMemoryMB+"] is moved to " + plan.get(serviceData).name);
	 	}
	}

    
}
