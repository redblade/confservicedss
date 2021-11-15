package eu.pledgerproject.confservice.scheduler;

public class DescriptorParserDockerCompose {

	public static final String PLACEHOLDER_RESOURCES = "PLACEHOLDER_RESOURCES:";

	public static final String TEMPLATE_RESOURCES = ""
	+       "resources:\n"
	+ "        requests:\n"
	+ "          cpus: MY_CPU\n"
	+ "          memory: MY_MEMORYM\n"
	+ "        limits:\n"
	+ "          cpus: MY_CPU\n"
	+ "          memory: MY_MEMORYM\n"
	+ "";
	
	
	public static String parseDeploymentDescriptor(String deploymentDescriptor, String requestCpuMillicore, String requestMemMB) {
		
		String resources = TEMPLATE_RESOURCES.replace("MY_CPU", requestCpuMillicore).replace("MY_MEMORY", requestMemMB);
		deploymentDescriptor = deploymentDescriptor.replace(PLACEHOLDER_RESOURCES, resources);
		
		return deploymentDescriptor;
	}
	
}
