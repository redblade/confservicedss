package eu.pledgerproject.confservice.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.pledgerproject.confservice.domain.OptimisationReport;
import eu.pledgerproject.confservice.service.OptimisationReportService;
import io.github.jhipster.web.util.PaginationUtil;


@RestController
@RequestMapping("/api")
public class OptimisationReportResource {

    private final Logger log = LoggerFactory.getLogger(OptimisationReportResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OptimisationReportService optimisationReportService;

    public OptimisationReportResource(OptimisationReportService optimisationReportService) {
        this.optimisationReportService = optimisationReportService;
    }
    /**
     * {@code GET  /optimisation-reports} : get all the optimisationReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of optimisationReports in body.
     */
    @GetMapping("/optimisation-reports")
    public ResponseEntity<List<OptimisationReport>> getAllOptimisationReports(Pageable pageable) {
        log.debug("REST request to get a page of OptimisationReports");
        Page<OptimisationReport> page = optimisationReportService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }


}
