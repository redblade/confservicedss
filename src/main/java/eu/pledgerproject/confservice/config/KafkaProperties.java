package eu.pledgerproject.confservice.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProperties {
	private final Logger log = LoggerFactory.getLogger(KafkaProperties.class);

    private Map<String, String> consumer = new HashMap<>();
    private Map<String, String> producer = new HashMap<>();
    
    private Map<String, String> properties = new HashMap<>();

    public KafkaProperties() {
    	log.info("Using Kafka bootstrap servers: " + System.getenv().get("KAFKA_BOOTSTRAP_SERVERS"));
    	properties.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
    	if(System.getenv("KAFKA_SECURITY_PROTOCOL") != null){
    		properties.put("security.protocol", System.getenv("KAFKA_SECURITY_PROTOCOL"));
	    	properties.put("ssl.enabled.protocols", System.getenv("KAFKA_ENABLED_PROTOCOL"));
	    	properties.put("ssl.truststore.location", System.getenv("KAFKA_TRUSTSTORE_LOCATION"));
	    	properties.put("ssl.truststore.password", System.getenv("KAFKA_TRUSTSTORE_PASSWORD"));
	    	properties.put("ssl.keystore.location", System.getenv("KAFKA_KEYSTORE_LOCATION"));
	    	properties.put("ssl.keystore.password", System.getenv("KAFKA_KEYSTORE_PASSWORD"));
	    	properties.put("ssl.key.password", System.getenv("KAFKA_KEY_PASSWORD"));
    	}
    	
    	consumer.putAll(properties);
    	consumer.put("key.deserializer", System.getenv("KAFKA_CONSUMER_KEY_DESERIALIZER"));
    	consumer.put("value.deserializer", System.getenv("KAFKA_CONSUMER_VALUE_DESERIALIZER"));

    	producer.putAll(properties);
    	producer.put("key.serializer", System.getenv("KAFKA_PRODUCER_KEY_SERIALIZER"));
    	producer.put("value.serializer", System.getenv("KAFKA_PRODUCER_VALUE_SERIALIZER"));
    	
    	/*
    	for(String key : consumer.keySet()) {
    		System.out.println(key + "-->" + consumer.get(key));
    	}
    	for(String key : producer.keySet()) {
    		System.out.println(key + "-->" + producer.get(key));
    	}
    	*/
    	
    	/*
    	Properties propertiesConsumer = new Properties();
    	propertiesConsumer.load(new FileInputStream("/Users/francesco/Documents/pledger-workspace/demo/src/main/resources/consumer.properties"));
    	for(Object key : propertiesConsumer.keySet()) {
    		String keyString = key.toString();
    		consumer.put(keyString, propertiesConsumer.getProperty(keyString));
    	}
    	
    	Properties propertiesProducer = new Properties();
    	propertiesProducer.load(new FileInputStream("/Users/francesco/Documents/pledger-workspace/demo/src/main/resources/producer.properties"));
    	for(Object key : propertiesProducer.keySet()) {
    		String keyString = key.toString();
    		producer.put(keyString, propertiesProducer.getProperty(keyString));
    	}
    	*/

    }
    
    public Map<String, Object> getConsumerProps() {
        Map<String, Object> properties = new HashMap<>(this.consumer);
        return properties;
    }

    public Map<String, Object> getProducerProps() {
        Map<String, Object> properties = new HashMap<>(this.producer);
        return properties;
    }
}

