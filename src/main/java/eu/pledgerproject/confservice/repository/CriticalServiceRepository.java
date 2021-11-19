package eu.pledgerproject.confservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.CriticalService;

/**
 * Spring Data  repository for the CriticalService entity.
 */
@Repository
public interface CriticalServiceRepository extends JpaRepository<CriticalService, Long> {
	@Query(value = "select criticalService from CriticalService criticalService where criticalService.service.app.serviceProvider.name =:serviceProviderName ")
	Page<CriticalService> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select criticalService from CriticalService criticalService where criticalService.timestampProcessed is null and criticalService.service.id= :serviceId")
	Optional<CriticalService> getOpenByServiceID(@Param("serviceId") Long serviceId);
	
	@Query(value = "select criticalService from CriticalService criticalService where criticalService.service.id= :serviceId")
	Optional<CriticalService> getByServiceID(@Param("serviceId") Long serviceId);
	
	@Query(value = "select criticalService from CriticalService criticalService order by criticalService.score desc")
	List<CriticalService> getAllOrderedByScoreDesc();
}
