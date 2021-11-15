package eu.pledgerproject.confservice.monitoring;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Guarantee;

@Component
public class PrometheusRuleGenerator {

	private static final String HEADER = ""
	+ "apiVersion: monitoring.coreos.com/v1\n"
	+ "kind: PrometheusRule\n"
	+ "metadata:\n"
	+ "  labels:\n"
	+ "    release: prometheus\n"
	+ "  name: SERVICE_NAME_PLACEHOLDER-rules\n"
	+ "  namespace: monitoring\n"
	+ "spec:\n"
	+ "  groups:\n"
	+ "  - name: guaranteeID_GUARANTEE_ID_PLACEHOLDER\n"
	+ "    rules:\n"
	+"";
	
	private static final String ALERT = ""
	+ "    - alert: CONSTRAINT_PLACEHOLDER SEVERITY_PLACEHOLDER\n"
	+ "      expr: RULE_PLACEHOLDER THRESHOLD_PLACEHOLDER\n"
	+ "      for: 0s\n"
	+ "      labels:\n"
	+ "        guarantee: guaranteeID_GUARANTEE_ID_PLACEHOLDER\n"
	+ "        severity: SEVERITY_PLACEHOLDER\n"
	+ "        namespace: SERVICE_NAMESPACE_PLACEHOLDER\n"
	+ "      annotations:\n"
	+ "        summary: \"SUMMARY_PLACEHOLDER is SEVERITY_PLACEHOLDER\"\n"
	+ "        description: \"VALUE = {{ $value }}\\n  LABELS = {{ $labels }}\"\n"
	+ "";
	
	
	public String generate(Guarantee guarantee, String namespace) {
		String result = null;
		
		if(guarantee.getSla() != null) {
			result = HEADER
				.replace("SERVICE_NAME_PLACEHOLDER", guarantee.getSla().getService().getName())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"");
			
			if(guarantee.getThresholdMild() != null) {
				result += ALERT
				.replace("CONSTRAINT_PLACEHOLDER", guarantee.getName())
				.replace("RULE_PLACEHOLDER", guarantee.getConstraint())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"")
				.replace("SERVICE_NAMESPACE_PLACEHOLDER", namespace)
				.replace("SUMMARY_PLACEHOLDER", guarantee.getName())
				.replace("THRESHOLD_PLACEHOLDER", guarantee.getThresholdMild())
				.replace("SEVERITY_PLACEHOLDER", "Mild");
			}
			if(guarantee.getThresholdSerious() != null) {
				result += ALERT
				.replace("CONSTRAINT_PLACEHOLDER", guarantee.getName())
				.replace("RULE_PLACEHOLDER", guarantee.getConstraint())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"")
				.replace("SERVICE_NAMESPACE_PLACEHOLDER", namespace)
				.replace("SUMMARY_PLACEHOLDER", guarantee.getName())
				.replace("THRESHOLD_PLACEHOLDER", guarantee.getThresholdSerious())
				.replace("SEVERITY_PLACEHOLDER", "Serious");
			}
			if(guarantee.getThresholdSevere() != null) {
				result += ALERT
				.replace("CONSTRAINT_PLACEHOLDER", guarantee.getName())
				.replace("RULE_PLACEHOLDER", guarantee.getConstraint())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"")
				.replace("SERVICE_NAMESPACE_PLACEHOLDER", namespace)
				.replace("SUMMARY_PLACEHOLDER", guarantee.getName())
				.replace("THRESHOLD_PLACEHOLDER", guarantee.getThresholdSevere())
				.replace("SEVERITY_PLACEHOLDER", "Severe");
			}
			if(guarantee.getThresholdCatastrophic() != null) {
				result += ALERT
				.replace("CONSTRAINT_PLACEHOLDER", guarantee.getName())
				.replace("RULE_PLACEHOLDER", guarantee.getConstraint())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"")
				.replace("SERVICE_NAMESPACE_PLACEHOLDER", namespace)
				.replace("SUMMARY_PLACEHOLDER", guarantee.getName())
				.replace("THRESHOLD_PLACEHOLDER", guarantee.getThresholdCatastrophic())
				.replace("SEVERITY_PLACEHOLDER", "Catastrophic");
			}
			if(guarantee.getThresholdWarning() != null) {
				result += ALERT
				.replace("CONSTRAINT_PLACEHOLDER", guarantee.getName())
				.replace("RULE_PLACEHOLDER", guarantee.getConstraint())
				.replace("GUARANTEE_ID_PLACEHOLDER", guarantee.getId()+"")
				.replace("SERVICE_NAMESPACE_PLACEHOLDER", namespace)
				.replace("SUMMARY_PLACEHOLDER", guarantee.getName())
				.replace("THRESHOLD_PLACEHOLDER", guarantee.getThresholdWarning())
				.replace("SEVERITY_PLACEHOLDER", "Warning");
			}
		}
		return result;
	}
}
