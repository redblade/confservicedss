package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.domain.SlaViolation;

/**
 * Spring Data  repository for the SlaViolation entity.
 */
@Repository
public interface SlaViolationRepository extends JpaRepository<SlaViolation, Long> {

	@Query(value = "select slaViolation from SlaViolation slaViolation order by slaViolation.timestamp desc")
	Page<SlaViolation> findAll(Pageable pageable);
	
	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.sla.serviceProvider.name = :serviceProviderName order by slaViolation.timestamp desc")
	Page<SlaViolation> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status = :status and slaViolation.sla.service =:service and slaViolation.timestamp > :timestamp order by slaViolation.timestamp")
	List<SlaViolation> findAllByServiceAndStatusSinceTimestampRegardlessOfOptimisationType(@Param("service") Service service, @Param("status") String status, @Param("timestamp") Instant timestamp);

	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status = :status and slaViolation.sla.service =:service and slaViolation.timestamp > :timestamp order by slaViolation.timestamp")
	List<SlaViolation> findAllByServiceAndStatusAndSinceTimestamp(@Param("service") Service service, @Param("status") String status, @Param("timestamp") Instant timestamp);
	
	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status = :status and slaViolation.sla.service.serviceOptimisation.optimisation = :optimisationType and slaViolation.sla.serviceProvider.name =:serviceProviderName and slaViolation.timestamp > :timestamp order by slaViolation.timestamp")
	List<SlaViolation> findAllByServiceProviderAndStatusAndServiceOptimisationTypeSinceTimestamp(@Param("serviceProviderName") String serviceProviderName, @Param("status") String status, @Param("optimisationType") String optimisationType, @Param("timestamp") Instant timestamp);

	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status = :status and slaViolation.sla.service =:service and slaViolation.sla.service.serviceOptimisation.optimisation =:optimisationType and slaViolation.timestamp > :timestamp order by slaViolation.timestamp")
	List<SlaViolation> findAllByServiceAndStatusAndServiceOptimisationTypeSinceTimestamp(@Param("service") Service service, @Param("status") String status, @Param("optimisationType") String optimisationType, @Param("timestamp") Instant timestamp);
	
	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status = :status")
	List<SlaViolation> findAllByStatus(@Param("status") String status);
	
	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status not like 'closed%' and slaViolation.sla.service =:service") 
	List<SlaViolation> findAllNotClosed(@Param("service") Service service);

	@Query(value = "select slaViolation from SlaViolation slaViolation where slaViolation.status not like 'closed%' and slaViolation.sla.service =:service and slaViolation.sla.service.serviceOptimisation.optimisation =:optimisationType") 
	List<SlaViolation> findAllNotClosedByServiceOptimisationType(@Param("service") Service service, @Param("optimisationType") String optimisationType);

}
