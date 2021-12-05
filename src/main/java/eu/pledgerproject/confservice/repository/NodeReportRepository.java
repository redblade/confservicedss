package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;

/**
 * Spring Data  repository for the NodeReport entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NodeReportRepository extends JpaRepository<NodeReport, Long> {
	
	@Query(value = "select nodeReport from NodeReport nodeReport where :categoryFilter is null or :categoryFilter = nodeReport.category order by id desc")
	Page<NodeReport> findAll(Pageable pageable, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select nodeReport from NodeReport nodeReport where nodeReport.node.id = :nodeSourceID and nodeReport.nodeDestination.id = :nodeDestinationID and nodeReport.category = :category")
	Optional<NodeReport> getNodeReportByNodeSourceIDNodeDestinationIDCategory(@Param("nodeSourceID") Long nodeSourceID, @Param("nodeDestinationID") Long nodeDestinationID, @Param("category") String category);
	
	@Query(value = "select nodeReport from NodeReport nodeReport where (:categoryFilter is null or :categoryFilter = nodeReport.category) and nodeReport.node.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	Page<NodeReport> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName, @Param("categoryFilter") String categoryFilter);

	@Query(value = "select nodeReport from NodeReport nodeReport where (:categoryFilter is null or :categoryFilter = nodeReport.category) and nodeReport.node.infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	Page<NodeReport> findAllAuthorizedIP(Pageable pageable, @Param("infrastructureProviderName") String infrastructureProviderName, @Param("categoryFilter") String categoryFilter);

	@Query(value = "select nodeReport from NodeReport nodeReport where nodeReport.node.id = :nodeId and nodeReport.key = :key order by nodeReport.id desc")
	List<NodeReport> findNodeReportByNodeIdAndKey(@Param("nodeId") Long nodeId, @Param("key") String key);

	@Query(value = "select nodeReport from NodeReport nodeReport where nodeReport.key = 'latency' and nodeReport.node is not null and nodeReport.node in :nodeListSrc and nodeReport.nodeDestination is not null and nodeReport.nodeDestination in :nodeListDst and nodeReport.timestamp > :timestamp")
	List<NodeReport> getAverageLatencyAmongTwoNodeGroups(@Param("nodeListSrc") Set<Node> nodeListSrc, @Param("nodeListDst") Set<Node> nodeListDst, @Param("timestamp") Instant timestamp);
	
	@Modifying
	@Query(value = "delete from NodeReport nodeReport where nodeReport.timestamp < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);

}
