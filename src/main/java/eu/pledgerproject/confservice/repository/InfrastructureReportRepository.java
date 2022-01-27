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

import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.domain.ServiceReport;

/**
 * Spring Data  repository for the InfrastructureReport entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InfrastructureReportRepository extends JpaRepository<InfrastructureReport, Long> {

	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where :categoryFilter is null or :categoryFilter = infrastructureReport.category order by id desc")
	List<InfrastructureReport> findAll(@Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where :categoryFilter is null or :categoryFilter = infrastructureReport.category order by id desc")
	Page<InfrastructureReport> findAll(Pageable pageable, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where (:categoryFilter is null or :categoryFilter = infrastructureReport.category) and infrastructureReport.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	Page<InfrastructureReport> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName, @Param("categoryFilter") String categoryFilter);

	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where (:categoryFilter is null or :categoryFilter = infrastructureReport.category) and infrastructureReport.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	List<InfrastructureReport> findAllAuthorizedSP(@Param("serviceProviderName") String serviceProviderName, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where (:categoryFilter is null or :categoryFilter = infrastructureReport.category) and infrastructureReport.infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	Page<InfrastructureReport> findAllAuthorizedIP(Pageable pageable, @Param("infrastructureProviderName") String infrastructureProviderName, @Param("categoryFilter") String categoryFilter);

	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where (:categoryFilter is null or :categoryFilter = infrastructureReport.category) and infrastructureReport.infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	List<InfrastructureReport> findAllAuthorizedIP(@Param("infrastructureProviderName") String infrastructureProviderName, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select infrastructureReport from InfrastructureReport infrastructureReport where infrastructureReport.group =:serviceProviderName and infrastructureReport.key =:key and infrastructureReport.group='*' and infrastructureReport.infrastructure =:infrastructure order by infrastructureReport.id desc")
	List<InfrastructureReport> findInfrastructureReportListByServiceProviderNameAndKeyAndInfrastructure(@Param("serviceProviderName") String serviceProviderName, @Param("key") String key, @Param("infrastructure") Infrastructure infrastructure);
	
	@Modifying
	@Query(value = "delete from InfrastructureReport infrastructureReport where infrastructureReport.timestamp < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);
}
