package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.AppDeploymentOptions;

/**
 * Service Interface for managing {@link AppDeploymentOptions}.
 */
public interface AppDeploymentOptionsService {

    /**
     * Get all the appDeploymentOptions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppDeploymentOptions> findAll(Pageable pageable);


    /**
     * Get the "id" appDeploymentOptions.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppDeploymentOptions> findOne(Long id);

    
}
