package eu.pledgerproject.confservice.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.service.InfrastructureReportService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.InfrastructureReport}.
 */
@RestController
@RequestMapping("/api")
public class InfrastructureReportResource {

    private final Logger log = LoggerFactory.getLogger(InfrastructureReportResource.class);

    private static final String ENTITY_NAME = "infrastructureReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InfrastructureReportService infrastructureReportService;

    public InfrastructureReportResource(InfrastructureReportService infrastructureReportService) {
        this.infrastructureReportService = infrastructureReportService;
    }

    /**
     * {@code POST  /infrastructure-reports} : Create a new infrastructureReport.
     *
     * @param infrastructureReport the infrastructureReport to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new infrastructureReport, or with status {@code 400 (Bad Request)} if the infrastructureReport has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/infrastructure-reports")
    public ResponseEntity<InfrastructureReport> createInfrastructureReport(@RequestBody InfrastructureReport infrastructureReport) throws URISyntaxException {
        log.debug("REST request to save InfrastructureReport : {}", infrastructureReport);
        if (infrastructureReport.getId() != null) {
            throw new BadRequestAlertException("A new infrastructureReport cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InfrastructureReport result = infrastructureReportService.save(infrastructureReport);
        return ResponseEntity.created(new URI("/api/infrastructure-reports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /infrastructure-reports} : Updates an existing infrastructureReport.
     *
     * @param infrastructureReport the infrastructureReport to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated infrastructureReport,
     * or with status {@code 400 (Bad Request)} if the infrastructureReport is not valid,
     * or with status {@code 500 (Internal Server Error)} if the infrastructureReport couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/infrastructure-reports")
    public ResponseEntity<InfrastructureReport> updateInfrastructureReport(@RequestBody InfrastructureReport infrastructureReport) throws URISyntaxException {
        log.debug("REST request to update InfrastructureReport : {}", infrastructureReport);
        if (infrastructureReport.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        InfrastructureReport result = infrastructureReportService.save(infrastructureReport);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, infrastructureReport.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /infrastructure-reports} : get all the infrastructureReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of infrastructureReports in body.
     */
    @GetMapping("/infrastructure-reports")
    public ResponseEntity<List<InfrastructureReport>> getAllInfrastructureReports(Pageable pageable, @RequestParam Map<String,String> allParams) {
        log.debug("REST request to get a page of InfrastructureReports");
        String categoryFilter = allParams.get("categoryFilter");
        if(categoryFilter.trim().isEmpty()) {
        	categoryFilter = null;
        }
        Page<InfrastructureReport> page = infrastructureReportService.findAll(pageable, categoryFilter);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /infrastructure-reports/:id} : get the "id" infrastructureReport.
     *
     * @param id the id of the infrastructureReport to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the infrastructureReport, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/infrastructure-reports/{id}")
    public ResponseEntity<InfrastructureReport> getInfrastructureReport(@PathVariable Long id) {
        log.debug("REST request to get InfrastructureReport : {}", id);
        Optional<InfrastructureReport> infrastructureReport = infrastructureReportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(infrastructureReport);
    }

    /**
     * {@code DELETE  /infrastructure-reports/:id} : delete the "id" infrastructureReport.
     *
     * @param id the id of the infrastructureReport to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/infrastructure-reports/{id}")
    public ResponseEntity<Void> deleteInfrastructureReport(@PathVariable Long id) {
        log.debug("REST request to delete InfrastructureReport : {}", id);
        infrastructureReportService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
