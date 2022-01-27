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

import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.repository.InfrastructureReportRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.InfrastructureReportService;

/**
 * Service Implementation for managing {@link InfrastructureReport}.
 */
@Service
@Transactional
public class InfrastructureReportServiceImpl implements InfrastructureReportService {

    private final Logger log = LoggerFactory.getLogger(InfrastructureReportServiceImpl.class);

    private final InfrastructureReportRepository infrastructureReportRepository;

    public InfrastructureReportServiceImpl(InfrastructureReportRepository infrastructureReportRepository) {
        this.infrastructureReportRepository = infrastructureReportRepository;
    }

    @Override
    public InfrastructureReport save(InfrastructureReport infrastructureReport) {
        log.debug("Request to save InfrastructureReport : {}", infrastructureReport);
        CheckRole.block("ROLE_ROAPI");

        return infrastructureReportRepository.save(infrastructureReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InfrastructureReport> findAll(Pageable pageable, String categoryFilter) {
        log.debug("Request to get all InfrastructureReports");
        SecurityContext securityContext = SecurityContextHolder.getContext();

        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return infrastructureReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return infrastructureReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return infrastructureReportRepository.findAllAuthorizedSP(pageable, serviceProviderName, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IP"))) {
        	String infrastructureReportName = securityContext.getAuthentication().getName();
        	return infrastructureReportRepository.findAllAuthorizedIP(pageable, infrastructureReportName, categoryFilter);
        }
        return new PageImpl<InfrastructureReport>(new ArrayList<InfrastructureReport>());
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<InfrastructureReport> findOne(Long id) {
        log.debug("Request to get InfrastructureReport : {}", id);
        return infrastructureReportRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete InfrastructureReport : {}", id);
        CheckRole.block("ROLE_ROAPI");

        infrastructureReportRepository.deleteById(id);
    }
}
