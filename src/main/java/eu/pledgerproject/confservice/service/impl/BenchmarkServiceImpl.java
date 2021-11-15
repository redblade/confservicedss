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

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.BenchmarkService;

/**
 * Service Implementation for managing {@link Benchmark}.
 */
@Service
@Transactional
public class BenchmarkServiceImpl implements BenchmarkService {

    private final Logger log = LoggerFactory.getLogger(BenchmarkServiceImpl.class);

    private final BenchmarkRepository benchmarkRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public BenchmarkServiceImpl(BenchmarkRepository benchmarkRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.benchmarkRepository = benchmarkRepository;
        this.configurationNotifierService = configurationNotifierService;
   }

    @Override
    public Benchmark save(Benchmark benchmark) {
        log.debug("Request to save Benchmark : {}", benchmark);
        configurationNotifierService.publish(benchmark.getId(), "benchmark", "update");
        CheckRole.block("ROLE_ROAPI");

        return benchmarkRepository.save(benchmark);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Benchmark> findAll(Pageable pageable) {
        log.debug("Request to get all Benchmarks");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return benchmarkRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return benchmarkRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	
        	List<Benchmark> result = new ArrayList<Benchmark>();
        	result.addAll(benchmarkRepository.findAllPublic(pageable).getContent());
        	result.addAll(benchmarkRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent());
        	return new PageImpl<Benchmark>(result);
        }
        else {
        	return new PageImpl<Benchmark>(new ArrayList<Benchmark>());
        }
    }

    
    @Override
    @Transactional(readOnly = true)
    public Optional<Benchmark> findOne(Long id) {
        log.debug("Request to get Benchmark : {}", id);
        return benchmarkRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Benchmark : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "benchmark", "delete");
        benchmarkRepository.deleteById(id);
    }
}
