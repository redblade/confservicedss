package eu.pledgerproject.confservice.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
import eu.pledgerproject.confservice.domain.BenchmarkSummary;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.monitoring.BenchmarkManager;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.service.BenchmarkSummaryService;
import eu.pledgerproject.confservice.util.DoubleFormatter;

/**
 * Service Implementation for managing {@link BenchmarkSummary}.
 */
@Service
@Transactional
public class BenchmarkSummaryServiceImpl implements BenchmarkSummaryService {

    private final Logger log = LoggerFactory.getLogger(BenchmarkSummaryServiceImpl.class);

    private final BenchmarkRepository benchmarkRepository;
    private final BenchmarkReportRepository benchmarkReportRepository;

    public BenchmarkSummaryServiceImpl(BenchmarkRepository benchmarkRepository, BenchmarkReportRepository benchmarkReportRepository) {
    	this.benchmarkRepository = benchmarkRepository;
    	this.benchmarkReportRepository = benchmarkReportRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BenchmarkSummary> findAll(Pageable pageable) {
    	log.debug("Request to get all BenchmarkSummarys");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return createBenchmarkSummary(pageable, benchmarkRepository.findAll());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return createBenchmarkSummary(pageable, benchmarkRepository.findAll());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	
        	List<Benchmark> benchmarkList = new ArrayList<Benchmark>();
        	benchmarkList.addAll(benchmarkRepository.findAllPublic(pageable).getContent());
        	benchmarkList.addAll(benchmarkRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent());
        	return createBenchmarkSummary(pageable, benchmarkList);
        }
        else {
        	return new PageImpl<BenchmarkSummary>(new ArrayList<BenchmarkSummary>());
        }
    }

    private Page<BenchmarkSummary> createBenchmarkSummary(Pageable pageable, List<Benchmark> benchmarkList){
    	List<BenchmarkSummary> result = new ArrayList<BenchmarkSummary>();
    	
    	List<Object> objectList = benchmarkReportRepository.findBenchmarkNodeMeanFromBenchmarkReportMetricAndTimestampAndBenchmarkList(BenchmarkManager.DEFAULT_METRIC, Instant.now().minusSeconds(BenchmarkManager.DEFAULT_SEC_CHECK_BENCHMARK_REPORT), benchmarkList);
    	long i = 0;
    	for(Object object : objectList) {
    		Object[] elems = (Object[]) object;
    		Benchmark benchmark = (Benchmark) elems[0];
    		Node node = (Node) elems[1];
    		Double score = DoubleFormatter.format((Double) elems[2]);
    		result.add(new BenchmarkSummary(i++, score, node, benchmark));
    	}
    	int indexStart = pageable.getPageNumber()*pageable.getPageSize();
    	int indexStop = (1+pageable.getPageNumber())*pageable.getPageSize();
    	indexStart = Math.min(indexStart, result.size());
    	indexStop = Math.min(indexStop, result.size());
    	return new PageImpl<BenchmarkSummary>(result.subList(indexStart, indexStop), pageable, result.size());
    }
    
}
