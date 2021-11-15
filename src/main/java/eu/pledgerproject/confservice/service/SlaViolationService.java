package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.SlaViolation;

/**
 * Service Interface for managing {@link SlaViolation}.
 */
public interface SlaViolationService {

    /**
     * Save a slaViolation.
     *
     * @param slaViolation the entity to save.
     * @return the persisted entity.
     */
    SlaViolation save(SlaViolation slaViolation);

    /**
     * Get all the slaViolations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SlaViolation> findAll(Pageable pageable);


    /**
     * Get the "id" slaViolation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SlaViolation> findOne(Long id);

    /**
     * Delete the "id" slaViolation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
