package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.domain.Node;

/**
 * Spring Data  repository for the BenchmarkReport entity.
 */
@Repository
public interface BenchmarkReportRepository extends JpaRepository<BenchmarkReport, Long> {
	
	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider.name = :serviceProviderName")
	Page<BenchmarkReport> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider is null")
	Page<BenchmarkReport> findAllPublic(Pageable pageable);
	
	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport group by benchmarkReport.node where benchmarkReport.benchmark.metric = :metric and benchmarkReport.benchmark.category like :category and benchmarkReport.node in (:nodeSet) order by benchmarkReport.mean desc")
	List<BenchmarkReport> findBenchmarkReportByCategoryMetricAndNodeSet(@Param("category") String category, @Param("metric") String metric, @Param("nodeSet") List<Node> nodeSet);
	
	@Modifying
	@Query(value = "delete from BenchmarkReport benchmarkReport where benchmarkReport.time < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);
}
