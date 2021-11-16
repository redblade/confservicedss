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

import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.service.GuaranteeService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Guarantee}.
 */
@RestController
@RequestMapping("/api")
public class GuaranteeResource {

    private final Logger log = LoggerFactory.getLogger(GuaranteeResource.class);

    private static final String ENTITY_NAME = "guarantee";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GuaranteeService guaranteeService;

    public GuaranteeResource(GuaranteeService guaranteeService) {
        this.guaranteeService = guaranteeService;
    }

    /**
     * {@code POST  /guarantees} : Create a new guarantee.
     *
     * @param guarantee the guarantee to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new guarantee, or with status {@code 400 (Bad Request)} if the guarantee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/guarantees")
    public ResponseEntity<Guarantee> createGuarantee(@Valid @RequestBody Guarantee guarantee) throws URISyntaxException {
        log.debug("REST request to save Guarantee : {}", guarantee);
        if (guarantee.getId() != null) {
            throw new BadRequestAlertException("A new guarantee cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Guarantee result = guaranteeService.save(guarantee);
        return ResponseEntity.created(new URI("/api/guarantees/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /guarantees} : Updates an existing guarantee.
     *
     * @param guarantee the guarantee to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated guarantee,
     * or with status {@code 400 (Bad Request)} if the guarantee is not valid,
     * or with status {@code 500 (Internal Server Error)} if the guarantee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/guarantees")
    public ResponseEntity<Guarantee> updateGuarantee(@Valid @RequestBody Guarantee guarantee) throws URISyntaxException {
        log.debug("REST request to update Guarantee : {}", guarantee);
        if (guarantee.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Guarantee result = guaranteeService.save(guarantee);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, guarantee.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /guarantees} : get all the guarantees.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of guarantees in body.
     */
    @GetMapping("/guarantees")
    public ResponseEntity<List<Guarantee>> getAllGuarantees(Pageable pageable) {
        log.debug("REST request to get a page of Guarantees");
        Page<Guarantee> page = guaranteeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /guarantees/prometheusrule/:id} : get the "id" guarantee.
     *
     * @param id the id of the Prometheus Rule for the guarantee to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Prometheus Rule for the guarantee, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/guarantees/prometheusrule/{id}/{namespace}")
    public ResponseEntity<String> getGuaranteePrometheusRule(@PathVariable Long id, @PathVariable String namespace) {
        log.debug("REST request to get Prometheus Rule for Guarantee : {}", id, namespace);
        String prometheusRule = guaranteeService.getPrometheusRules(id, namespace);
        return ResponseUtil.wrapOrNotFound(prometheusRule == null ? Optional.empty() : Optional.of(prometheusRule));
    }
    
    /**
     * {@code GET  /guarantees/:id} : get the "id" guarantee.
     *
     * @param id the id of the guarantee to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the guarantee, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/guarantees/sla/{id}")
    public ResponseEntity<List<Guarantee>> getGuaranteeBySLA(@PathVariable Long id) {
        log.debug("REST request to get a List of Guarantees by SLA id");
        List<Guarantee> result = guaranteeService.findBySLA(id);
        return ResponseEntity.ok().body(result);
    }
    
    /**
     * {@code GET  /guarantees/:id} : get the "id" guarantee.
     *
     * @param id the id of the guarantee to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the guarantee, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/guarantees/{id}")
    public ResponseEntity<Guarantee> getGuarantee(@PathVariable Long id) {
        log.debug("REST request to get Guarantee : {}", id);
        Optional<Guarantee> guarantee = guaranteeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(guarantee);
    }

    /**
     * {@code DELETE  /guarantees/:id} : delete the "id" guarantee.
     *
     * @param id the id of the guarantee to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/guarantees/{id}")
    public ResponseEntity<Void> deleteGuarantee(@PathVariable Long id) {
        log.debug("REST request to delete Guarantee : {}", id);
        guaranteeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
