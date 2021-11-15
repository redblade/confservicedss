package eu.pledgerproject.confservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.ServiceOptimisation;

/**
 * Spring Data  repository for the ServiceOptimisation entity.
 */
@Repository
public interface ServiceOptimisationRepository extends JpaRepository<ServiceOptimisation, Long> {
	
	@Query(value = "select serviceOptimisation from ServiceOptimisation serviceOptimisation where serviceOptimisation.service.app.serviceProvider.name =:serviceProviderName ")
	Page<ServiceOptimisation> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	
	@Query(value = "select serviceOptimisation from ServiceOptimisation serviceOptimisation where serviceOptimisation.service.status = 'RUNNING' and serviceOptimisation.optimisation = :optimisation ")
	List<ServiceOptimisation> findServiceOptimisationOnRunningServices(@Param("optimisation") String optimisation);

}
