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

import eu.pledgerproject.confservice.domain.InfrastructureProvider;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.InfrastructureProviderRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.InfrastructureProviderService;

/**
 * Service Implementation for managing {@link InfrastructureProvider}.
 */
@Service
@Transactional
public class InfrastructureProviderServiceImpl implements InfrastructureProviderService {

    private final Logger log = LoggerFactory.getLogger(InfrastructureProviderServiceImpl.class);

    private final InfrastructureProviderRepository infrastructureProviderRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public InfrastructureProviderServiceImpl(InfrastructureProviderRepository infrastructureProviderRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.infrastructureProviderRepository = infrastructureProviderRepository;
        this.configurationNotifierService = configurationNotifierService;
  }

    @Override
    public InfrastructureProvider save(InfrastructureProvider infrastructureProvider) {
        log.debug("Request to save InfrastructureProvider : {}", infrastructureProvider);
        CheckRole.block("ROLE_ROAPI");

        InfrastructureProvider result = infrastructureProviderRepository.save(infrastructureProvider);
        configurationNotifierService.publish(result.getId(), "infrastructureProvider", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InfrastructureProvider> findAll(Pageable pageable) {
        log.debug("Request to get all InfrastructureProviders");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        List<InfrastructureProvider> tempResult = new ArrayList<InfrastructureProvider>();
        
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	tempResult.addAll(infrastructureProviderRepository.findAll(pageable).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	tempResult.addAll(infrastructureProviderRepository.findAll(pageable).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	for (InfrastructureProvider infrastructureProvider : infrastructureProviderRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent()) {
        		if(!tempResult.contains(infrastructureProvider)) {
        			tempResult.add(infrastructureProvider);
        		}
        	}
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IP"))) {
        	String infrastructureProviderName = securityContext.getAuthentication().getName();
        	tempResult.addAll(infrastructureProviderRepository.findByNamePageable(pageable, infrastructureProviderName).getContent());
        }
        return new PageImpl<InfrastructureProvider>(tempResult);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<InfrastructureProvider> findOne(Long id) {
        log.debug("Request to get InfrastructureProvider : {}", id);
        return infrastructureProviderRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete InfrastructureProvider : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "infrastructureProvider", "delete");
        infrastructureProviderRepository.deleteById(id);
    }
}
