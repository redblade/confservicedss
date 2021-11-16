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

import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.monitoring.PrometheusRuleGenerator;
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
    private final PrometheusRuleGenerator prometheusRuleGenerator;

    public GuaranteeServiceImpl(GuaranteeRepository guaranteeRepository, PublisherConfigurationUpdate configurationNotifierService, PrometheusRuleGenerator prometheusRuleGenerator) {
        this.guaranteeRepository = guaranteeRepository;
        this.configurationNotifierService = configurationNotifierService;
        this.prometheusRuleGenerator = prometheusRuleGenerator;
    }
    
    private void managePrometheusRule(Guarantee guarantee) {
    	if(guarantee.getSla() != null && guarantee.getSla().getService().getApp().getManagementType().equals(ManagementType.MANAGED)) {
    		String prometheusRule = prometheusRuleGenerator.generate(guarantee, "core");
    		if(prometheusRule != null) {
    			log.info("\n\n\n"+prometheusRule+"\n\n\n");
    		}
        }
    }

    @Override
    public Guarantee save(Guarantee guarantee) {
        log.debug("Request to save Guarantee : {}", guarantee);
        CheckRole.block("ROLE_ROAPI");

        
        Guarantee result = guaranteeRepository.save(guarantee);
        configurationNotifierService.publish(result.getId(), "guarantee", "update");
        
        managePrometheusRule(guarantee);
        
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
    public String getPrometheusRules(Long id, String namespace) {
        log.debug("Request to get Prometheus Rule for Guarantee : {}", id);
        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(id);
        if(guaranteeOptional.isPresent()) {
        	return prometheusRuleGenerator.generate(guaranteeOptional.get(), namespace);	
        }
        else {
        	return "";
        }
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

        configurationNotifierService.publish(id, "guarantee", "delete");
        guaranteeRepository.deleteById(id);
    }
}
