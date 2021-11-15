package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.InfrastructureReport;

/**
 * Service Interface for managing {@link InfrastructureReport}.
 */
public interface InfrastructureReportService {

    /**
     * Save a infrastructureReport.
     *
     * @param infrastructureReport the entity to save.
     * @return the persisted entity.
     */
    InfrastructureReport save(InfrastructureReport infrastructureReport);

    /**
     * Get all the infrastructureReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InfrastructureReport> findAll(Pageable pageable, String categoryFilter);


    /**
     * Get the "id" infrastructureReport.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InfrastructureReport> findOne(Long id);

    /**
     * Delete the "id" infrastructureReport.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
