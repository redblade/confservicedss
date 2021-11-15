package eu.pledgerproject.confservice.test;

import java.io.File;
import java.io.FileReader;

import com.coreos.monitoring.models.V1PrometheusRule;
import com.coreos.monitoring.models.V1PrometheusRuleList;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.generic.GenericKubernetesApi;

public class PrometheusRule {

	public static void main(String[] args) {
		ApiClient client = null;

		try(FileReader fileReader = new FileReader(args[0])){
			client = Config.fromConfig(fileReader);
			
			Configuration.setDefaultApiClient(client);
			
			GenericKubernetesApi<V1PrometheusRule, V1PrometheusRuleList> prometheusApi =
			        new GenericKubernetesApi<>(
			        		V1PrometheusRule.class,
			        		V1PrometheusRuleList.class,
			            "monitoring.coreos.com",
			            "v1",
			            "prometheuses",
			            client);
			V1PrometheusRule rule = Yaml.loadAs(new File(args[1]), V1PrometheusRule.class);
			prometheusApi.create(rule).throwsApiException();
				
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
