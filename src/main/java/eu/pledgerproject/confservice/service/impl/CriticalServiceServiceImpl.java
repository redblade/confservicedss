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

import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.repository.CriticalServiceRepository;
import eu.pledgerproject.confservice.service.CriticalServiceService;

/**
 * Service Implementation for managing {@link CriticalService}.
 */
@Service
@Transactional
public class CriticalServiceServiceImpl implements CriticalServiceService {
	

    private final Logger log = LoggerFactory.getLogger(CriticalServiceServiceImpl.class);

    private final CriticalServiceRepository criticalServiceRepository;
    
    public CriticalServiceServiceImpl(CriticalServiceRepository criticalServiceRepository) {
        this.criticalServiceRepository = criticalServiceRepository;
    }
    
    @Override
    public CriticalService save(CriticalService criticalService) {
        log.debug("Request to save CriticalService : {}", criticalService);
        return criticalServiceRepository.save(criticalService);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CriticalService> findAll(Pageable pageable) {
        log.debug("Request to get all CriticalServices");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return criticalServiceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return criticalServiceRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceReportName = securityContext.getAuthentication().getName();
        	return criticalServiceRepository.findAllAuthorizedSP(pageable, serviceReportName);
        }
        else {
        	return new PageImpl<CriticalService>(new ArrayList<CriticalService>());
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public Optional<CriticalService> findOne(Long id) {
        log.debug("Request to get CriticalService : {}", id);
        return criticalServiceRepository.findById(id);
    }
	
	@Override
    public void delete(Long id) {
        log.debug("Request to delete CriticalService : {}", id);
        criticalServiceRepository.deleteById(id);
    }
}
