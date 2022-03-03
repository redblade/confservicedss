package eu.pledgerproject.confservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.message.PublisherOrchestrationUpdate;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.ProjectService;

/**
 * Service Implementation for managing {@link Project}.
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;
    private final PublisherOrchestrationUpdate orchestrationNotifierService;

    public ProjectServiceImpl(ProjectRepository projectRepository, EventRepository eventRepository, PublisherConfigurationUpdate configurationNotifierService, PublisherOrchestrationUpdate orchestrationNotifierService) {
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
        this.configurationNotifierService = configurationNotifierService;
        this.orchestrationNotifierService = orchestrationNotifierService;
   }

    @Override
    public Project save(Project project) {
        log.debug("Request to save Project : {}", project);
        CheckRole.block("ROLE_ROAPI");

        Project result = projectRepository.save(project);
        configurationNotifierService.publish(result.getId(), "project", "update");
        return result;
    }
    
    
    //private static final String REST_TEMPLATE = "{\n  \"k8s_ns_name\": \"PLACEHOLDER_NAMESPACE\",\n  \"k8s_resource_quota_name\": \"PLACEHOLDER_SLICE\",\n  \"limits_cpu\": \"PLACEHOLDER_CPU\",\n  \"limits_memory\": \"PLACEHOLDER_MEMGi\",\n  \"requests_cpu\": \"PLACEHOLDER_CPU\",\n  \"requests_memory\": \"PLACEHOLDER_MEMGi\"\n}";
    
    private void saveErrorEvent(String category, String msg) {
    	if(log.isErrorEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory(category);
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}

    /*
    private void provision_rest(Project project) {
    	String namespace = ConverterJSON.getProperty(project.getProperties(), "namespace");
        String sliceName = ConverterJSON.getProperty(project.getProperties(), "slice_name");
        if(namespace != null && namespace.trim().length() > 0 && sliceName != null && sliceName.trim().length() > 0) {
        	String soeEndpoint = ConverterJSON.getProperty(project.getInfrastructure().getMonitoringPlugin(), "soe_endpoint");
        	if(soeEndpoint != null) {
        		log.info("Provisioning SOE slice via REST");
        		try {
	        		OkHttpClient client = new OkHttpClient().newBuilder().build();
	        		MediaType mediaType = MediaType.parse("application/json");
	        		int cpuCore = project.getQuotaCpuMillicore() / 1000;
	        		int memGB = project.getQuotaMemMB() / 1024;
	        		if(cpuCore >= 0 && memGB >= 0) {
	        			RequestBody body = RequestBody.create(mediaType, REST_TEMPLATE
	        					.replace("PLACEHOLDER_SLICE", sliceName)
	        					.replace("PLACEHOLDER_NAMESPACE", namespace)
	        					.replace("PLACEHOLDER_CPU", ""+cpuCore)
	        					.replace("PLACEHOLDER_MEM", ""+memGB)
	        					);
	        			
		        		log.info("Creating a slice with name " + sliceName + " on namespace " + namespace);
		        		Request request = new Request.Builder()
		        				  .url(soeEndpoint + "/k8s_compute_chunk")
		        				  .method("POST", body)
		        				  .addHeader("Content-Type", "application/json")
		        				  .build();
		        		client.newCall(request).execute();
	        		}
        		}catch(Exception e) {
            		log.error("Provisioning SOE slice", e);
        			saveErrorEvent("SOE provisioning", e.getMessage());
        		}
        	}
        }
    }
    */
    
    private void provision_kafka(Project project) {
    	boolean msgSent = false;
    	
    	int cpuCore = project.getQuotaCpuMillicore();
		int memGB = project.getQuotaMemMB();
		if(cpuCore >= 0 && memGB >= 0) {
        	String soeEndpoint = ConverterJSON.getProperty(project.getInfrastructure().getMonitoringPlugin(), "soe_endpoint");
        	if(soeEndpoint != null) {
        		log.info("Provisioning SOE slice via Kafka");

                long infrastructureId = project.getInfrastructure().getId();
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("limits_cpu", ""+cpuCore);
                parameters.put("limits_memory", ""+memGB);
                parameters.put("requests_cpu", ""+cpuCore);
                parameters.put("requests_memory", ""+memGB);
                
                parameters.putAll(ConverterJSON.convertToMap(project.getProperties()));
                
                orchestrationNotifierService.publish(infrastructureId, "infrastructure", "provision", parameters, new JSONArray());
                
                msgSent = true;
        	}
		}
        if(!msgSent) {
        	saveErrorEvent("SOE provisioning", "Missing parameters for SOE Kafka msg");
        }
    }
    
    @Override
    public void provision(Project project) {
        log.debug("Request to provision Project : {}", project);
        CheckRole.block("ROLE_ROAPI");
        provision_kafka(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        log.debug("Request to get all Projects");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return projectRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return projectRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return projectRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<Project>(new ArrayList<Project>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findOne(Long id) {
        log.debug("Request to get Project : {}", id);

        return projectRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "project", "delete");
        projectRepository.deleteById(id);
    }
}
