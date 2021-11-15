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

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.service.BenchmarkService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Benchmark}.
 */
@RestController
@RequestMapping("/api")
public class BenchmarkResource {

    private final Logger log = LoggerFactory.getLogger(BenchmarkResource.class);

    private static final String ENTITY_NAME = "benchmark";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BenchmarkService benchmarkService;

    public BenchmarkResource(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    /**
     * {@code POST  /benchmarks} : Create a new benchmark.
     *
     * @param benchmark the benchmark to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new benchmark, or with status {@code 400 (Bad Request)} if the benchmark has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/benchmarks")
    public ResponseEntity<Benchmark> createBenchmark(@Valid @RequestBody Benchmark benchmark) throws URISyntaxException {
        log.debug("REST request to save Benchmark : {}", benchmark);
        if (benchmark.getId() != null) {
            throw new BadRequestAlertException("A new benchmark cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Benchmark result = benchmarkService.save(benchmark);
        return ResponseEntity.created(new URI("/api/benchmarks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /benchmarks} : Updates an existing benchmark.
     *
     * @param benchmark the benchmark to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated benchmark,
     * or with status {@code 400 (Bad Request)} if the benchmark is not valid,
     * or with status {@code 500 (Internal Server Error)} if the benchmark couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/benchmarks")
    public ResponseEntity<Benchmark> updateBenchmark(@Valid @RequestBody Benchmark benchmark) throws URISyntaxException {
        log.debug("REST request to update Benchmark : {}", benchmark);
        if (benchmark.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Benchmark result = benchmarkService.save(benchmark);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, benchmark.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /benchmarks} : get all the benchmarks.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of benchmarks in body.
     */
    @GetMapping("/benchmarks")
    public ResponseEntity<List<Benchmark>> getAllBenchmarks(Pageable pageable) {
        log.debug("REST request to get a page of Benchmarks");
        Page<Benchmark> page = benchmarkService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /benchmarks/:id} : get the "id" benchmark.
     *
     * @param id the id of the benchmark to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the benchmark, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/benchmarks/{id}")
    public ResponseEntity<Benchmark> getBenchmark(@PathVariable Long id) {
        log.debug("REST request to get Benchmark : {}", id);
        Optional<Benchmark> benchmark = benchmarkService.findOne(id);
        return ResponseUtil.wrapOrNotFound(benchmark);
    }

    /**
     * {@code DELETE  /benchmarks/:id} : delete the "id" benchmark.
     *
     * @param id the id of the benchmark to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/benchmarks/{id}")
    public ResponseEntity<Void> deleteBenchmark(@PathVariable Long id) {
        log.debug("REST request to delete Benchmark : {}", id);
        benchmarkService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
