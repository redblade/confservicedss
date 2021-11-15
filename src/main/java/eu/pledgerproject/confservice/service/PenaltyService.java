package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.Penalty;

/**
 * Service Interface for managing {@link Penalty}.
 */
public interface PenaltyService {

    /**
     * Save a penalty.
     *
     * @param penalty the entity to save.
     * @return the persisted entity.
     */
    Penalty save(Penalty penalty);

    /**
     * Get all the penalties.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Penalty> findAll(Pageable pageable);


    /**
     * Get the "id" penalty.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Penalty> findOne(Long id);

    /**
     * Delete the "id" penalty.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
