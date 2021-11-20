package eu.pledgerproject.confservice.service.impl;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.enumeration.DeployType;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.AppConstraintRepository;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.AppService;

/**
 * Service Implementation for managing {@link App}.
 */
@org.springframework.stereotype.Service
@Transactional
public class AppServiceImpl implements AppService {

    private final Logger log = LoggerFactory.getLogger(AppServiceImpl.class);

    private final AppRepository appRepository;
    private final AppConstraintRepository appConstraintRepository;
    private final ServiceRepository serviceRepository;
    private final EventRepository eventRepository;

    private final PublisherConfigurationUpdate configurationNotifierService;

    public AppServiceImpl(AppRepository appRepository, AppConstraintRepository appConstraintRepository, ServiceRepository serviceRepository, PublisherConfigurationUpdate configurationNotifierService, EventRepository eventRepository) {
        this.appRepository = appRepository;
        this.appConstraintRepository = appConstraintRepository;
        this.serviceRepository = serviceRepository;
        this.configurationNotifierService = configurationNotifierService;
        this.eventRepository = eventRepository;
    }
    
	private void saveErrorEvent(App app, String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setServiceProvider(app.getServiceProvider());
		event.setDetails(msg);
		event.setCategory("AppServiceImpl");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
    
    private void createServices(App app) {
    	try {    
    		String[] services = app.getAppDescriptor().split("service_name: ");
	    	
	    	for(int i=1; i<services.length; i++) {
	    		Properties properties = new Properties();
	    		properties.load(new StringReader("service_name: " + services[i]));
	    		
	    		Service service = new Service();
				
				service.setApp(app);
				service.setName(properties.getProperty("service_name"));
				service.setDeployDescriptor(services[i].split("service_descriptor:")[1]);
				service.setDeployType(DeployType.valueOf(properties.getProperty("service_type")));
				
				service.setInitialConfiguration(Service.DEFAULT_SERVICE_PROFILE);
				service.setInitialConfiguration(Service.DEFAULT_SERVICE_INITIAL_CONF);
				service.setRuntimeConfiguration(Service.DEFAULT_SERVICE_RUNTIME_CONF);
				service.setStatus(ExecStatus.STOPPED);

				serviceRepository.save(service);
	    	}
    	
    	}
    	catch(Exception e) {
    		log.error("Unable to parse appDescriptor " + e);
    		saveErrorEvent(app, e.getMessage());
    	}
    }
    
    @Override
    public App save(App app) {
        log.debug("Request to save App : {}", app);
        CheckRole.block("ROLE_ROAPI");
        if(app.getId() == null) {
        	if(app.getCatalogApp() != null) {
        		app.setAppDescriptor(app.getCatalogApp().getAppDescriptor());
        	}
        	app.setStatus(ExecStatus.STOPPED);
        	createServices(app);
        }
        else {
        	Optional<App> appDB = appRepository.findById(app.getId());
        	if(appDB.isPresent() && !appDB.get().getAppDescriptor().equals(app.getAppDescriptor())) {
        		for(eu.pledgerproject.confservice.domain.Service service : appDB.get().getServiceSets()) {
        			appConstraintRepository.deleteAll(service.getAppConstraintSourceSets());
        			appConstraintRepository.deleteAll(service.getAppConstraintDestinationSets());
        			serviceRepository.delete(service);
        		}
        		createServices(app);
        	}
        }
        App result = appRepository.save(app);
        configurationNotifierService.publish(result.getId(), "app", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<App> findAll(Pageable pageable) {
        log.debug("Request to get all Apps");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return appRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return appRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return appRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<App>(new ArrayList<App>());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<App> findOne(Long id) {
        log.debug("Request to get App : {}", id);
        return appRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete App : {}", id);
        CheckRole.block("ROLE_ROAPI");
        configurationNotifierService.publish(id, "app", "delete");
        for(eu.pledgerproject.confservice.domain.Service service : serviceRepository.findAllByAppId(id)) {
        	serviceRepository.delete(service);
        }
        appRepository.deleteById(id);
    }
}
