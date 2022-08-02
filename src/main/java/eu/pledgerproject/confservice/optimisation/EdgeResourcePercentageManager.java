package eu.pledgerproject.confservice.optimisation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;

@Component
public class EdgeResourcePercentageManager {
	private Map<String, EdgeResourcePercentage> mapEdgeResourcePercentage;
	private final ServiceProviderRepository serviceProviderRepository;

	
	public EdgeResourcePercentageManager(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
		this.mapEdgeResourcePercentage = new HashMap<String, EdgeResourcePercentage>();
	}
	
	private void createIfNotExistsUsingValueFromServiceProviderProperties(ServiceProvider serviceProvider) {
		if(!mapEdgeResourcePercentage.containsKey(serviceProvider.getName())) {
			Map<String, String> serviceProviderPreferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
			if(serviceProviderPreferences.containsKey("edge.max.percentage")) {
				int value = Integer.parseInt(serviceProviderPreferences.get("edge.max.percentage"));
				mapEdgeResourcePercentage.put(serviceProvider.getName(), new EdgeResourcePercentage(value));
			}
			else {
				int defaultValue = EAECODAHelper.MAX_PERCENTAGE;
				serviceProviderPreferences.put("edge.max.percentage", ""+defaultValue);
				String jsonPreferences = ConverterJSON.convertToJSON(serviceProviderPreferences);
				serviceProvider.setPreferences(jsonPreferences);
				serviceProviderRepository.save(serviceProvider);
				
				mapEdgeResourcePercentage.put(serviceProvider.getName(), new EdgeResourcePercentage(defaultValue));
			}
		}
	}
	
	private void saveOnServiceProviderProperties(ServiceProvider serviceProvider, int value) {
		if(value < EAECODAHelper.MIN_PERCENTAGE || value > EAECODAHelper.MAX_PERCENTAGE) {
			value = EAECODAHelper.MAX_PERCENTAGE;
		}
		mapEdgeResourcePercentage.get(serviceProvider.getName()).setValue(value);

		Map<String, String> serviceProviderPreferences = ConverterJSON.convertToMap(serviceProvider.getPreferences());
		serviceProviderPreferences.put("edge.max.percentage", ""+value);
		String jsonPreferences = ConverterJSON.convertToJSON(serviceProviderPreferences);
		serviceProvider.setPreferences(jsonPreferences);
		serviceProviderRepository.save(serviceProvider);
		
	}
		
	int getValue(ServiceProvider serviceProvider) {
		createIfNotExistsUsingValueFromServiceProviderProperties(serviceProvider);
		return mapEdgeResourcePercentage.get(serviceProvider.getName()).value;
	}
	void setValue(ServiceProvider serviceProvider, int value) {
		createIfNotExistsUsingValueFromServiceProviderProperties(serviceProvider);
		saveOnServiceProviderProperties(serviceProvider, value);
	}
	boolean isValueChanged(ServiceProvider serviceProvider) {
		createIfNotExistsUsingValueFromServiceProviderProperties(serviceProvider);
		return mapEdgeResourcePercentage.get(serviceProvider.getName()).changed;
	}
	
	class EdgeResourcePercentage {
		private boolean changed;
		private int value;
		
		EdgeResourcePercentage(int value){
			this.value = value;
		}
		
		int getValue() {
			return this.value;
		}
		boolean isChanged() {
			return changed;
		}
		void setValue(int value) {
			this.changed = this.value != value;
			this.value = value;
		}
	}
}
