package eu.pledgerproject.confservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Benchmark;

/**
 * Spring Data  repository for the Benchmark entity.
 */
@Repository
public interface BenchmarkRepository extends JpaRepository<Benchmark, Long> {
	@Query(value = "select benchmark from Benchmark benchmark where benchmark.name =:benchmarkName and benchmark.serviceProvider.name =:serviceProviderName")
	Optional<Benchmark> findByBenchmarkNameAndServiceProviderName(@Param("benchmarkName") String benchmarkName, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select benchmark from Benchmark benchmark where benchmark.category like :category order by benchmark.name")
	List<Benchmark> findByCategoryLike(@Param("category") String category);
	
	@Query(value = "select benchmark from Benchmark benchmark where benchmark.serviceProvider.name = :serviceProviderName")
	Page<Benchmark> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select benchmark from Benchmark benchmark where benchmark.serviceProvider is null")
	Page<Benchmark> findAllPublic(Pageable pageable);

}
