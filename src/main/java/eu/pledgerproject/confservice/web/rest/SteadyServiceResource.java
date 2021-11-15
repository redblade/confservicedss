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

import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.service.SteadyServiceService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.SteadyService}.
 */
@RestController
@RequestMapping("/api")
public class SteadyServiceResource {

    private final Logger log = LoggerFactory.getLogger(SteadyServiceResource.class);

    private static final String ENTITY_NAME = "steadyService";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SteadyServiceService steadyServiceService;

    public SteadyServiceResource(SteadyServiceService steadyServiceService) {
        this.steadyServiceService = steadyServiceService;
    }

    /**
     * {@code POST  /steady-services} : Create a new steadyService.
     *
     * @param steadyService the steadyService to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new steadyService, or with status {@code 400 (Bad Request)} if the steadyService has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/steady-services")
    public ResponseEntity<SteadyService> createSteadyService(@RequestBody SteadyService steadyService) throws URISyntaxException {
        log.debug("REST request to save SteadyService : {}", steadyService);
        if (steadyService.getId() != null) {
            throw new BadRequestAlertException("A new steadyService cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SteadyService result = steadyServiceService.save(steadyService);
        return ResponseEntity.created(new URI("/api/steady-services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /steady-services} : Updates an existing steadyService.
     *
     * @param steadyService the steadyService to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated steadyService,
     * or with status {@code 400 (Bad Request)} if the steadyService is not valid,
     * or with status {@code 500 (Internal Server Error)} if the steadyService couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/steady-services")
    public ResponseEntity<SteadyService> updateSteadyService(@RequestBody SteadyService steadyService) throws URISyntaxException {
        log.debug("REST request to update SteadyService : {}", steadyService);
        if (steadyService.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        SteadyService result = steadyServiceService.save(steadyService);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, steadyService.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /steady-services} : get all the steadyServices.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of steadyServices in body.
     */
    @GetMapping("/steady-services")
    public ResponseEntity<List<SteadyService>> getAllSteadyServices(Pageable pageable) {
        log.debug("REST request to get a page of SteadyServices");
        Page<SteadyService> page = steadyServiceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /steady-services/:id} : get the "id" steadyService.
     *
     * @param id the id of the steadyService to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the steadyService, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/steady-services/{id}")
    public ResponseEntity<SteadyService> getSteadyService(@PathVariable Long id) {
        log.debug("REST request to get SteadyService : {}", id);
        Optional<SteadyService> steadyService = steadyServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(steadyService);
    }

    /**
     * {@code DELETE  /steady-services/:id} : delete the "id" steadyService.
     *
     * @param id the id of the steadyService to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/steady-services/{id}")
    public ResponseEntity<Void> deleteSteadyService(@PathVariable Long id) {
        log.debug("REST request to delete SteadyService : {}", id);
        steadyServiceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
