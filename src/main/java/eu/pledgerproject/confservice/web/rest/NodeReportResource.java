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

import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.service.NodeReportService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.NodeReport}.
 */
@RestController
@RequestMapping("/api")
public class NodeReportResource {

    private final Logger log = LoggerFactory.getLogger(NodeReportResource.class);

    private static final String ENTITY_NAME = "nodeReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NodeReportService nodeReportService;

    public NodeReportResource(NodeReportService nodeReportService) {
        this.nodeReportService = nodeReportService;
    }

    /**
     * {@code POST  /node-reports} : Create a new nodeReport.
     *
     * @param nodeReport the nodeReport to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new nodeReport, or with status {@code 400 (Bad Request)} if the nodeReport has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/node-reports")
    public ResponseEntity<NodeReport> createNodeReport(@RequestBody NodeReport nodeReport) throws URISyntaxException {
        log.debug("REST request to save NodeReport : {}", nodeReport);
        if (nodeReport.getId() != null) {
            throw new BadRequestAlertException("A new nodeReport cannot already have an ID", ENTITY_NAME, "idexists");
        }
        NodeReport result = nodeReportService.save(nodeReport);
        return ResponseEntity.created(new URI("/api/node-reports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /node-reports} : Updates an existing nodeReport.
     *
     * @param nodeReport the nodeReport to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated nodeReport,
     * or with status {@code 400 (Bad Request)} if the nodeReport is not valid,
     * or with status {@code 500 (Internal Server Error)} if the nodeReport couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/node-reports")
    public ResponseEntity<NodeReport> updateNodeReport(@RequestBody NodeReport nodeReport) throws URISyntaxException {
        log.debug("REST request to update NodeReport : {}", nodeReport);
        if (nodeReport.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        NodeReport result = nodeReportService.save(nodeReport);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, nodeReport.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /node-reports} : get all the nodeReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of nodeReports in body.
     */
    @GetMapping("/node-reports")
    public ResponseEntity<List<NodeReport>> getAllNodeReports(Pageable pageable, @RequestParam Map<String,String> allParams) {
        log.debug("REST request to get a page of NodeReports");
        String categoryFilter = allParams.get("categoryFilter");
        if(categoryFilter.trim().isEmpty()) {
        	categoryFilter = null;
        }
        Page<NodeReport> page = nodeReportService.findAll(pageable, categoryFilter);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /node-reports/:id} : get the "id" nodeReport.
     *
     * @param id the id of the nodeReport to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the nodeReport, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/node-reports/{id}")
    public ResponseEntity<NodeReport> getNodeReport(@PathVariable Long id) {
        log.debug("REST request to get NodeReport : {}", id);
        Optional<NodeReport> nodeReport = nodeReportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(nodeReport);
    }

    /**
     * {@code DELETE  /node-reports/:id} : delete the "id" nodeReport.
     *
     * @param id the id of the nodeReport to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/node-reports/{id}")
    public ResponseEntity<Void> deleteNodeReport(@PathVariable Long id) {
        log.debug("REST request to delete NodeReport : {}", id);
        nodeReportService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
