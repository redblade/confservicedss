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

import eu.pledgerproject.confservice.domain.CatalogApp;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.CatalogAppRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.CatalogAppService;

/**
 * Service Implementation for managing {@link CatalogApp}.
 */
@Service
@Transactional
public class CatalogAppServiceImpl implements CatalogAppService {

    private final Logger log = LoggerFactory.getLogger(CatalogAppServiceImpl.class);

    private final CatalogAppRepository catalogAppRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;

    public CatalogAppServiceImpl(CatalogAppRepository catalogAppRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.catalogAppRepository = catalogAppRepository;
        this.configurationNotifierService = configurationNotifierService;
   }

    @Override
    public CatalogApp save(CatalogApp catalogApp) {
        log.debug("Request to save CatalogApp : {}", catalogApp);
        CheckRole.block("ROLE_ROAPI");

        CatalogApp result = catalogAppRepository.save(catalogApp);;
        configurationNotifierService.publish(result.getId(), "catalogApp", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogApp> findAll(Pageable pageable) {
        log.debug("Request to get all CatalogApps");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	return catalogAppRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	return catalogAppRepository.findAll(pageable);
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	List<CatalogApp> catalogAppList = new ArrayList<CatalogApp>();
        	catalogAppList.addAll(catalogAppRepository.findAllPublic(pageable).getContent());
        	catalogAppList.addAll(new ArrayList<CatalogApp>(catalogAppRepository.findAllPrivate(pageable, serviceProviderName).getContent()));

        	return new PageImpl<CatalogApp>(catalogAppList);
        }
        else {
        	return new PageImpl<CatalogApp>(new ArrayList<CatalogApp>());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogApp> findOne(Long id) {
        log.debug("Request to get CatalogApp : {}", id);
        return catalogAppRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CatalogApp : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "catalogApp", "delete");
        catalogAppRepository.deleteById(id);
    }
}
