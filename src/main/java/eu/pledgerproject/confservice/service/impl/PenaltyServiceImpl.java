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

import eu.pledgerproject.confservice.domain.Penalty;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.PenaltyRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.PenaltyService;

/**
 * Service Implementation for managing {@link Penalty}.
 */
@Service
@Transactional
public class PenaltyServiceImpl implements PenaltyService {

    private final Logger log = LoggerFactory.getLogger(PenaltyServiceImpl.class);

    private final PenaltyRepository penaltyRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public PenaltyServiceImpl(PenaltyRepository penaltyRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.penaltyRepository = penaltyRepository;
        this.configurationNotifierService = configurationNotifierService;
    }

    @Override
    public Penalty save(Penalty penalty) {
        log.debug("Request to save Penalty : {}", penalty);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(penalty.getId(), "penalty", "update");
        return penaltyRepository.save(penalty);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Penalty> findAll(Pageable pageable) {
        log.debug("Request to get all Penalties");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return penaltyRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return penaltyRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return penaltyRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<Penalty>(new ArrayList<Penalty>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Penalty> findOne(Long id) {
        log.debug("Request to get Penalty : {}", id);
        return penaltyRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Penalty : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "penalty", "delete");
        penaltyRepository.deleteById(id);
    }
}
