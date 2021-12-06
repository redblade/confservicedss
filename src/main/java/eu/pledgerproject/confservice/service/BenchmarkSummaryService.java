package eu.pledgerproject.confservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.BenchmarkSummary;

/**
 * Service Interface for managing {@link BenchmarkSummary}.
 */
public interface BenchmarkSummaryService {

    /**
     * Get all the benchmarkSummarys.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<BenchmarkSummary> findAll(Pageable pageable);


}
