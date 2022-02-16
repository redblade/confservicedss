package eu.pledgerproject.confservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.Guarantee;

/**
 * Service Interface for managing {@link Guarantee}.
 */
public interface GuaranteeService {

    /**
     * Save a guarantee.
     *
     * @param guarantee the entity to save.
     * @return the persisted entity.
     */
    Guarantee save(Guarantee guarantee);

    /**
     * Get all the guarantees.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Guarantee> findAll(Pageable pageable);


    /**
     * Get the "id" guarantee.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Guarantee> findOne(Long id);
    
    /**
     * Get all the guarantees by SLA id
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    List<Guarantee> findBySLA(Long id);
    
    /**
     * Get the "id" guarantee.
     *
     * @param id the id of the guarantee entity.
     * @return the Prometheus Rule.
     */
    String readPrometheusRule(Long id);

    /**
     * Delete the "id" guarantee.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
