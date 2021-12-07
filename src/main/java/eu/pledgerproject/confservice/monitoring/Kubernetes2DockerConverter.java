package eu.pledgerproject.confservice.monitoring;

import java.io.IOException;
import java.util.Map;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Yaml;

public class Kubernetes2DockerConverter {

	private static final String TEMPLATE = ""+
	"version: \"3.9\"\n"
	+ "services:\n"
	+ "  PLACEHOLDER_SERVICE_NAME:\n"
	+ "    image: PLACEHOLDER_SERVICE_IMAGE\n"
	+ "    deploy:\n"
	+ "      resources:\n"
	+ "        limits:\n"
	+ "          cpus: PLACEHOLDER_CPU_MILLICORE\n"
	+ "          memory: PLACEHOLDER_MEMORY_MB\n"
	+ "        reservations:\n"
	+ "          cpus: PLACEHOLDER_CPU_MILLICORE\n"
	+ "          memory: PLACEHOLDER_MEMORY_MB\n"
	+ "";
	
	public static String getServiceName(String kubernetesYaml) throws IOException {
		V1Deployment deployment = (V1Deployment) Yaml.load(kubernetesYaml);
		String serviceName = deployment.getMetadata().getName();
		return serviceName;
	}
	
	public static String getServiceImage(String kubernetesYaml) throws IOException {
		V1Deployment deployment = (V1Deployment) Yaml.load(kubernetesYaml);
		String serviceImage = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
		return serviceImage;
	}
	
	public static String convert(String kubernetesYaml) throws IOException {
		V1Deployment deployment = (V1Deployment) Yaml.load(kubernetesYaml);

		String serviceName = deployment.getMetadata().getName();
		String serviceImage = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
		Map<String, Quantity> limits = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits();
		String serviceCPU = ""+limits.get("cpu").getNumber();
		String serviceMillicore = ""+limits.get("memory").getNumber();
		
		return TEMPLATE
				.replace("PLACEHOLDER_SERVICE_NAME", serviceName)
				.replace("PLACEHOLDER_SERVICE_IMAGE", serviceImage)
				.replace("PLACEHOLDER_CPU_MILLICORE", serviceCPU)
				.replace("PLACEHOLDER_MEMORY_MB", serviceMillicore);
	}
}
