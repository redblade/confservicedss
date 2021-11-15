package eu.pledgerproject.confservice.scheduler;

public class DescriptorParserKubernetes {

	public static final String PLACEHOLDER_NAMESPACE = "PLACEHOLDER_NAMESPACE";
	public static final String PLACEHOLDER_HOSTNAME = "PLACEHOLDER_HOSTNAME";
	public static final String PLACEHOLDER_REPLICAS = "PLACEHOLDER_REPLICAS";
	public static final String PLACEHOLDER_CPU_MILLICORE = "PLACEHOLDER_CPU_MILLICORE";
	public static final String PLACEHOLDER_MEMORY_MB = "PLACEHOLDER_MEMORY_MB";

	public static String getHostnameVectorString(String nodeStringCSV) {
		StringBuilder result = new StringBuilder();

		String[] nodeVector = nodeStringCSV.split(",");
		result.append(nodeVector[0] + "\n");
		for(int i=1; i<nodeVector.length; i++) {
			result.append("                - " + nodeVector[i] + "\n");
		}
		
		return result.toString();
	}
	
	public static String parseDeploymentDescriptor(String deploymentDescriptor, String namespace, String nodeStringCSV, String replicas) {
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_NAMESPACE, namespace);
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_REPLICAS, replicas);
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_HOSTNAME, getHostnameVectorString(nodeStringCSV));
		return deploymentDescriptor;
	}
	
	public static String parseDeploymentDescriptor(String deploymentDescriptor, String namespace, String requestCpuMillicore, String requestMemMB, String nodeStringCSV, String replicas) {
		deploymentDescriptor = parseDeploymentDescriptor(deploymentDescriptor, namespace, nodeStringCSV, replicas);
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_CPU_MILLICORE, requestCpuMillicore);
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_MEMORY_MB, requestMemMB);
		return deploymentDescriptor;
	}
	
}
