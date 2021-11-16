package eu.pledgerproject.confservice.service.impl;

import java.util.ArrayList;
import java.util.List;
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

import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.InfrastructureService;

/**
 * Service Implementation for managing {@link Infrastructure}.
 */
@Service
@Transactional
public class InfrastructureServiceImpl implements InfrastructureService {

    private final Logger log = LoggerFactory.getLogger(InfrastructureServiceImpl.class);

    private final InfrastructureRepository infrastructureRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;
    
    public InfrastructureServiceImpl(InfrastructureRepository infrastructureRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.infrastructureRepository = infrastructureRepository;
        this.configurationNotifierService = configurationNotifierService;
   }

    @Override
    public Infrastructure save(Infrastructure infrastructure) {
        log.debug("Request to save Infrastructure : {}", infrastructure);
        CheckRole.block("ROLE_ROAPI");

        Infrastructure result = infrastructureRepository.save(infrastructure);
        configurationNotifierService.publish(result.getId(), "infrastructure", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Infrastructure> findAll(Pageable pageable) {
        log.debug("Request to get all Infrastructures");
        SecurityContext securityContext = SecurityContextHolder.getContext();

        List<Infrastructure> tempResult = new ArrayList<Infrastructure>();

        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) || securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	tempResult.addAll(infrastructureRepository.findAll(pageable).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	tempResult.addAll(infrastructureRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IP"))) {
        	String infrastructureProviderName = securityContext.getAuthentication().getName();
        	tempResult.addAll(infrastructureRepository.findAllAuthorizedIP(pageable, infrastructureProviderName).getContent());
        }
        
        return new PageImpl<Infrastructure>(tempResult);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Infrastructure> findOne(Long id) {
        log.debug("Request to get Infrastructure : {}", id);

        return infrastructureRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Infrastructure : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "infrastructure", "delete");
        infrastructureRepository.deleteById(id);
    }
}
