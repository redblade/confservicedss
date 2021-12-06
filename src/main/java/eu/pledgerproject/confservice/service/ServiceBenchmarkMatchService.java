package eu.pledgerproject.confservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.ServiceBenchmarkMatch;

/**
 * Service Interface for managing {@link ServiceBenchmarkMatchSummary}.
 */
public interface ServiceBenchmarkMatchService {

    /**
     * Get all the ServiceBenchmarkMatchs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ServiceBenchmarkMatch> findAll(Pageable pageable);


}
