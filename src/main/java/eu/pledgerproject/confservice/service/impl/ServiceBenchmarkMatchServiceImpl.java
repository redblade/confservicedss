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
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.domain.BenchmarkSummary;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceBenchmarkMatch;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.service.ServiceBenchmarkMatchService;

/**
 * Service Implementation for managing {@link BenchmarkSummary}.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceBenchmarkMatchServiceImpl implements ServiceBenchmarkMatchService {
	public static final String RATIONALE_MATCH_BENCHMARK_NAME = "service.profile field is EQUAL TO benchmark.name";
	public static final String RATIONALE_MATCH_BENCHMARK_LABEL = "service.profile field is CONTAINED INTO benchmark.category labels";

    private final Logger log = LoggerFactory.getLogger(ServiceBenchmarkMatchServiceImpl.class);

    private final BenchmarkRepository benchmarkRepository;
    private final ServiceRepository serviceRepository;

    public ServiceBenchmarkMatchServiceImpl(BenchmarkRepository benchmarkRepository, ServiceRepository serviceRepository) {
    	this.benchmarkRepository = benchmarkRepository;
    	this.serviceRepository = serviceRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ServiceBenchmarkMatch> findAll(Pageable pageable) {
    	log.debug("Request to get all BenchmarkSummarys");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return createServiceBenchmarkMatch(pageable, serviceRepository.findAll());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return createServiceBenchmarkMatch(pageable, serviceRepository.findAll());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	return createServiceBenchmarkMatch(pageable, serviceRepository.findAllAuthorizedSP(serviceProviderName));
        }
        else {
        	return new PageImpl<ServiceBenchmarkMatch>(new ArrayList<ServiceBenchmarkMatch>());
        }
        
    }

    private Page<ServiceBenchmarkMatch> createServiceBenchmarkMatch(Pageable pageable, List<Service> serviceList){
    	List<ServiceBenchmarkMatch> result = new ArrayList<ServiceBenchmarkMatch>();

    	long i = 0;
    	for(Service service : serviceList) {
    		Optional<Benchmark> benchmarkDB = benchmarkRepository.findByName(service.getProfile());
    		if(benchmarkDB.isPresent()) {
    			result.add(new ServiceBenchmarkMatch(i++, service, benchmarkDB.get(), RATIONALE_MATCH_BENCHMARK_NAME));
    		}
    		else {
    			String categoryLike = "%\""+service.getProfile()+"\"%";
    			for(Benchmark benchmark : benchmarkRepository.findByCategoryLike(categoryLike)) {
        			result.add(new ServiceBenchmarkMatch(i++, service, benchmark, RATIONALE_MATCH_BENCHMARK_LABEL));    				
    			}
    		}
    	}
    	
    	int indexStart = pageable.getPageNumber()*pageable.getPageSize();
    	int indexStop = (1+pageable.getPageNumber())*pageable.getPageSize();
    	indexStart = Math.min(indexStart, result.size());
    	indexStop = Math.min(indexStop, result.size());
    	return new PageImpl<ServiceBenchmarkMatch>(result.subList(indexStart, indexStop), pageable, result.size());
    }
    
}
