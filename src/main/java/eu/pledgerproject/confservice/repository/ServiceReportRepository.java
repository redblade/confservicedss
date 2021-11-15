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

import eu.pledgerproject.confservice.domain.ServiceReport;

/**
 * Spring Data  repository for the ServiceReport entity.
 */
@Repository
public interface ServiceReportRepository extends JpaRepository<ServiceReport, Long> {
	@Query(value = "select serviceReport from ServiceReport serviceReport where (:categoryFilter is null or :categoryFilter = serviceReport.category) and serviceReport.service.app.serviceProvider.name =:serviceProviderName order by id desc")
	Page<ServiceReport> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select serviceReport from ServiceReport serviceReport where :categoryFilter is null or :categoryFilter = serviceReport.category order by id desc")
	Page<ServiceReport> findAll(Pageable pageable, @Param("categoryFilter") String categoryFilter);
	
	@Query(value = "select max(serviceReport.value) from ServiceReport serviceReport where serviceReport.service.id = :serviceId and serviceReport.category = :category and serviceReport.key = :key and serviceReport.timestamp > :timestamp")
	Integer findMaxResourceUsedByServiceIdCategoryKeyTimestamp(@Param("serviceId") long serviceId, @Param("category") String category, @Param("key") String key, @Param("timestamp") Instant timestamp);
	
	@Query(value = "select serviceReport from ServiceReport serviceReport where serviceReport.service.id = :serviceId and serviceReport.category = :category and serviceReport.key = :key order by serviceReport.timestamp desc")
	List<ServiceReport> findLastByServiceIdCategoryKey(@Param("serviceId") long serviceId, @Param("category") String category, @Param("key") String key);
	
	@Query(value = "select serviceReport from ServiceReport serviceReport where serviceReport.service.id = :serviceId and serviceReport.category = :category and serviceReport.key = :name order by serviceReport.id desc")
	List<ServiceReport> findServiceReportByServiceIdCategoryName(@Param("serviceId") long serviceId, @Param("category") String category, @Param("name") String name);

	@Modifying
	@Query(value = "delete from ServiceReport serviceReport where serviceReport.timestamp < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);
}
