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

import eu.pledgerproject.confservice.domain.CatalogApp;
import eu.pledgerproject.confservice.service.CatalogAppService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.CatalogApp}.
 */
@RestController
@RequestMapping("/api")
public class CatalogAppResource {

    private final Logger log = LoggerFactory.getLogger(CatalogAppResource.class);

    private static final String ENTITY_NAME = "catalogApp";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CatalogAppService catalogAppService;

    public CatalogAppResource(CatalogAppService catalogAppService) {
        this.catalogAppService = catalogAppService;
    }

    /**
     * {@code POST  /catalog-apps} : Create a new catalogApp.
     *
     * @param catalogApp the catalogApp to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new catalogApp, or with status {@code 400 (Bad Request)} if the catalogApp has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/catalog-apps")
    public ResponseEntity<CatalogApp> createCatalogApp(@Valid @RequestBody CatalogApp catalogApp) throws URISyntaxException {
        log.debug("REST request to save CatalogApp : {}", catalogApp);
        if (catalogApp.getId() != null) {
            throw new BadRequestAlertException("A new catalogApp cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CatalogApp result = catalogAppService.save(catalogApp);
        return ResponseEntity.created(new URI("/api/catalog-apps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /catalog-apps} : Updates an existing catalogApp.
     *
     * @param catalogApp the catalogApp to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated catalogApp,
     * or with status {@code 400 (Bad Request)} if the catalogApp is not valid,
     * or with status {@code 500 (Internal Server Error)} if the catalogApp couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/catalog-apps")
    public ResponseEntity<CatalogApp> updateCatalogApp(@Valid @RequestBody CatalogApp catalogApp) throws URISyntaxException {
        log.debug("REST request to update CatalogApp : {}", catalogApp);
        if (catalogApp.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CatalogApp result = catalogAppService.save(catalogApp);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, catalogApp.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /catalog-apps} : get all the catalogApps.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of catalogApps in body.
     */
    @GetMapping("/catalog-apps")
    public ResponseEntity<List<CatalogApp>> getAllCatalogApps(Pageable pageable) {
        log.debug("REST request to get a page of CatalogApps");
        Page<CatalogApp> page = catalogAppService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /catalog-apps/:id} : get the "id" catalogApp.
     *
     * @param id the id of the catalogApp to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the catalogApp, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/catalog-apps/{id}")
    public ResponseEntity<CatalogApp> getCatalogApp(@PathVariable Long id) {
        log.debug("REST request to get CatalogApp : {}", id);
        Optional<CatalogApp> catalogApp = catalogAppService.findOne(id);
        return ResponseUtil.wrapOrNotFound(catalogApp);
    }

    /**
     * {@code DELETE  /catalog-apps/:id} : delete the "id" catalogApp.
     *
     * @param id the id of the catalogApp to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/catalog-apps/{id}")
    public ResponseEntity<Void> deleteCatalogApp(@PathVariable Long id) {
        log.debug("REST request to delete CatalogApp : {}", id);
        catalogAppService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
