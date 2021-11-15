package eu.pledgerproject.confservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.InfrastructureProvider;

/**
 * Spring Data  repository for the InfrastructureProvider entity.
 */
@Repository
public interface InfrastructureProviderRepository extends JpaRepository<InfrastructureProvider, Long> {
	@Query(value = "select project.infrastructure.infrastructureProvider from Project project where project.serviceProvider.name = :serviceProviderName")
	Page<InfrastructureProvider> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select infrastructureProvider from InfrastructureProvider infrastructureProvider where infrastructureProvider.name = :infrastructureProviderName")
	Page<InfrastructureProvider> findByNamePageable(Pageable pageable, @Param("infrastructureProviderName") String infrastructureProviderName);

	@Query(value = "select infrastructureProvider from InfrastructureProvider infrastructureProvider where infrastructureProvider.name = :infrastructureProviderName")
	Optional<InfrastructureProvider> findByName(@Param("infrastructureProviderName") String infrastructureProviderName);


}
