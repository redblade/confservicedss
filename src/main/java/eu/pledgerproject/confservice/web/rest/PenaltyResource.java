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

import eu.pledgerproject.confservice.domain.Penalty;
import eu.pledgerproject.confservice.service.PenaltyService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Penalty}.
 */
@RestController
@RequestMapping("/api")
public class PenaltyResource {

    private final Logger log = LoggerFactory.getLogger(PenaltyResource.class);

    private static final String ENTITY_NAME = "penalty";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PenaltyService penaltyService;

    public PenaltyResource(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    /**
     * {@code POST  /penalties} : Create a new penalty.
     *
     * @param penalty the penalty to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new penalty, or with status {@code 400 (Bad Request)} if the penalty has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/penalties")
    public ResponseEntity<Penalty> createPenalty(@Valid @RequestBody Penalty penalty) throws URISyntaxException {
        log.debug("REST request to save Penalty : {}", penalty);
        if (penalty.getId() != null) {
            throw new BadRequestAlertException("A new penalty cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Penalty result = penaltyService.save(penalty);
        return ResponseEntity.created(new URI("/api/penalties/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /penalties} : Updates an existing penalty.
     *
     * @param penalty the penalty to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated penalty,
     * or with status {@code 400 (Bad Request)} if the penalty is not valid,
     * or with status {@code 500 (Internal Server Error)} if the penalty couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/penalties")
    public ResponseEntity<Penalty> updatePenalty(@Valid @RequestBody Penalty penalty) throws URISyntaxException {
        log.debug("REST request to update Penalty : {}", penalty);
        if (penalty.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Penalty result = penaltyService.save(penalty);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, penalty.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /penalties} : get all the penalties.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of penalties in body.
     */
    @GetMapping("/penalties")
    public ResponseEntity<List<Penalty>> getAllPenalties(Pageable pageable) {
        log.debug("REST request to get a page of Penalties");
        Page<Penalty> page = penaltyService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /penalties/:id} : get the "id" penalty.
     *
     * @param id the id of the penalty to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the penalty, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/penalties/{id}")
    public ResponseEntity<Penalty> getPenalty(@PathVariable Long id) {
        log.debug("REST request to get Penalty : {}", id);
        Optional<Penalty> penalty = penaltyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(penalty);
    }

    /**
     * {@code DELETE  /penalties/:id} : delete the "id" penalty.
     *
     * @param id the id of the penalty to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/penalties/{id}")
    public ResponseEntity<Void> deletePenalty(@PathVariable Long id) {
        log.debug("REST request to delete Penalty : {}", id);
        penaltyService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
