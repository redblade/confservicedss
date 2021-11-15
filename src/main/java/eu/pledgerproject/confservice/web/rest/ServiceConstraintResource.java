package eu.pledgerproject.confservice.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.pledgerproject.confservice.domain.ServiceConstraint;
import eu.pledgerproject.confservice.service.ServiceConstraintService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.ServiceConstraint}.
 */
@RestController
@RequestMapping("/api")
public class ServiceConstraintResource {

    private final Logger log = LoggerFactory.getLogger(ServiceConstraintResource.class);

    private static final String ENTITY_NAME = "serviceConstraint";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServiceConstraintService serviceConstraintService;

    public ServiceConstraintResource(ServiceConstraintService serviceConstraintService) {
        this.serviceConstraintService = serviceConstraintService;
    }

    /**
     * {@code POST  /service-constraints} : Create a new serviceConstraint.
     *
     * @param serviceConstraint the serviceConstraint to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new serviceConstraint, or with status {@code 400 (Bad Request)} if the serviceConstraint has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/service-constraints")
    public ResponseEntity<ServiceConstraint> createServiceConstraint(@RequestBody ServiceConstraint serviceConstraint) throws URISyntaxException {
        log.debug("REST request to save ServiceConstraint : {}", serviceConstraint);
        if (serviceConstraint.getId() != null) {
            throw new BadRequestAlertException("A new serviceConstraint cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ServiceConstraint result = serviceConstraintService.save(serviceConstraint);
        return ResponseEntity.created(new URI("/api/service-constraints/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /service-constraints} : Updates an existing serviceConstraint.
     *
     * @param serviceConstraint the serviceConstraint to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceConstraint,
     * or with status {@code 400 (Bad Request)} if the serviceConstraint is not valid,
     * or with status {@code 500 (Internal Server Error)} if the serviceConstraint couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/service-constraints")
    public ResponseEntity<ServiceConstraint> updateServiceConstraint(@RequestBody ServiceConstraint serviceConstraint) throws URISyntaxException {
        log.debug("REST request to update ServiceConstraint : {}", serviceConstraint);
        if (serviceConstraint.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ServiceConstraint result = serviceConstraintService.save(serviceConstraint);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, serviceConstraint.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /service-constraints} : get all the serviceConstraints.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of serviceConstraints in body.
     */
    @GetMapping("/service-constraints")
    public ResponseEntity<List<ServiceConstraint>> getAllServiceConstraints(Pageable pageable) {
        log.debug("REST request to get a page of ServiceConstraints");
        Page<ServiceConstraint> page = serviceConstraintService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /service-constraints/:id} : get the "id" serviceConstraint.
     *
     * @param id the id of the serviceConstraint to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceConstraint, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/service-constraints/{id}")
    public ResponseEntity<ServiceConstraint> getServiceConstraint(@PathVariable Long id) {
        log.debug("REST request to get ServiceConstraint : {}", id);
        Optional<ServiceConstraint> serviceConstraint = serviceConstraintService.findOne(id);
        return ResponseUtil.wrapOrNotFound(serviceConstraint);
    }

    /**
     * {@code DELETE  /service-constraints/:id} : delete the "id" serviceConstraint.
     *
     * @param id the id of the serviceConstraint to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/service-constraints/{id}")
    public ResponseEntity<Void> deleteServiceConstraint(@PathVariable Long id) {
        log.debug("REST request to delete ServiceConstraint : {}", id);
        serviceConstraintService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
