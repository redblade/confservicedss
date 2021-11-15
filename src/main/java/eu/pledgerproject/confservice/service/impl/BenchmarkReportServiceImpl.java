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

import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.BenchmarkReportService;

/**
 * Service Implementation for managing {@link BenchmarkReport}.
 */
@Service
@Transactional
public class BenchmarkReportServiceImpl implements BenchmarkReportService {

    private final Logger log = LoggerFactory.getLogger(BenchmarkReportServiceImpl.class);

    private final BenchmarkReportRepository benchmarkReportRepository;

    public BenchmarkReportServiceImpl(BenchmarkReportRepository benchmarkReportRepository) {
        this.benchmarkReportRepository = benchmarkReportRepository;
    }

    @Override
    public BenchmarkReport save(BenchmarkReport benchmarkReport) {
        log.debug("Request to save BenchmarkReport : {}", benchmarkReport);
        CheckRole.block("ROLE_ROAPI");

        return benchmarkReportRepository.save(benchmarkReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BenchmarkReport> findAll(Pageable pageable) {
        log.debug("Request to get all BenchmarkReports");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return benchmarkReportRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return benchmarkReportRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	
        	List<BenchmarkReport> result = new ArrayList<BenchmarkReport>();
        	result.addAll(benchmarkReportRepository.findAllPublic(pageable).getContent());
        	result.addAll(benchmarkReportRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent());
        	return new PageImpl<BenchmarkReport>(result);
        }
        else {
        	return new PageImpl<BenchmarkReport>(new ArrayList<BenchmarkReport>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<BenchmarkReport> findOne(Long id) {
        log.debug("Request to get BenchmarkReport : {}", id);
        return benchmarkReportRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete BenchmarkReport : {}", id);
        CheckRole.block("ROLE_ROAPI");

        benchmarkReportRepository.deleteById(id);
    }
}
