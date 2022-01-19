package eu.pledgerproject.confservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.OptimisationReport;

/**
 * Service Interface for managing {@link OptimisationReport}.
 */
public interface OptimisationReportService {

    /**
     * Get all the optimisationReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<OptimisationReport> findAll(Pageable page);


}
