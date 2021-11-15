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

import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.service.ServiceOptimisationService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.ServiceOptimisation}.
 */
@RestController
@RequestMapping("/api")
public class ServiceOptimisationResource {

    private final Logger log = LoggerFactory.getLogger(ServiceOptimisationResource.class);

    private static final String ENTITY_NAME = "serviceOptimisation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServiceOptimisationService serviceOptimisationService;

    public ServiceOptimisationResource(ServiceOptimisationService serviceOptimisationService) {
        this.serviceOptimisationService = serviceOptimisationService;
    }

    /**
     * {@code POST  /service-optimisations} : Create a new serviceOptimisation.
     *
     * @param serviceOptimisation the serviceOptimisation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new serviceOptimisation, or with status {@code 400 (Bad Request)} if the serviceOptimisation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/service-optimisations")
    public ResponseEntity<ServiceOptimisation> createServiceOptimisation(@RequestBody ServiceOptimisation serviceOptimisation) throws URISyntaxException {
        log.debug("REST request to save ServiceOptimisation : {}", serviceOptimisation);
        if (serviceOptimisation.getId() != null) {
            throw new BadRequestAlertException("A new serviceOptimisation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ServiceOptimisation result = serviceOptimisationService.save(serviceOptimisation);
        return ResponseEntity.created(new URI("/api/service-optimisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /service-optimisations} : Updates an existing serviceOptimisation.
     *
     * @param serviceOptimisation the serviceOptimisation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceOptimisation,
     * or with status {@code 400 (Bad Request)} if the serviceOptimisation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the serviceOptimisation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/service-optimisations")
    public ResponseEntity<ServiceOptimisation> updateServiceOptimisation(@RequestBody ServiceOptimisation serviceOptimisation) throws URISyntaxException {
        log.debug("REST request to update ServiceOptimisation : {}", serviceOptimisation);
        if (serviceOptimisation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ServiceOptimisation result = serviceOptimisationService.save(serviceOptimisation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, serviceOptimisation.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /service-optimisations} : get all the serviceOptimisations.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of serviceOptimisations in body.
     */
    @GetMapping("/service-optimisations")
    public ResponseEntity<List<ServiceOptimisation>> getAllServiceOptimisations(Pageable pageable) {
        log.debug("REST request to get a page of ServiceOptimisations");
        Page<ServiceOptimisation> page = serviceOptimisationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /service-optimisations/:id} : get the "id" serviceOptimisation.
     *
     * @param id the id of the serviceOptimisation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceOptimisation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/service-optimisations/{id}")
    public ResponseEntity<ServiceOptimisation> getServiceOptimisation(@PathVariable Long id) {
        log.debug("REST request to get ServiceOptimisation : {}", id);
        Optional<ServiceOptimisation> serviceOptimisation = serviceOptimisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(serviceOptimisation);
    }

    /**
     * {@code DELETE  /service-optimisations/:id} : delete the "id" serviceOptimisation.
     *
     * @param id the id of the serviceOptimisation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/service-optimisations/{id}")
    public ResponseEntity<Void> deleteServiceOptimisation(@PathVariable Long id) {
        log.debug("REST request to delete ServiceOptimisation : {}", id);
        serviceOptimisationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
