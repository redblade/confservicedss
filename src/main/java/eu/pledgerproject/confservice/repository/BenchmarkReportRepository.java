package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.domain.Node;

/**
 * Spring Data  repository for the BenchmarkReport entity.
 */
@Repository
public interface BenchmarkReportRepository extends JpaRepository<BenchmarkReport, Long> {
	
	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider.name = :serviceProviderName")
	Page<BenchmarkReport> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider is null or benchmarkReport.benchmark.serviceProvider.name = :serviceProviderName")
	Page<BenchmarkReport> findAllPublicOrAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider.name = :serviceProviderName")
	List<BenchmarkReport> findAllAuthorizedSP(@Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider is null")
	Page<BenchmarkReport> findAllPublic(Pageable pageable);

	@Query(value = "select benchmarkReport from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.serviceProvider is null")
	List<BenchmarkReport> findAllPublic();
	
	//USED by BenchmarkManager.getBestNodeUsingBenchmark
	@Query(value = "select benchmarkReport.node, max(benchmarkReport.mean),max(benchmarkReport.time) from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.name = :benchmarkName and benchmarkReport.metric = :metric group by benchmarkReport.node having benchmarkReport.node in :nodeSet")
	List<Object> findNodeMeanFromBenchmarkReportByBenchmarkNameMetricAndNodeSet(@Param("benchmarkName") String benchmarkName, @Param("metric") String metric, @Param("nodeSet") Set<Node> nodeSet);

	//USED by BenchmarkManager.getBestNodeUsingBenchmark
	@Query(value = "select benchmarkReport.node, max(benchmarkReport.mean),max(benchmarkReport.time) from BenchmarkReport benchmarkReport where benchmarkReport.benchmark.category like :category and benchmarkReport.metric = :metric group by benchmarkReport.node having benchmarkReport.node in :nodeSet")
	List<Object> findNodeMeanFromBenchmarkReportByCategoryMetricAndNodeSet(@Param("category") String category, @Param("metric") String metric, @Param("nodeSet") Set<Node> nodeSet);

	//USED by BenchmarkSummaryServiceImpl.createBenchmarkSummary
	@Query(value = "select benchmarkReport.benchmark, benchmarkReport.node, max(benchmarkReport.mean),max(benchmarkReport.time) from BenchmarkReport benchmarkReport where benchmarkReport.metric = :metric group by benchmarkReport.benchmark,benchmarkReport.node having benchmarkReport.benchmark in :benchmarkSet")
	List<Object> findBenchmarkNodeMeanFromBenchmarkReportMetricAndBenchmarkList(@Param("metric") String metric, @Param("benchmarkSet") List<Benchmark> benchmarkList);

	@Modifying
	@Query(value = "delete from BenchmarkReport benchmarkReport where benchmarkReport.time < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);
}
