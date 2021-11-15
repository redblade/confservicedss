package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.InfrastructureProvider;

/**
 * Service Interface for managing {@link InfrastructureProvider}.
 */
public interface InfrastructureProviderService {

    /**
     * Save a infrastructureProvider.
     *
     * @param infrastructureProvider the entity to save.
     * @return the persisted entity.
     */
    InfrastructureProvider save(InfrastructureProvider infrastructureProvider);

    /**
     * Get all the infrastructureProviders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InfrastructureProvider> findAll(Pageable pageable);


    /**
     * Get the "id" infrastructureProvider.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InfrastructureProvider> findOne(Long id);

    /**
     * Delete the "id" infrastructureProvider.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
