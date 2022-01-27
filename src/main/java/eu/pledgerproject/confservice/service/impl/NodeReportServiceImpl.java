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

import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.NodeReportService;

/**
 * Service Implementation for managing {@link NodeReport}.
 */
@Service
@Transactional
public class NodeReportServiceImpl implements NodeReportService {

    private final Logger log = LoggerFactory.getLogger(NodeReportServiceImpl.class);

    private final NodeReportRepository nodeReportRepository;

    public NodeReportServiceImpl(NodeReportRepository nodeReportRepository) {
        this.nodeReportRepository = nodeReportRepository;
    }

    @Override
    public NodeReport save(NodeReport nodeReport) {
        log.debug("Request to save NodeReport : {}", nodeReport);
        CheckRole.block("ROLE_ROAPI");

        return nodeReportRepository.save(nodeReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NodeReport> findAll(Pageable pageable, String categoryFilter) {
        log.debug("Request to get all NodeReports");
        SecurityContext securityContext = SecurityContextHolder.getContext();

        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return nodeReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return nodeReportRepository.findAll(pageable, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return nodeReportRepository.findAllAuthorizedSP(pageable, serviceProviderName, categoryFilter);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IP"))) {
        	String nodeReportName = securityContext.getAuthentication().getName();
        	return nodeReportRepository.findAllAuthorizedIP(pageable, nodeReportName, categoryFilter);
        }
        return new PageImpl<NodeReport>(new ArrayList<NodeReport>());

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NodeReport> findOne(Long id) {
        log.debug("Request to get NodeReport : {}", id);
        return nodeReportRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete NodeReport : {}", id);
        CheckRole.block("ROLE_ROAPI");

        nodeReportRepository.deleteById(id);
    }
}
