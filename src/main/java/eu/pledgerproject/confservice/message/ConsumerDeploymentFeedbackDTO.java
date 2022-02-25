package eu.pledgerproject.confservice.message;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;
import eu.pledgerproject.confservice.message.dto.DeploymentFeedbackDTO;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;

@Repository
public class ConsumerDeploymentFeedbackDTO { 
	
    private static final Logger log = LoggerFactory.getLogger(ConsumerDeploymentFeedbackDTO.class);
    
    private ServiceRepository serviceRepository;
    private AppRepository appRepository;
    
    public ConsumerDeploymentFeedbackDTO(ServiceRepository serviceRepository, AppRepository appRepository) {
    	this.serviceRepository = serviceRepository;
    	this.appRepository = appRepository;

    }
    
    @KafkaListener(topics = "deployment_feedback", groupId = "id", containerFactory = "deploymentFeedbackDTOListener") 
    public void consume(DeploymentFeedbackDTO message) { 
    	log.info("New DeploymentFeedbackDTO received: " + message); 
    	
    	Long serviceID = message.id;
    	String status = message.status;
    	if(status != null && !status.isEmpty()) {
	    	Optional<Service> serviceOptional = serviceRepository.findById(serviceID);
	    	if(serviceOptional.isPresent()) {
	    		Service service = serviceOptional.get();
	    		if(service.getApp().getManagementType().equals(ManagementType.DELEGATED)) {
	    			ExecStatus serviceStatus = null;
	    			if(status.equals("DEPLOYED")) {
	    				serviceStatus = ExecStatus.RUNNING;
	    			}
	    			else if(status.equals("ERROR")) {
	    				serviceStatus = ExecStatus.ERROR;
	    			}
	    			else if(status.equals("STOPPED")) {
	    				serviceStatus = ExecStatus.STOPPED;
	    			}
	    			if(serviceStatus != null) {
		    			service.setStatus(serviceStatus);
		    			serviceRepository.save(service);
		    			if(serviceStatus.equals(ExecStatus.ERROR)) {
		    				App app = service.getApp();
		    				app.setStatus(serviceStatus);
		    				appRepository.save(app);
		    			}
	    			}
	    		}
	    	}
    	}
    }
    		
} 