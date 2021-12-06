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

import eu.pledgerproject.confservice.domain.ServiceBenchmarkMatch;
import eu.pledgerproject.confservice.service.ServiceBenchmarkMatchService;
import io.github.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.ServiceBenchmarkMatch}.
 */
@RestController
@RequestMapping("/api")
public class ServiceBenchmarkMatchResource {

    private final Logger log = LoggerFactory.getLogger(ServiceBenchmarkMatchResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServiceBenchmarkMatchService serviceBenchmarkMatchService;

    public ServiceBenchmarkMatchResource(ServiceBenchmarkMatchService serviceBenchmarkMatchService) {
        this.serviceBenchmarkMatchService = serviceBenchmarkMatchService;
    }

    /**
     * {@code GET  /service-benchmark-match} : get all the service benchmark match.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of service benchmark match in body.
     */
    @GetMapping("/service-benchmark-match")
    public ResponseEntity<List<ServiceBenchmarkMatch>> getAllServiceBenchmarkMatchs(Pageable pageable) {
        log.debug("REST request to get a page of ServiceBenchmarkMatch");
        Page<ServiceBenchmarkMatch> page = serviceBenchmarkMatchService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
    
}
