package eu.pledgerproject.confservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Penalty;

/**
 * Spring Data  repository for the Penalty entity.
 */
@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
	
	@Query(value = "select penalty from Penalty penalty where penalty.guarantee.sla.serviceProvider.name =:serviceProviderName")
	Page<Penalty> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
}
