package eu.pledgerproject.confservice.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ConverterJSON {

	
	public static Map<String, String> convertToMap(String source) {
		Map<String, String> result = new HashMap<String, String>();
		
		if(source != null && source.trim().length() > 0) {
			JSONObject jsonObject = new JSONObject(source);
			for(String key : jsonObject.keySet()) {
				result.put(key, jsonObject.get(key).toString());
			}
		}
		
		return result;
	}
	
	public static String convertToJSON(Map<String, String> map) {
		JSONObject jsonObject = new JSONObject();
		for(String key : map.keySet()) {
			jsonObject.put(key, map.get(key));
		}
		
		return jsonObject.toString();
	}
	
	public static String getProperty(String source, String key) {
		String result = "";
		Map<String, String> map = convertToMap(source);
		if(map.containsKey(key)) {
			result = map.get(key);
		}
		return result;
	}
	
	public static int getPropertyInt(String source, String key, int defaultValue) {
		int result = defaultValue;
		Map<String, String> map = convertToMap(source);
		if(map.containsKey(key)) {
			result = Integer.parseInt(map.get(key));
		}
		return result;
	}
	
}
