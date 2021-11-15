package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.Benchmark;

/**
 * Service Interface for managing {@link Benchmark}.
 */
public interface BenchmarkService {

    /**
     * Save a benchmark.
     *
     * @param benchmark the entity to save.
     * @return the persisted entity.
     */
    Benchmark save(Benchmark benchmark);

    /**
     * Get all the benchmarks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Benchmark> findAll(Pageable pageable);


    /**
     * Get the "id" benchmark.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Benchmark> findOne(Long id);

    /**
     * Delete the "id" benchmark.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
