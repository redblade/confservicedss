package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.ServiceOptimisation;

/**
 * Service Interface for managing {@link ServiceOptimisation}.
 */
public interface ServiceOptimisationService {

    /**
     * Save a serviceOptimisation.
     *
     * @param serviceOptimisation the entity to save.
     * @return the persisted entity.
     */
    ServiceOptimisation save(ServiceOptimisation serviceOptimisation);

    /**
     * Get all the serviceOptimisations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ServiceOptimisation> findAll(Pageable pageable);


    /**
     * Get the "id" serviceOptimisation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ServiceOptimisation> findOne(Long id);

    /**
     * Delete the "id" serviceOptimisation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
