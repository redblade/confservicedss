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

import eu.pledgerproject.confservice.domain.AppConstraint;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.AppConstraintRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.AppConstraintService;

/**
 * Service Implementation for managing {@link AppConstraint}.
 */
@Service
@Transactional
public class AppConstraintServiceImpl implements AppConstraintService {

    private final Logger log = LoggerFactory.getLogger(AppConstraintServiceImpl.class);

    private final AppConstraintRepository appConstraintRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public AppConstraintServiceImpl(AppConstraintRepository appConstraintRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.appConstraintRepository = appConstraintRepository;
        this.configurationNotifierService = configurationNotifierService;
    }

    @Override
    public AppConstraint save(AppConstraint appConstraint) {
        log.debug("Request to save AppConstraint : {}", appConstraint);
        CheckRole.block("ROLE_ROAPI");
        configurationNotifierService.publish(appConstraint.getId(), "appConstraint", "update");
        return appConstraintRepository.save(appConstraint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppConstraint> findAll(Pageable pageable) {
    	log.debug("Request to get all AppConstraints");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return appConstraintRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return appConstraintRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return appConstraintRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<AppConstraint>(new ArrayList<AppConstraint>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AppConstraint> findOne(Long id) {
        log.debug("Request to get AppConstraint : {}", id);
        return appConstraintRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AppConstraint : {}", id);
        CheckRole.block("ROLE_ROAPI");
        configurationNotifierService.publish(id, "appConstraint", "delete");
        appConstraintRepository.deleteById(id);
    }
}
