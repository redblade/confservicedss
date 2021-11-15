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

import eu.pledgerproject.confservice.domain.ServiceConstraint;
import eu.pledgerproject.confservice.repository.ServiceConstraintRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.ServiceConstraintService;

/**
 * Service Implementation for managing {@link ServiceConstraint}.
 */
@Service
@Transactional
public class ServiceConstraintServiceImpl implements ServiceConstraintService {

    private final Logger log = LoggerFactory.getLogger(ServiceConstraintServiceImpl.class);

    private final ServiceConstraintRepository serviceConstraintRepository;

    public ServiceConstraintServiceImpl(ServiceConstraintRepository serviceConstraintRepository) {
        this.serviceConstraintRepository = serviceConstraintRepository;
    }

    @Override
    public ServiceConstraint save(ServiceConstraint serviceConstraint) {
        log.debug("Request to save ServiceConstraint : {}", serviceConstraint);
        CheckRole.block("ROLE_ROAPI");

        return serviceConstraintRepository.save(serviceConstraint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceConstraint> findAll(Pageable pageable) {
        log.debug("Request to get all Apps");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return serviceConstraintRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return serviceConstraintRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return serviceConstraintRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<ServiceConstraint>(new ArrayList<ServiceConstraint>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceConstraint> findOne(Long id) {
        log.debug("Request to get ServiceConstraint : {}", id);
        return serviceConstraintRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceConstraint : {}", id);
        CheckRole.block("ROLE_ROAPI");

        serviceConstraintRepository.deleteById(id);
    }
}
