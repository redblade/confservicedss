package eu.pledgerproject.confservice.service.impl;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.ServiceService;

/**
 * Service Implementation for managing {@link Service}.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceServiceImpl implements ServiceService {

    private final Logger log = LoggerFactory.getLogger(ServiceServiceImpl.class);
    private final PublisherConfigurationUpdate configurationNotifierService;

    private final ServiceRepository serviceRepository;

    public ServiceServiceImpl(ServiceRepository serviceRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.configurationNotifierService = configurationNotifierService;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public Service save(Service service) {
        log.debug("Request to save Service : {}", service);
        CheckRole.block("ROLE_ROAPI");
        if(service.getProfile() == null) {
			service.setProfile(Service.DEFAULT_SERVICE_PROFILE);
        }
        if(service.getInitialConfiguration() == null || service.getInitialConfiguration().trim().length() == 0) {
			service.setInitialConfiguration(Service.DEFAULT_SERVICE_INITIAL_CONF);
        }
        if(service.getRuntimeConfiguration() == null) {
			service.setInitialConfiguration(Service.DEFAULT_SERVICE_RUNTIME_CONF);
        }
        if(service.getStatus() == null) {
        	service.setStatus(ExecStatus.STOPPED);
        }

        Service result = serviceRepository.save(service);
        configurationNotifierService.publish(result.getId(), "service", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Service> findAll(Pageable pageable) {
        log.debug("Request to get all Services");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return serviceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return serviceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return serviceRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<Service>(new ArrayList<Service>());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public Optional<Service> findOne(Long id) {
        log.debug("Request to get Service : {}", id);
        return serviceRepository.findById(id);
    }
    

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Service : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "service", "delete");
        serviceRepository.deleteById(id);
    }
}
