package eu.pledgerproject.confservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.App;

/**
 * Spring Data  repository for the App entity.
 */
@Repository
public interface AppRepository extends JpaRepository<App, Long> {
	@Query(value = "select app from App app where app.serviceProvider.name =:serviceProviderName")
	List<App> findAllAuthorizedSP( @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select app from App app where app.serviceProvider.name =:serviceProviderName")
	Page<App> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select app from App app where app.serviceProvider.id =:serviceProviderId")
	List<App> getAppListByServiceProviderId(@Param("serviceProviderId") Long serviceProviderId);
}
