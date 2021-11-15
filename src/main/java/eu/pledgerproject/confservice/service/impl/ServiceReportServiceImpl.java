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

import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.ServiceReportService;

/**
 * Service Implementation for managing {@link ServiceReport}.
 */
@Service
@Transactional
public class ServiceReportServiceImpl implements ServiceReportService {

    private final Logger log = LoggerFactory.getLogger(ServiceReportServiceImpl.class);

    private final ServiceReportRepository serviceReportRepository;

    public ServiceReportServiceImpl(ServiceReportRepository serviceReportRepository) {
        this.serviceReportRepository = serviceReportRepository;
    }

    @Override
    public ServiceReport save(ServiceReport serviceReport) {
        log.debug("Request to save ServiceReport : {}", serviceReport);
        CheckRole.block("ROLE_ROAPI");

        return serviceReportRepository.save(serviceReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceReport> findAll(Pageable pageable, String categoryFilter) {
    	
        log.debug("Request to get all ServiceReports");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return serviceReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return serviceReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceReportName = securityContext.getAuthentication().getName();
        	return serviceReportRepository.findAllAuthorizedSP(pageable, serviceReportName, categoryFilter);
        }
        else {
        	return new PageImpl<ServiceReport>(new ArrayList<ServiceReport>());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceReport> findLastByServiceIdCategoryKey(Long serviceId, String category, String key) {
        log.debug("Request to get findByServiceIdCategoryKey : {}", serviceId, key, category);
        List<ServiceReport> tempResult = serviceReportRepository.findLastByServiceIdCategoryKey(serviceId, category, key);
        return tempResult.size() == 0 ? Optional.empty() : Optional.of(tempResult.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceReport> findOne(Long id) {
        log.debug("Request to get ServiceReport : {}", id);
        return serviceReportRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceReport : {}", id);
        CheckRole.block("ROLE_ROAPI");

        serviceReportRepository.deleteById(id);
    }
}
