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

import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.service.ServiceReportService;
import eu.pledgerproject.confservice.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.ServiceReport}.
 */
@RestController
@RequestMapping("/api")
public class ServiceReportResource {

    private final Logger log = LoggerFactory.getLogger(ServiceReportResource.class);

    private static final String ENTITY_NAME = "serviceReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServiceReportService serviceReportService;

    public ServiceReportResource(ServiceReportService serviceReportService) {
        this.serviceReportService = serviceReportService;
    }

    /**
     * {@code POST  /service-reports} : Create a new serviceReport.
     *
     * @param serviceReport the serviceReport to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new serviceReport, or with status {@code 400 (Bad Request)} if the serviceReport has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/service-reports")
    public ResponseEntity<ServiceReport> createServiceReport(@RequestBody ServiceReport serviceReport) throws URISyntaxException {
        log.debug("REST request to save ServiceReport : {}", serviceReport);
        if (serviceReport.getId() != null) {
            throw new BadRequestAlertException("A new serviceReport cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ServiceReport result = serviceReportService.save(serviceReport);
        return ResponseEntity.created(new URI("/api/service-reports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /service-reports} : Updates an existing serviceReport.
     *
     * @param serviceReport the serviceReport to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceReport,
     * or with status {@code 400 (Bad Request)} if the serviceReport is not valid,
     * or with status {@code 500 (Internal Server Error)} if the serviceReport couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/service-reports")
    public ResponseEntity<ServiceReport> updateServiceReport(@RequestBody ServiceReport serviceReport) throws URISyntaxException {
        log.debug("REST request to update ServiceReport : {}", serviceReport);
        if (serviceReport.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ServiceReport result = serviceReportService.save(serviceReport);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, serviceReport.getId().toString()))
            .body(result);
    }
    
    /**
     * {@code GET  /service-reports/:serviceId/:category/:key} : get the last serviceReport by serviceId, categoty, key 
     *
     * @param serviceId the service id.
     * @param category the category.
     * @param key the key.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceReport, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/service-reports/{serviceId}/{category}/{key}")
    public ResponseEntity<ServiceReport> getLastServiceReportByService(@PathVariable Long serviceId, @PathVariable String category, @PathVariable String key) {
        log.debug("REST request to get a page of ServiceReport by serviceId, category, key");
        Optional<ServiceReport> serviceReport = serviceReportService.findLastByServiceIdCategoryKey(serviceId, category, key);
        return ResponseUtil.wrapOrNotFound(serviceReport);
    }

    /**
     * {@code GET  /service-reports} : get all the serviceReports.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of serviceReports in body.
     */
    @GetMapping("/service-reports")
    public ResponseEntity<List<ServiceReport>> getAllServiceReports(Pageable pageable, @RequestParam Map<String,String> allParams) {
        log.debug("REST request to get a page of ServiceReports");
        String categoryFilter = allParams.get("categoryFilter");
        if(categoryFilter.trim().isEmpty()) {
        	categoryFilter = null;
        }
        Page<ServiceReport> page = serviceReportService.findAll(pageable, categoryFilter);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /service-reports/:id} : get the "id" serviceReport.
     *
     * @param id the id of the serviceReport to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceReport, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/service-reports/{id}")
    public ResponseEntity<ServiceReport> getServiceReport(@PathVariable Long id) {
        log.debug("REST request to get ServiceReport : {}", id);
        Optional<ServiceReport> serviceReport = serviceReportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(serviceReport);
    }

    /**
     * {@code DELETE  /service-reports/:id} : delete the "id" serviceReport.
     *
     * @param id the id of the serviceReport to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/service-reports/{id}")
    public ResponseEntity<Void> deleteServiceReport(@PathVariable Long id) {
        log.debug("REST request to delete ServiceReport : {}", id);
        serviceReportService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
