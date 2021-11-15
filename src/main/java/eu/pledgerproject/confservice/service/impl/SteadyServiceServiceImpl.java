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

import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.repository.SteadyServiceRepository;
import eu.pledgerproject.confservice.service.SteadyServiceService;

/**
 * Service Implementation for managing {@link SteadyService}.
 */
@Service
@Transactional
public class SteadyServiceServiceImpl implements SteadyServiceService {
	

    private final Logger log = LoggerFactory.getLogger(SteadyServiceServiceImpl.class);

    private final SteadyServiceRepository steadyServiceRepository;
    
    public SteadyServiceServiceImpl(SteadyServiceRepository steadyServiceRepository) {
        this.steadyServiceRepository = steadyServiceRepository;
    }
    
    @Override
    public SteadyService save(SteadyService steadyService) {
        log.debug("Request to save SteadyService : {}", steadyService);
        return steadyServiceRepository.save(steadyService);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SteadyService> findAll(Pageable pageable) {
        log.debug("Request to get all SteadyServices");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return steadyServiceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return steadyServiceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceReportName = securityContext.getAuthentication().getName();
        	return steadyServiceRepository.findAllAuthorizedSP(pageable, serviceReportName);
        }
        else {
        	return new PageImpl<SteadyService>(new ArrayList<SteadyService>());
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public Optional<SteadyService> findOne(Long id) {
        log.debug("Request to get SteadyService : {}", id);
        return steadyServiceRepository.findById(id);
    }
	
	@Override
    public void delete(Long id) {
        log.debug("Request to delete SteadyService : {}", id);
        steadyServiceRepository.deleteById(id);
    }
}
