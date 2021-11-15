package eu.pledgerproject.confservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.SteadyService;

/**
 * Spring Data  repository for the SteadyService entity.
 */
@Repository
public interface SteadyServiceRepository extends JpaRepository<SteadyService, Long> {
	
	@Query(value = "select steadyService from SteadyService steadyService where steadyService.service.app.serviceProvider.name =:serviceProviderName ")
	Page<SteadyService> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select steadyService from SteadyService steadyService where steadyService.service.id= :serviceId")
	Optional<SteadyService> getByServiceID(@Param("serviceId") Long serviceId);
	
	@Query(value = "select steadyService from SteadyService steadyService order by steadyService.score desc")
	List<SteadyService> getOpenWithActionsToTakeOrdered();

}
