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

import eu.pledgerproject.confservice.domain.Sla;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.SlaRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.SlaService;

/**
 * Service Implementation for managing {@link Sla}.
 */
@Service
@Transactional
public class SlaServiceImpl implements SlaService {

    private final Logger log = LoggerFactory.getLogger(SlaServiceImpl.class);

    private final SlaRepository slaRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public SlaServiceImpl(SlaRepository slaRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.slaRepository = slaRepository;
        this.configurationNotifierService = configurationNotifierService;
   }

    @Override
    public Sla save(Sla sla) {
        log.debug("Request to save Sla : {}", sla);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(sla.getId(), "sla", "update");
        return slaRepository.save(sla);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Sla> findAll(Pageable pageable) {
        log.debug("Request to get all Slas");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return slaRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return slaRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return slaRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<Sla>(new ArrayList<Sla>());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public Optional<Sla> findOne(Long id) {
        log.debug("Request to get Sla : {}", id);
        return slaRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Sla : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "sla", "delete");
        slaRepository.deleteById(id);
    }
}
