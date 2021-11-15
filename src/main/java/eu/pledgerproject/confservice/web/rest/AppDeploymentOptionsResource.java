package eu.pledgerproject.confservice.web.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.pledgerproject.confservice.domain.AppDeploymentOptions;
import eu.pledgerproject.confservice.service.AppDeploymentOptionsService;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.AppDeploymentOptions}.
 */
@RestController
@RequestMapping("/api")
public class AppDeploymentOptionsResource {

    private final Logger log = LoggerFactory.getLogger(AppDeploymentOptionsResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppDeploymentOptionsService appDeploymentOptionsService;

    public AppDeploymentOptionsResource(AppDeploymentOptionsService appDeploymentOptionsService) {
        this.appDeploymentOptionsService = appDeploymentOptionsService;
    }

    /**
     * {@code GET  /app-deployment-options} : get all the appDeploymentOptions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appDeploymentOptions in body.
     */
    @GetMapping("/app-deployment-options")
    public ResponseEntity<List<AppDeploymentOptions>> getAllAppDeploymentOptions(Pageable pageable) {
        log.debug("REST request to get a page of AppDeploymentOptions");
        Page<AppDeploymentOptions> page = appDeploymentOptionsService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /app-deployment-options/:id} : get the "id" appDeploymentOptions.
     *
     * @param id the id of the appDeploymentOptions to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appDeploymentOptions, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/app-deployment-options/{id}")
    public ResponseEntity<AppDeploymentOptions> getAppDeploymentOptions(@PathVariable Long id) {
        log.debug("REST request to get AppDeploymentOptions : {}", id);
        Optional<AppDeploymentOptions> appDeploymentOptions = appDeploymentOptionsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appDeploymentOptions);
    }

}
