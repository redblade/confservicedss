package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.BenchmarkReport;

/**
 * Service Interface for managing {@link BenchmarkReport}.
 */
public interface BenchmarkReportService {

    /**
     * Save a benchmarkReport.
     *
     * @param benchmarkReport the entity to save.
     * @return the persisted entity.
     */
    BenchmarkReport save(BenchmarkReport benchmarkReport);

    /**
     * Get all the benchmarkReports.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<BenchmarkReport> findAll(Pageable pageable);


    /**
     * Get the "id" benchmarkReport.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BenchmarkReport> findOne(Long id);

    /**
     * Delete the "id" benchmarkReport.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
