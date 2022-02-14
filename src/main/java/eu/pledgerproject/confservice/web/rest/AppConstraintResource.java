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

import eu.pledgerproject.confservice.domain.AppConstraint;
import eu.pledgerproject.confservice.service.AppConstraintService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.AppConstraint}.
 */
@RestController
@RequestMapping("/api")
public class AppConstraintResource {

    private final Logger log = LoggerFactory.getLogger(AppConstraintResource.class);

    private static final String ENTITY_NAME = "appConstraint";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppConstraintService appConstraintService;

    public AppConstraintResource(AppConstraintService appConstraintService) {
        this.appConstraintService = appConstraintService;
    }

    /**
     * {@code POST  /app-constraints} : Create a new appConstraint.
     *
     * @param appConstraint the appConstraint to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appConstraint, or with status {@code 400 (Bad Request)} if the appConstraint has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/app-constraints")
    public ResponseEntity<AppConstraint> createAppConstraint(@RequestBody AppConstraint appConstraint) throws URISyntaxException {
        log.debug("REST request to save AppConstraint : {}", appConstraint);
        if (appConstraint.getId() != null) {
            throw new BadRequestAlertException("A new appConstraint cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AppConstraint result = appConstraintService.save(appConstraint);
        return ResponseEntity.created(new URI("/api/app-constraints/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    @PostMapping("/app-constraints/expose")
    public ResponseEntity<Void> expose(@RequestBody AppConstraint appConstraint) throws URISyntaxException {
        log.debug("REST request to expose AppConstraint : {}", appConstraint);
        appConstraintService.expose(appConstraint);
        return ResponseEntity.noContent().build();
    }

    
    @PostMapping("/app-constraints/unexpose")
    public ResponseEntity<Void> unexpose(@RequestBody AppConstraint appConstraint) throws URISyntaxException {
    	log.debug("REST request to unexpose AppConstraint : {}", appConstraint);
    	appConstraintService.unexpose(appConstraint);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * {@code PUT  /app-constraints} : Updates an existing appConstraint.
     *
     * @param appConstraint the appConstraint to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appConstraint,
     * or with status {@code 400 (Bad Request)} if the appConstraint is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appConstraint couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/app-constraints")
    public ResponseEntity<AppConstraint> updateAppConstraint(@RequestBody AppConstraint appConstraint) throws URISyntaxException {
        log.debug("REST request to update AppConstraint : {}", appConstraint);
        if (appConstraint.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AppConstraint result = appConstraintService.save(appConstraint);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, appConstraint.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /app-constraints} : get all the appConstraints.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appConstraints in body.
     */
    @GetMapping("/app-constraints")
    public ResponseEntity<List<AppConstraint>> getAllAppConstraints(Pageable pageable) {
        log.debug("REST request to get a page of AppConstraints");
        Page<AppConstraint> page = appConstraintService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /app-constraints/:id} : get the "id" appConstraint.
     *
     * @param id the id of the appConstraint to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appConstraint, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/app-constraints/{id}")
    public ResponseEntity<AppConstraint> getAppConstraint(@PathVariable Long id) {
        log.debug("REST request to get AppConstraint : {}", id);
        Optional<AppConstraint> appConstraint = appConstraintService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appConstraint);
    }

    /**
     * {@code DELETE  /app-constraints/:id} : delete the "id" appConstraint.
     *
     * @param id the id of the appConstraint to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/app-constraints/{id}")
    public ResponseEntity<Void> deleteAppConstraint(@PathVariable Long id) {
        log.debug("REST request to delete AppConstraint : {}", id);
        appConstraintService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
