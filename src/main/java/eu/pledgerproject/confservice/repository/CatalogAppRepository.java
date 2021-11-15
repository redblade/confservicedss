package eu.pledgerproject.confservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.CatalogApp;

/**
 * Spring Data  repository for the CatalogApp entity.
 */
@Repository
public interface CatalogAppRepository extends JpaRepository<CatalogApp, Long> {
	@Query(value = "select catalogApp from CatalogApp catalogApp where catalogApp.serviceProvider is null")
	Page<CatalogApp> findAllPublic(Pageable pageable);

	@Query(value = "select catalogApp from CatalogApp catalogApp  where catalogApp.serviceProvider.name = :serviceProviderName")
	Page<CatalogApp> findAllPrivate(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
}
