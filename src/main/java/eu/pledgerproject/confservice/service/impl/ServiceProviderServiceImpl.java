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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.ServiceProviderService;

/**
 * Service Implementation for managing {@link ServiceProvider}.
 */
@Service
@Transactional
public class ServiceProviderServiceImpl implements ServiceProviderService {
	public static String SERVICE_PROVIDER_PREFERENCES_TEMPLATE = "{";

    private final Logger log = LoggerFactory.getLogger(ServiceProviderServiceImpl.class);

    private final ServiceProviderRepository serviceProviderRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public ServiceProviderServiceImpl(ServiceProviderRepository serviceProviderRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.configurationNotifierService = configurationNotifierService;
   }

    @Override
    public ServiceProvider save(ServiceProvider serviceProvider) {
        log.debug("Request to save ServiceProvider : {}", serviceProvider);
        CheckRole.block("ROLE_ROAPI");
        
        if(serviceProvider.getPreferences()== null || serviceProvider.getPreferences().isEmpty()) {
        	serviceProvider.setPreferences(SERVICE_PROVIDER_PREFERENCES_TEMPLATE);
        }

        configurationNotifierService.publish(serviceProvider.getId(), "serviceProvider", "update");
        return serviceProviderRepository.save(serviceProvider);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceProvider> findAll(Pageable pageable) {
        log.debug("Request to get all ServiceProviders");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return serviceProviderRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return serviceProviderRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return serviceProviderRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<ServiceProvider>(new ArrayList<ServiceProvider>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceProvider> findOne(Long id) {
        log.debug("Request to get ServiceProvider : {}", id);
        return serviceProviderRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceProvider : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "serviceProvider", "delete");
        serviceProviderRepository.deleteById(id);
    }
}
