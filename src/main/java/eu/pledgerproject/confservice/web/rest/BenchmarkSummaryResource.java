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

import eu.pledgerproject.confservice.domain.BenchmarkSummary;
import eu.pledgerproject.confservice.service.BenchmarkSummaryService;
import io.github.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Benchmark}.
 */
@RestController
@RequestMapping("/api")
public class BenchmarkSummaryResource {

    private final Logger log = LoggerFactory.getLogger(BenchmarkSummaryResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BenchmarkSummaryService benchmarkSummaryService;

    public BenchmarkSummaryResource(BenchmarkSummaryService benchmarkSummaryService) {
        this.benchmarkSummaryService = benchmarkSummaryService;
    }

   

    /**
     * {@code GET  /benchmark-summary} : get all the benchmark summary.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of benchmark summarys in body.
     */
    @GetMapping("/benchmark-summary")
    public ResponseEntity<List<BenchmarkSummary>> getAllBenchmarkSummarys(Pageable pageable) {
        log.debug("REST request to get a page of BenchmarkSummary");
        Page<BenchmarkSummary> page = benchmarkSummaryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
    
}
