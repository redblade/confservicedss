package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.SteadyService;

/**
 * Service Interface for managing {@link SteadyService}.
 */
public interface SteadyServiceService {

	/**
     * Save a steadyService.
     *
     * @param steadyService the entity to save.
     * @return the persisted entity.
     */
    SteadyService save(SteadyService steadyService);

    /**
     * Get all the steadyServices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SteadyService> findAll(Pageable pageable);
    
    /**
     * Get the "id" steadyService.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SteadyService> findOne(Long id);
    
	/**
     * Delete the "id" steadyService.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
