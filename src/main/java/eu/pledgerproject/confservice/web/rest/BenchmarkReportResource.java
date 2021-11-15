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

import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.service.BenchmarkReportService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.BenchmarkReport}.
 */
@RestController
@RequestMapping("/api")
public class BenchmarkReportResource {

    private final Logger log = LoggerFactory.getLogger(BenchmarkReportResource.class);

    private static final String ENTITY_NAME = "benchmarkReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BenchmarkReportService benchmarkReportService;

    public BenchmarkReportResource(BenchmarkReportService benchmarkReportService) {
        this.benchmarkReportService = benchmarkReportService;
    }

    /**
     * {@code POST  /benchmark-reports} : Create a new benchmarkReport.
     *
     * @param benchmarkReport the benchmarkReport to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new benchmarkReport, or with status {@code 400 (Bad Request)} if the benchmarkReport has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/benchmark-reports")
    public ResponseEntity<BenchmarkReport> createBenchmarkReport(@RequestBody BenchmarkReport benchmarkReport) throws URISyntaxException {
        log.debug("REST request to save BenchmarkReport : {}", benchmarkReport);
        if (benchmarkReport.getId() != null) {
            throw new BadRequestAlertException("A new benchmarkReport cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BenchmarkReport result = benchmarkReportService.save(benchmarkReport);
        return ResponseEntity.created(new URI("/api/benchmark-reports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /benchmark-reports} : Updates an existing benchmarkReport.
     *
     * @param benchmarkReport the benchmarkReport to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated benchmarkReport,
     * or with status {@code 400 (Bad Request)} if the benchmarkReport is not valid,
     * or with status {@code 500 (Internal Server Error)} if the benchmarkReport couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/benchmark-reports")
    public ResponseEntity<BenchmarkReport> updateBenchmarkReport(@RequestBody BenchmarkReport benchmarkReport) throws URISyntaxException {
        log.debug("REST request to update BenchmarkReport : {}", benchmarkReport);
        if (benchmarkReport.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BenchmarkReport result = benchmarkReportService.save(benchmarkReport);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, benchmarkReport.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /benchmark-reports} : get all the benchmarkReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of benchmarkReports in body.
     */
    @GetMapping("/benchmark-reports")
    public ResponseEntity<List<BenchmarkReport>> getAllBenchmarkReports(Pageable pageable) {
        log.debug("REST request to get a page of BenchmarkReports");
        Page<BenchmarkReport> page = benchmarkReportService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /benchmark-reports/:id} : get the "id" benchmarkReport.
     *
     * @param id the id of the benchmarkReport to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the benchmarkReport, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/benchmark-reports/{id}")
    public ResponseEntity<BenchmarkReport> getBenchmarkReport(@PathVariable Long id) {
        log.debug("REST request to get BenchmarkReport : {}", id);
        Optional<BenchmarkReport> benchmarkReport = benchmarkReportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(benchmarkReport);
    }

    /**
     * {@code DELETE  /benchmark-reports/:id} : delete the "id" benchmarkReport.
     *
     * @param id the id of the benchmarkReport to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/benchmark-reports/{id}")
    public ResponseEntity<Void> deleteBenchmarkReport(@PathVariable Long id) {
        log.debug("REST request to delete BenchmarkReport : {}", id);
        benchmarkReportService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
