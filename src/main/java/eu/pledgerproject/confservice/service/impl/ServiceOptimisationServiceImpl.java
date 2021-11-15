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

import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.repository.ServiceOptimisationRepository;
import eu.pledgerproject.confservice.service.ServiceOptimisationService;

/**
 * Service Implementation for managing {@link ServiceOptimisation}.
 */
@Service
@Transactional
public class ServiceOptimisationServiceImpl implements ServiceOptimisationService {

    private final Logger log = LoggerFactory.getLogger(ServiceOptimisationServiceImpl.class);

    private final ServiceOptimisationRepository serviceOptimisationRepository;

    public ServiceOptimisationServiceImpl(ServiceOptimisationRepository serviceOptimisationRepository) {
        this.serviceOptimisationRepository = serviceOptimisationRepository;
    }

    @Override
    public ServiceOptimisation save(ServiceOptimisation serviceOptimisation) {
        log.debug("Request to save ServiceOptimisation : {}", serviceOptimisation);
        return serviceOptimisationRepository.save(serviceOptimisation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOptimisation> findAll(Pageable pageable) {
    	log.debug("Request to get all Apps");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return serviceOptimisationRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return serviceOptimisationRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return serviceOptimisationRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<ServiceOptimisation>(new ArrayList<ServiceOptimisation>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceOptimisation> findOne(Long id) {
        log.debug("Request to get ServiceOptimisation : {}", id);
        return serviceOptimisationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceOptimisation : {}", id);
        serviceOptimisationRepository.deleteById(id);
    }
}
