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

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.message.PublisherConfigurationUpdate;
import eu.pledgerproject.confservice.repository.NodeRepository;
import eu.pledgerproject.confservice.security.CheckRole;
import eu.pledgerproject.confservice.service.NodeService;

/**
 * Service Implementation for managing {@link Node}.
 */
@Service
@Transactional
public class NodeServiceImpl implements NodeService {

    private final Logger log = LoggerFactory.getLogger(NodeServiceImpl.class);

    private final NodeRepository nodeRepository;
    private final PublisherConfigurationUpdate configurationNotifierService;
    
    public NodeServiceImpl(NodeRepository nodeRepository, PublisherConfigurationUpdate configurationNotifierService) {
        this.nodeRepository = nodeRepository;
        this.configurationNotifierService = configurationNotifierService;
    }

    @Override
    public Node save(Node node) {
        log.debug("Request to save Node : {}", node);
        CheckRole.block("ROLE_ROAPI");

        Node result = nodeRepository.save(node);
        configurationNotifierService.publish(result.getId(), "node", "update");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Node> findAll(Pageable pageable) {
        log.debug("Request to get all InfrastructureProviders");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        List<Node> tempResult = new ArrayList<Node>();

        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        	tempResult.addAll(nodeRepository.findAll(pageable).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROAPI"))) {
        	tempResult.addAll(nodeRepository.findAll(pageable).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SP"))) {
        	String serviceProviderName = securityContext.getAuthentication().getName();
        	tempResult.addAll(nodeRepository.findAllAuthorizedSP(pageable, serviceProviderName).getContent());
        }
        else if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IP"))) {
        	String infrastructureProviderName = securityContext.getAuthentication().getName();
        	tempResult.addAll(nodeRepository.findAllAuthorizedIP(pageable, infrastructureProviderName).getContent());
        }
        return new PageImpl<Node>(new ArrayList<Node>(tempResult));

    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Node> findOne(Long id) {
        log.debug("Request to get Node : {}", id);
        return nodeRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Node : {}", id);
        CheckRole.block("ROLE_ROAPI");

        configurationNotifierService.publish(id, "node", "delete");
        nodeRepository.deleteById(id);
    }
}
