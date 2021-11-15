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

import eu.pledgerproject.confservice.domain.InfrastructureProvider;
import eu.pledgerproject.confservice.service.InfrastructureProviderService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.InfrastructureProvider}.
 */
@RestController
@RequestMapping("/api")
public class InfrastructureProviderResource {

    private final Logger log = LoggerFactory.getLogger(InfrastructureProviderResource.class);

    private static final String ENTITY_NAME = "infrastructureProvider";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InfrastructureProviderService infrastructureProviderService;

    public InfrastructureProviderResource(InfrastructureProviderService infrastructureProviderService) {
        this.infrastructureProviderService = infrastructureProviderService;
    }

    /**
     * {@code POST  /infrastructure-providers} : Create a new infrastructureProvider.
     *
     * @param infrastructureProvider the infrastructureProvider to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new infrastructureProvider, or with status {@code 400 (Bad Request)} if the infrastructureProvider has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/infrastructure-providers")
    public ResponseEntity<InfrastructureProvider> createInfrastructureProvider(@Valid @RequestBody InfrastructureProvider infrastructureProvider) throws URISyntaxException {
        log.debug("REST request to save InfrastructureProvider : {}", infrastructureProvider);
        if (infrastructureProvider.getId() != null) {
            throw new BadRequestAlertException("A new infrastructureProvider cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InfrastructureProvider result = infrastructureProviderService.save(infrastructureProvider);
        return ResponseEntity.created(new URI("/api/infrastructure-providers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /infrastructure-providers} : Updates an existing infrastructureProvider.
     *
     * @param infrastructureProvider the infrastructureProvider to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated infrastructureProvider,
     * or with status {@code 400 (Bad Request)} if the infrastructureProvider is not valid,
     * or with status {@code 500 (Internal Server Error)} if the infrastructureProvider couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/infrastructure-providers")
    public ResponseEntity<InfrastructureProvider> updateInfrastructureProvider(@Valid @RequestBody InfrastructureProvider infrastructureProvider) throws URISyntaxException {
        log.debug("REST request to update InfrastructureProvider : {}", infrastructureProvider);
        if (infrastructureProvider.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        InfrastructureProvider result = infrastructureProviderService.save(infrastructureProvider);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, infrastructureProvider.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /infrastructure-providers} : get all the infrastructureProviders.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of infrastructureProviders in body.
     */
    @GetMapping("/infrastructure-providers")
    public ResponseEntity<List<InfrastructureProvider>> getAllInfrastructureProviders(Pageable pageable) {
        log.debug("REST request to get a page of InfrastructureProviders");
        Page<InfrastructureProvider> page = infrastructureProviderService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /infrastructure-providers/:id} : get the "id" infrastructureProvider.
     *
     * @param id the id of the infrastructureProvider to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the infrastructureProvider, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/infrastructure-providers/{id}")
    public ResponseEntity<InfrastructureProvider> getInfrastructureProvider(@PathVariable Long id) {
        log.debug("REST request to get InfrastructureProvider : {}", id);
        Optional<InfrastructureProvider> infrastructureProvider = infrastructureProviderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(infrastructureProvider);
    }

    /**
     * {@code DELETE  /infrastructure-providers/:id} : delete the "id" infrastructureProvider.
     *
     * @param id the id of the infrastructureProvider to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/infrastructure-providers/{id}")
    public ResponseEntity<Void> deleteInfrastructureProvider(@PathVariable Long id) {
        log.debug("REST request to delete InfrastructureProvider : {}", id);
        infrastructureProviderService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
