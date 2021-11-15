package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.NodeReport;

/**
 * Service Interface for managing {@link NodeReport}.
 */
public interface NodeReportService {

    /**
     * Save a nodeReport.
     *
     * @param nodeReport the entity to save.
     * @return the persisted entity.
     */
    NodeReport save(NodeReport nodeReport);

    /**
     * Get all the nodeReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NodeReport> findAll(Pageable pageable, String categoryFilter);


    /**
     * Get the "id" nodeReport.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<NodeReport> findOne(Long id);

    /**
     * Delete the "id" nodeReport.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
