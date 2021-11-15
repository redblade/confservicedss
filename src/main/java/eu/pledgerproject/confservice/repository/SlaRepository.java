package eu.pledgerproject.confservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Sla;

/**
 * Spring Data  repository for the Sla entity.
 */
@Repository
public interface SlaRepository extends JpaRepository<Sla, Long> {
	@Query(value = "select sla from Sla sla where sla.serviceProvider.name =:serviceProviderName")
	Page<Sla> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
}
