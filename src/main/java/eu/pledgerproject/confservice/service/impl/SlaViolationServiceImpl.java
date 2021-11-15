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

import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.SlaViolationService;

/**
 * Service Implementation for managing {@link SlaViolation}.
 */
@Service
@Transactional
public class SlaViolationServiceImpl implements SlaViolationService {

    private final Logger log = LoggerFactory.getLogger(SlaViolationServiceImpl.class);

    private final SlaViolationRepository slaViolationRepository;

    public SlaViolationServiceImpl(SlaViolationRepository slaViolationRepository) {
        this.slaViolationRepository = slaViolationRepository;
    }

    @Override
    public SlaViolation save(SlaViolation slaViolation) {
        log.debug("Request to save SlaViolation : {}", slaViolation);
        CheckRole.block("ROLE_ROAPI");

        return slaViolationRepository.save(slaViolation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SlaViolation> findAll(Pageable pageable) {
        log.debug("Request to get all SlaViolations");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return slaViolationRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return slaViolationRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return slaViolationRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<SlaViolation>(new ArrayList<SlaViolation>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<SlaViolation> findOne(Long id) {
        log.debug("Request to get SlaViolation : {}", id);
        return slaViolationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SlaViolation : {}", id);
        CheckRole.block("ROLE_ROAPI");

        slaViolationRepository.deleteById(id);
    }
}
