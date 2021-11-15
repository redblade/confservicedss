package eu.pledgerproject.confservice.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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

import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.service.SlaViolationService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.SlaViolation}.
 */
@RestController
@RequestMapping("/api")
public class SlaViolationResource {

    private final Logger log = LoggerFactory.getLogger(SlaViolationResource.class);

    private static final String ENTITY_NAME = "slaViolation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SlaViolationService slaViolationService;

    public SlaViolationResource(SlaViolationService slaViolationService) {
        this.slaViolationService = slaViolationService;
    }

    /**
     * {@code POST  /sla-violations} : Create a new slaViolation.
     *
     * @param slaViolation the slaViolation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new slaViolation, or with status {@code 400 (Bad Request)} if the slaViolation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/sla-violations")
    public ResponseEntity<SlaViolation> createSlaViolation(@Valid @RequestBody SlaViolation slaViolation) throws URISyntaxException {
        log.debug("REST request to save SlaViolation : {}", slaViolation);
        if (slaViolation.getId() != null) {
            throw new BadRequestAlertException("A new slaViolation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SlaViolation result = slaViolationService.save(slaViolation);
        return ResponseEntity.created(new URI("/api/sla-violations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /sla-violations} : Updates an existing slaViolation.
     *
     * @param slaViolation the slaViolation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated slaViolation,
     * or with status {@code 400 (Bad Request)} if the slaViolation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the slaViolation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/sla-violations")
    public ResponseEntity<SlaViolation> updateSlaViolation(@Valid @RequestBody SlaViolation slaViolation) throws URISyntaxException {
        log.debug("REST request to update SlaViolation : {}", slaViolation);
        if (slaViolation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        SlaViolation result = slaViolationService.save(slaViolation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, slaViolation.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /sla-violations} : get all the slaViolations.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of slaViolations in body.
     */
    @GetMapping("/sla-violations")
    public ResponseEntity<List<SlaViolation>> getAllSlaViolations(Pageable pageable) {
        log.debug("REST request to get a page of SlaViolations");
        Page<SlaViolation> page = slaViolationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /sla-violations/:id} : get the "id" slaViolation.
     *
     * @param id the id of the slaViolation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the slaViolation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/sla-violations/{id}")
    public ResponseEntity<SlaViolation> getSlaViolation(@PathVariable Long id) {
        log.debug("REST request to get SlaViolation : {}", id);
        Optional<SlaViolation> slaViolation = slaViolationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(slaViolation);
    }

    /**
     * {@code DELETE  /sla-violations/:id} : delete the "id" slaViolation.
     *
     * @param id the id of the slaViolation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/sla-violations/{id}")
    public ResponseEntity<Void> deleteSlaViolation(@PathVariable Long id) {
        log.debug("REST request to delete SlaViolation : {}", id);
        slaViolationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
