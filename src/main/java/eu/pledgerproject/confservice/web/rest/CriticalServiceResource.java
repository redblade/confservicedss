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

import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.service.CriticalServiceService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.CriticalService}.
 */
@RestController
@RequestMapping("/api")
public class CriticalServiceResource {

    private final Logger log = LoggerFactory.getLogger(CriticalServiceResource.class);

    private static final String ENTITY_NAME = "criticalService";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CriticalServiceService criticalServiceService;

    public CriticalServiceResource(CriticalServiceService criticalServiceService) {
        this.criticalServiceService = criticalServiceService;
    }

    /**
     * {@code POST  /critical-services} : Create a new criticalService.
     *
     * @param criticalService the criticalService to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new criticalService, or with status {@code 400 (Bad Request)} if the criticalService has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/critical-services")
    public ResponseEntity<CriticalService> createCriticalService(@RequestBody CriticalService criticalService) throws URISyntaxException {
        log.debug("REST request to save CriticalService : {}", criticalService);
        if (criticalService.getId() != null) {
            throw new BadRequestAlertException("A new criticalService cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CriticalService result = criticalServiceService.save(criticalService);
        return ResponseEntity.created(new URI("/api/critical-services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /critical-services} : Updates an existing criticalService.
     *
     * @param criticalService the criticalService to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated criticalService,
     * or with status {@code 400 (Bad Request)} if the criticalService is not valid,
     * or with status {@code 500 (Internal Server Error)} if the criticalService couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/critical-services")
    public ResponseEntity<CriticalService> updateCriticalService(@RequestBody CriticalService criticalService) throws URISyntaxException {
        log.debug("REST request to update CriticalService : {}", criticalService);
        if (criticalService.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CriticalService result = criticalServiceService.save(criticalService);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, criticalService.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /critical-services} : get all the criticalServices.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of criticalServices in body.
     */
    @GetMapping("/critical-services")
    public ResponseEntity<List<CriticalService>> getAllCriticalServices(Pageable pageable) {
        log.debug("REST request to get a page of CriticalServices");
        Page<CriticalService> page = criticalServiceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /critical-services/:id} : get the "id" criticalService.
     *
     * @param id the id of the criticalService to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the criticalService, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/critical-services/{id}")
    public ResponseEntity<CriticalService> getCriticalService(@PathVariable Long id) {
        log.debug("REST request to get CriticalService : {}", id);
        Optional<CriticalService> criticalService = criticalServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(criticalService);
    }

    /**
     * {@code DELETE  /critical-services/:id} : delete the "id" criticalService.
     *
     * @param id the id of the criticalService to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/critical-services/{id}")
    public ResponseEntity<Void> deleteCriticalService(@PathVariable Long id) {
        log.debug("REST request to delete CriticalService : {}", id);
        criticalServiceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
