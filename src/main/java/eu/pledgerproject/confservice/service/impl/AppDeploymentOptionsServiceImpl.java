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

import eu.pledgerproject.confservice.domain.App;
import eu.pledgerproject.confservice.domain.AppDeploymentOptions;
import eu.pledgerproject.confservice.monitoring.DeploymentOptionsManager;
import eu.pledgerproject.confservice.repository.AppRepository;
import eu.pledgerproject.confservice.service.AppDeploymentOptionsService;

/**
 * Service Implementation for managing {@link AppDeploymentOptions}.
 */
@Service
@Transactional
public class AppDeploymentOptionsServiceImpl implements AppDeploymentOptionsService {

    private final Logger log = LoggerFactory.getLogger(AppDeploymentOptionsServiceImpl.class);
    
    private DeploymentOptionsManager filterService;
    private AppRepository appRepository;
    public AppDeploymentOptionsServiceImpl(DeploymentOptionsManager filterService, AppRepository appRepository) {
    	this.filterService = filterService;
    	this.appRepository = appRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppDeploymentOptions> findAll(Pageable pageable) {
        log.debug("Request to get all AppDeploymentOptions");
        List<AppDeploymentOptions> result = new ArrayList<AppDeploymentOptions>();
        
        SecurityContext securityContext = SecurityContextHolder.getContext();
        List<App> allowedApps = new ArrayList<App>();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	allowedApps = appRepository.findAll();
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	allowedApps = appRepository.findAll();
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	allowedApps = appRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent();
        }
        
        for(App app : appRepository.findAll()) {
        	if(allowedApps.contains(app)) {
	        	AppDeploymentOptions appDeploymentOptions = new AppDeploymentOptions();
	        	appDeploymentOptions.setId(app.getId());
	        	appDeploymentOptions.setApp(app);
	        	result.add(appDeploymentOptions);
        	}
        }
        return new PageImpl<AppDeploymentOptions>(result);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AppDeploymentOptions> findOne(Long id) {
        log.debug("Request to get AppDeploymentOptions : {}", id);
        Optional<App> appDB = appRepository.findById(id);
        if(appDB.isPresent()) {
        	
        	AppDeploymentOptions appDeploymentOptions = new AppDeploymentOptions();
        	appDeploymentOptions.setId(appDB.get().getId());
        	appDeploymentOptions.setApp(appDB.get());
        	
        	String options = filterService.getAppDeploymentOptionsString(appDB.get());
        	appDeploymentOptions.setOptions(options);
        	return Optional.of(appDeploymentOptions);
        }
        else {
        	return Optional.empty();
        }
    }

   
}
