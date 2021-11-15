package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.CriticalService;

/**
 * Service Interface for managing {@link CriticalService}.
 */
public interface CriticalServiceService {

	/**
     * Save a criticalService.
     *
     * @param criticalService the entity to save.
     * @return the persisted entity.
     */
    CriticalService save(CriticalService criticalService);
	
    /**
     * Get all the criticalServices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CriticalService> findAll(Pageable pageable);
    
    /**
     * Get the "id" criticalService.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CriticalService> findOne(Long id);
    
    
    /**
     * Delete the "id" criticalService.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

}
