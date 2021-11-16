package eu.pledgerproject.confservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Guarantee;

/**
 * Spring Data  repository for the Guarantee entity.
 */
@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
	@Query(value = "select sla.guaranteeSets from Sla sla where sla.serviceProvider.name =:serviceProviderName")
	Page<Guarantee> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select sla.guaranteeSets from Sla sla where sla.id =:slaID")
	List<Guarantee> findAllBySLA(@Param("slaID") Long slaID);

}
