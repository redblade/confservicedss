package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.ServiceProvider;

/**
 * Spring Data  repository for the Service entity.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
	
	@Query(value = "select service from Service service where service.name = :name")
	Optional<Service> findByName(@Param("name") String name);
	
	@Query(value = "select service from Service service where service.status = 'RUNNING' ")
	List<Service> findAllRunning();

	@Query(value = "select service from Service service where service.status = 'RUNNING' and service.lastChangedStatus is not null and service.lastChangedStatus < :lastChangedStatus")
	List<Service> findAllOldRunning(@Param("lastChangedStatus") Instant lastChangedStatus);
	
	@Query(value = "select service from Service service where service.app.id =:appId ")
	List<Service> findAllByAppId(@Param("appId") Long appId);
	
	@Query(value = "select service from Service service where service.app.serviceProvider.id =:serviceProviderId ")
	List<Service> getServiceListByServiceProviderId(@Param("serviceProviderId") Long serviceProviderId);

	@Query(value = "select service from Service service where service.app.serviceProvider.name =:serviceProviderName ")
	List<Service> findAllAuthorizedSP(@Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select service from Service service where service.app.serviceProvider.name =:serviceProviderName ")
	Page<Service> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select service from Service service where service.status='RUNNING'")
	List<Service> getRunningServiceList();
	
	@Query(value = "select service from Service service where service.app.serviceProvider.id = :serviceProviderId and service.status='RUNNING' ")
	List<Service> getRunningServiceListByServiceProviderId(@Param("serviceProviderId") Long serviceProviderId);
	
	@Query(value = "select service from Service service where service.app.serviceProvider.id = :serviceProviderId and service.status='RUNNING' and service.serviceOptimisation.optimisation=:serviceOptimisation")
	List<Service> getRunningServiceListByServiceProviderAndServiceOptimisation(@Param("serviceProviderId") Long serviceProviderId, @Param("serviceOptimisation") String serviceOptimisation);
	
	@Query(value = "select service from Service service where service.app.serviceProvider = :serviceProvider and service.serviceOptimisation.optimisation = :serviceOptimisation and service.lastChangedStatus < :timestamp")
	List<Service> findServiceListByServiceProviderServiceOptimisationSinceTimestamp(@Param("serviceProvider") ServiceProvider serviceProvider, @Param("serviceOptimisation") String serviceOptimisation, @Param("timestamp") Instant timestamp);


}
