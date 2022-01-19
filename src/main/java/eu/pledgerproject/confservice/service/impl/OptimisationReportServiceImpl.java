package eu.pledgerproject.confservice.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.OptimisationReport;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.monitoring.ConverterJSON;
import eu.pledgerproject.confservice.monitoring.ResourceDataReader;
import eu.pledgerproject.confservice.optimisation.ECODAHelper;
import eu.pledgerproject.confservice.optimisation.NodeGroup;
import eu.pledgerproject.confservice.optimisation.ServiceData;
import eu.pledgerproject.confservice.optimisation.ServiceOptimisationType;
import eu.pledgerproject.confservice.optimisation.TTODAHelper;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.service.OptimisationReportService;
import eu.pledgerproject.confservice.util.DoubleFormatter;

/**
 * Service Implementation for managing {@link OptimisationReport}.
 */
@Service
@Transactional
public class OptimisationReportServiceImpl implements OptimisationReportService {

    private final Logger log = LoggerFactory.getLogger(OptimisationReportServiceImpl.class);
    
    private ServiceProviderRepository serviceProviderRepository;
    private ServiceRepository serviceRepository;
    private ResourceDataReader resourceDataReader;
    
    private ECODAHelper ecodaHelper;
    private TTODAHelper ttodaHelper;
    public OptimisationReportServiceImpl(ECODAHelper ecodaHelper, TTODAHelper ttodaHelper, ServiceProviderRepository serviceProviderRepository, ServiceRepository serviceRepository, ResourceDataReader resourceDataReader) {
    	this.ecodaHelper = ecodaHelper;
    	this.ttodaHelper = ttodaHelper;
    	this.serviceProviderRepository = serviceProviderRepository;
    	this.serviceRepository = serviceRepository;
    	this.resourceDataReader = resourceDataReader;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OptimisationReport> findAll(Pageable pageable) {
        log.debug("Request to get all OptimisationReport");
        List<OptimisationReport> tempResult = new ArrayList<OptimisationReport>();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	for(ServiceProvider serviceProvider : serviceProviderRepository.findAll()) {
        		addOptimisationReportList(serviceProvider, tempResult);
        	}
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	Optional<ServiceProvider> serviceProviderDB = serviceProviderRepository.findByName(serviceProviderName);
        	if(serviceProviderDB.isPresent()) {
        		ServiceProvider serviceProvider = serviceProviderDB.get();
        		addOptimisationReportList(serviceProvider, tempResult);
        	}
        }
         
        return new PageImpl<OptimisationReport>(tempResult);
    }
    
    private void addOptimisationReportList(ServiceProvider serviceProvider, List<OptimisationReport> tempResult) {
    	
    	List<eu.pledgerproject.confservice.domain.Service> serviceListLatency = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.latency.name());
    	List<ServiceData> serviceDataListLatency = ecodaHelper.getNewOrderedServiceDataList(serviceProvider, serviceListLatency);
		tempResult.addAll(getOptimisationReportList(serviceProvider, serviceDataListLatency, ServiceOptimisationType.latency));
		
    	List<eu.pledgerproject.confservice.domain.Service> serviceListResourcesLatency = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.resources_latency.name());
    	List<ServiceData> serviceDataListResourcesLatency = ecodaHelper.getNewOrderedServiceDataList(serviceProvider, serviceListResourcesLatency);
		tempResult.addAll(getOptimisationReportList(serviceProvider, serviceDataListResourcesLatency, ServiceOptimisationType.resources_latency));

    	List<eu.pledgerproject.confservice.domain.Service> serviceListLatencyFaredge = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.latency_faredge.name());
    	List<ServiceData> serviceDataListLatencyFaredge = ttodaHelper.getNewOrderedServiceDataList(serviceProvider, serviceListLatencyFaredge);
		tempResult.addAll(getOptimisationReportList(serviceProvider, serviceDataListLatencyFaredge, ServiceOptimisationType.latency_faredge));

    	List<eu.pledgerproject.confservice.domain.Service> serviceListResourcesLatencyFaredge = serviceRepository.getRunningServiceListByServiceProviderAndServiceOptimisation(serviceProvider.getId(), ServiceOptimisationType.resources_latency_faredge.name());
    	List<ServiceData> serviceDataListResourcesLatencyFaredge = ttodaHelper.getNewOrderedServiceDataList(serviceProvider, serviceListResourcesLatencyFaredge);
		tempResult.addAll(getOptimisationReportList(serviceProvider, serviceDataListResourcesLatencyFaredge, ServiceOptimisationType.resources_latency_faredge));

    }

    private List<OptimisationReport> getOptimisationReportList(ServiceProvider serviceProvider, List<ServiceData> serviceDataList, ServiceOptimisationType optimisationType) {
        List<OptimisationReport> result = new ArrayList<OptimisationReport>();
        if(serviceDataList != null) {
	        for(ServiceData serviceData : serviceDataList) {
	        	OptimisationReport report = new OptimisationReport();
	        	
	        	report.setOptimisationType(serviceData.service.getServiceOptimisation().getOptimisation());
	        	report.setAppName(serviceData.service.getApp().getName());
	        	report.setServiceName(serviceData.service.getName());
	        	report.setServicePriority(serviceData.service.getPriority());
	        	report.setRequestCpu(ResourceDataReader.getServiceRuntimeCpuRequest(serviceData.service));
	        	report.setRequestMem(ResourceDataReader.getServiceRuntimeMemRequest(serviceData.service));
	        	report.setStartupTime(resourceDataReader.getServiceStartupTimeSec(serviceData.service));
	        	Node currentNode = resourceDataReader.getCurrentNode(serviceData.service);
	        	report.setNode(currentNode.getName());
	        	
	        	Map<String, String> nodeProperties = ConverterJSON.convertToMap(currentNode.getProperties());
				String nodeType = nodeProperties.get(NodeGroup.NODE_TYPE);
				report.setNodeCategory(nodeType);
				report.setServiceProvider(serviceProvider.getName());
	        	report.setOptimisationScore(DoubleFormatter.formatAsString(serviceData.score));
	        	result.add(report);
	    	}
        }
        
        return result;
    }
   
}
