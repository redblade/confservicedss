package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.AppConstraint;

/**
 * Service Interface for managing {@link AppConstraint}.
 */
public interface AppConstraintService {

    /**
     * Save a appConstraint.
     *
     * @param appConstraint the entity to save.
     * @return the persisted entity.
     */
    AppConstraint save(AppConstraint appConstraint);

    /**
     * Get all the appConstraints.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppConstraint> findAll(Pageable pageable);


    /**
     * Get the "id" appConstraint.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppConstraint> findOne(Long id);

    /**
     * Delete the "id" appConstraint.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
