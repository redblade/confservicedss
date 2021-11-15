package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.ServiceReport;

/**
 * Service Interface for managing {@link ServiceReport}.
 */
public interface ServiceReportService {

    /**
     * Save a serviceReport.
     *
     * @param serviceReport the entity to save.
     * @return the persisted entity.
     */
    ServiceReport save(ServiceReport serviceReport);

    /**
     * Get a serviceReport by serviceId and category.
     *
     * @param serviceId the service id.
     * @param category the category.
     * @param key the key.
     * @return the entity.
     */
    Optional<ServiceReport> findLastByServiceIdCategoryKey(Long serviceId, String category, String key);
    
    /**
     * Get all the serviceReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ServiceReport> findAll(Pageable pageable, String categoryFilter);


    /**
     * Get the "id" serviceReport.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ServiceReport> findOne(Long id);

    /**
     * Delete the "id" serviceReport.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
