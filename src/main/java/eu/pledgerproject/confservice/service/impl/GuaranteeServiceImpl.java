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

import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.monitoring.ControlFlags;
import eu.pledgerproject.confservice.monitoring.PrometheusRuleManager;
import eu.pledgerproject.confservice.repository.GuaranteeRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.GuaranteeService;

/**
 * Service Implementation for managing {@link Guarantee}.
 */
@Service
@Transactional
public class GuaranteeServiceImpl implements GuaranteeService {

    private final Logger log = LoggerFactory.getLogger(GuaranteeServiceImpl.class);

    private final GuaranteeRepository guaranteeRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;
    private final PrometheusRuleManager prometheusRuleManager;

    public GuaranteeServiceImpl(GuaranteeRepository guaranteeRepository, PublisherConfigurationUpdate configurationNotifierService, PrometheusRuleManager prometheusRuleManager) {
        this.guaranteeRepository = guaranteeRepository;
        this.configurationNotifierService = configurationNotifierService;
        this.prometheusRuleManager = prometheusRuleManager;
    }
    
    @Override
    public Guarantee save(Guarantee guarantee) {
        log.debug("Request to save Guarantee : {}", guarantee);
        CheckRole.block("ROLE_ROAPI");
        if(ControlFlags.SLAMANAGER_ENABLED) {
        	prometheusRuleManager.applyPrometheusRule(guarantee);
        }
        Guarantee result = guaranteeRepository.save(guarantee);
        configurationNotifierService.publish(result.getId(), "guarantee", "update");
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Guarantee> findAll(Pageable pageable) {
        log.debug("Request to get all Guarantees");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return guaranteeRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return guaranteeRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return guaranteeRepository.findAllAuthorizedSP(pageable, serviceProviderName);
        }
        else {
        	return new PageImpl<Guarantee>(new ArrayList<Guarantee>());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String readPrometheusRule(Long id) {
        log.debug("Request to read Prometheus Rule for Guarantee : {}", id);
        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(id);
        if(guaranteeOptional.isPresent()) {
        	try {
        		return prometheusRuleManager.readPrometheusRule(guaranteeOptional.get());
        	}catch(Exception e) {
        		throw new RuntimeException(e);
        	}
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guarantee> findBySLA(Long id) {
        log.debug("Request to get all Guarantee by SLA: {}", id);
        return guaranteeRepository.findAllBySLA(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Guarantee> findOne(Long id) {
        log.debug("Request to get Guarantee : {}", id);
        return guaranteeRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Guarantee : {}", id);
        CheckRole.block("ROLE_ROAPI");
        if(ControlFlags.SLAMANAGER_ENABLED) {
	        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(id);
	        if(guaranteeOptional.isPresent()) {
	        	prometheusRuleManager.deletePrometheusRule(guaranteeOptional.get());
	        }
        }

        configurationNotifierService.publish(id, "guarantee", "delete");
        guaranteeRepository.deleteById(id);
    }
}
