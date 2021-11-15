package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.ServiceConstraint;

/**
 * Service Interface for managing {@link ServiceConstraint}.
 */
public interface ServiceConstraintService {

    /**
     * Save a serviceConstraint.
     *
     * @param serviceConstraint the entity to save.
     * @return the persisted entity.
     */
    ServiceConstraint save(ServiceConstraint serviceConstraint);

    /**
     * Get all the serviceConstraints.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ServiceConstraint> findAll(Pageable pageable);


    /**
     * Get the "id" serviceConstraint.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ServiceConstraint> findOne(Long id);

    /**
     * Delete the "id" serviceConstraint.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
