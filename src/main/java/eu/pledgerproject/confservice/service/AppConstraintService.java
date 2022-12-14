package eu.pledgerproject.confservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.AppConstraint;

/**
 * Service Interface for managing {@link AppConstraint}.
 */
public interface AppConstraintService {


    /**
     * save a appConstraint.
     *
     * @param appConstraint the entity to save.
     * @return the persisted entity.
     */
	AppConstraint save(AppConstraint appConstraint);
	
    /**
     * expose a appConstraint.
     *
     * @param appConstraint the entity to expose.
     * @return the persisted entity.
     */
    void expose(AppConstraint appConstraint);

    /**
     * unexpose a appConstraint.
     *
     * @param appConstraint the entity to unexpose.
     * @return the persisted entity.
     */
    void unexpose(AppConstraint appConstraint);
    
    /**
     * Get all the appConstraints.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppConstraint> findAll(Pageable pageable);


    List<AppConstraint> findByServiceDstCategoryAndValueType(Long serviceSourceID, String category, String valueType);
    
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
