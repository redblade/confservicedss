package eu.pledgerproject.confservice.message;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;

import eu.pledgerproject.confservice.config.KafkaProperties;
import eu.pledgerproject.confservice.message.dto.AppProfilerDTO;
import eu.pledgerproject.confservice.message.dto.BenchmarkReportDTO;
import eu.pledgerproject.confservice.message.dto.DeploymentFeedbackDTO;
import eu.pledgerproject.confservice.message.dto.SlaViolationDTO;

@EnableKafka
@Configuration
public class KafkaConsumersConfiguration { 
	
	private KafkaProperties kafkaProperties;
	
	public KafkaConsumersConfiguration(KafkaProperties kafkaProperties) {
		this.kafkaProperties = kafkaProperties;
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
    public ConsumerFactory<String, AppProfilerDTO> appProfilerDTOConsumer() { 
        Map<String, Object> map = kafkaProperties.getConsumerProps();
        return new DefaultKafkaConsumerFactory<String, AppProfilerDTO>(map, new StringDeserializer(), new ErrorHandlingDeserializer2(new DeserializerAppProfilerDTO())); 
    } 
  
    @SuppressWarnings({ "deprecation" })
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, AppProfilerDTO> appProfilerDTOListener() { 
        ConcurrentKafkaListenerContainerFactory<String, AppProfilerDTO> factory = new ConcurrentKafkaListenerContainerFactory<>(); 
        factory.setConsumerFactory(appProfilerDTOConsumer()); 
        factory.setErrorHandler(new SeekToCurrentErrorHandler(1));
        return factory; 
    }
  
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Bean
    public ConsumerFactory<String, BenchmarkReportDTO> benchmarkReportDTOConsumer() { 
        Map<String, Object> map = kafkaProperties.getConsumerProps();
        return new DefaultKafkaConsumerFactory<String, BenchmarkReportDTO>(map, new StringDeserializer(), new ErrorHandlingDeserializer2(new DeserializerBenchmarkReportDTO())); 
    } 
  
    @SuppressWarnings({ "deprecation" })
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BenchmarkReportDTO> benchmarkReportDTOListener() { 
        ConcurrentKafkaListenerContainerFactory<String, BenchmarkReportDTO> factory = new ConcurrentKafkaListenerContainerFactory<>(); 
        factory.setConsumerFactory(benchmarkReportDTOConsumer()); 
        factory.setErrorHandler(new SeekToCurrentErrorHandler(1));
        return factory; 
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Bean
    public ConsumerFactory<String, SlaViolationDTO> slaViolationDTOConsumer() { 
        Map<String, Object> map = kafkaProperties.getConsumerProps();
        return new DefaultKafkaConsumerFactory<String, SlaViolationDTO>(map, new StringDeserializer(), new ErrorHandlingDeserializer2(new DeserializerSlaViolationDTO())); 
    } 
  
    @SuppressWarnings({ "deprecation" })
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SlaViolationDTO> slaViolationDTOListener() { 
        ConcurrentKafkaListenerContainerFactory<String, SlaViolationDTO> factory = new ConcurrentKafkaListenerContainerFactory<>(); 
        factory.setConsumerFactory(slaViolationDTOConsumer()); 
        factory.setErrorHandler(new SeekToCurrentErrorHandler(1));
        return factory; 
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Bean
    public ConsumerFactory<String, DeploymentFeedbackDTO> deploymentFeedbackDTOConsumer() { 
        Map<String, Object> map = kafkaProperties.getConsumerProps();
        return new DefaultKafkaConsumerFactory<String, DeploymentFeedbackDTO>(map, new StringDeserializer(), new ErrorHandlingDeserializer2(new DeserializerDeploymentFeedbackDTO())); 
    } 
  
    @SuppressWarnings({ "deprecation" })
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeploymentFeedbackDTO> deploymentFeedbackDTOListener() { 
        ConcurrentKafkaListenerContainerFactory<String, DeploymentFeedbackDTO> factory = new ConcurrentKafkaListenerContainerFactory<>(); 
        factory.setConsumerFactory(deploymentFeedbackDTOConsumer()); 
        factory.setErrorHandler(new SeekToCurrentErrorHandler(1));
        return factory; 
    } 

}