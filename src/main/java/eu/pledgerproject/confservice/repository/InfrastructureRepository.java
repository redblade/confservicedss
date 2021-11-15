package eu.pledgerproject.confservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Infrastructure;

/**
 * Spring Data  repository for the Infrastructure entity.
 */
@Repository
public interface InfrastructureRepository extends JpaRepository<Infrastructure, Long> {
	@Query(value = "select infrastructure from Infrastructure infrastructure")// where infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	Page<Infrastructure> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select infrastructure from Infrastructure infrastructure where infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	Page<Infrastructure> findAllAuthorizedIP(Pageable pageable, @Param("infrastructureProviderName") String infrastructureProviderName);

	@Query(value = "select distinct(infrastructure) from Infrastructure infrastructure left join fetch infrastructure.nodeSets")
	List<Infrastructure> findAllWithNodes();
}
