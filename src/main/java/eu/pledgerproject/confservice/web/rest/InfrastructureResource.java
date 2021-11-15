package eu.pledgerproject.confservice.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.repository.CustomAuditEventRepository;
import eu.pledgerproject.confservice.service.InfrastructureService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Infrastructure}.
 */
@RestController
@RequestMapping("/api")
public class InfrastructureResource {

    private final Logger log = LoggerFactory.getLogger(InfrastructureResource.class);

    private static final String ENTITY_NAME = "infrastructure";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InfrastructureService infrastructureService;
    private final CustomAuditEventRepository customAuditEventRepository;

    public InfrastructureResource(InfrastructureService infrastructureService, CustomAuditEventRepository customAuditEventRepository) {
        this.infrastructureService = infrastructureService;
        this.customAuditEventRepository = customAuditEventRepository;
    }

    /**
     * {@code POST  /infrastructures} : Create a new infrastructure.
     *
     * @param infrastructure the infrastructure to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new infrastructure, or with status {@code 400 (Bad Request)} if the infrastructure has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/infrastructures")
    public ResponseEntity<Infrastructure> createInfrastructure(@Valid @RequestBody Infrastructure infrastructure) throws URISyntaxException {
        log.debug("REST request to save Infrastructure : {}", infrastructure);
        if (infrastructure.getId() != null) {
            throw new BadRequestAlertException("A new infrastructure cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Infrastructure result = infrastructureService.save(infrastructure);
        return ResponseEntity.created(new URI("/api/infrastructures/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    /**
     * {@code PUT  /infrastructures} : Updates an existing infrastructure.
     *
     * @param infrastructure the infrastructure to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated infrastructure,
     * or with status {@code 400 (Bad Request)} if the infrastructure is not valid,
     * or with status {@code 500 (Internal Server Error)} if the infrastructure couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/infrastructures")
    public ResponseEntity<Infrastructure> updateInfrastructure(@Valid @RequestBody Infrastructure infrastructure) throws URISyntaxException {
        log.debug("REST request to update Infrastructure : {}", infrastructure);
        if (infrastructure.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Infrastructure result = infrastructureService.save(infrastructure);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, infrastructure.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /infrastructures} : get all the infrastructures.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of infrastructures in body.
     */
    @GetMapping("/infrastructures")
    public ResponseEntity<List<Infrastructure>> getAllInfrastructures(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of Infrastructures");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().iterator().next().getAuthority().equals("ROLE_ROAPI")) {
	        AuditEvent auditEvent = new AuditEvent(((org.springframework.security.core.userdetails.User)securityContext.getAuthentication().getPrincipal()).getUsername(), "READ_INFRASTRUCTURE from IP: " + request.getRemoteAddr());
	        customAuditEventRepository.add(auditEvent);
        }
        
        Page<Infrastructure> page = infrastructureService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /infrastructures/:id} : get the "id" infrastructure.
     *
     * @param id the id of the infrastructure to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the infrastructure, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/infrastructures/{id}")
    public ResponseEntity<Infrastructure> getInfrastructure(@PathVariable Long id, HttpServletRequest request) {
        log.debug("REST request to get Infrastructure : {}", id);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().iterator().next().getAuthority().equals("ROLE_ROAPI")) {
	        AuditEvent auditEvent = new AuditEvent(((org.springframework.security.core.userdetails.User)securityContext.getAuthentication().getPrincipal()).getUsername(), "READ_INFRASTRUCTURE from IP: " + request.getRemoteAddr());
	        customAuditEventRepository.add(auditEvent);
        }
        
        Optional<Infrastructure> infrastructure = infrastructureService.findOne(id);
        return ResponseUtil.wrapOrNotFound(infrastructure);
    }

    /**
     * {@code DELETE  /infrastructures/:id} : delete the "id" infrastructure.
     *
     * @param id the id of the infrastructure to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/infrastructures/{id}")
    public ResponseEntity<Void> deleteInfrastructure(@PathVariable Long id) {
        log.debug("REST request to delete Infrastructure : {}", id);
        infrastructureService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
