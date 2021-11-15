package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.CatalogApp;

/**
 * Service Interface for managing {@link CatalogApp}.
 */
public interface CatalogAppService {

    /**
     * Save a catalogApp.
     *
     * @param catalogApp the entity to save.
     * @return the persisted entity.
     */
    CatalogApp save(CatalogApp catalogApp);

    /**
     * Get all the catalogApps.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CatalogApp> findAll(Pageable pageable);


    /**
     * Get the "id" catalogApp.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CatalogApp> findOne(Long id);

    /**
     * Delete the "id" catalogApp.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
