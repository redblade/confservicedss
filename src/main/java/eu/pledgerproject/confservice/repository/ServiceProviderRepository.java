package eu.pledgerproject.confservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.ServiceProvider;

/**
 * Spring Data  repository for the ServiceProvider entity.
 */
@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
	@Query(value = "select serviceProvider from ServiceProvider serviceProvider where serviceProvider.name =:serviceProviderName")
	Page<ServiceProvider> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	Optional<ServiceProvider> findByName(@Param("name") String name);
	
}
