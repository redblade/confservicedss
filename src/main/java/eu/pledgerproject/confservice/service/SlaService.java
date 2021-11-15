package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.Sla;

/**
 * Service Interface for managing {@link Sla}.
 */
public interface SlaService {

    /**
     * Save a sla.
     *
     * @param sla the entity to save.
     * @return the persisted entity.
     */
    Sla save(Sla sla);

    /**
     * Get all the slas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Sla> findAll(Pageable pageable);


    /**
     * Get the "id" sla.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Sla> findOne(Long id);

    /**
     * Delete the "id" sla.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
