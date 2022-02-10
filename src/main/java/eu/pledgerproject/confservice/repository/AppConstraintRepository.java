package eu.pledgerproject.confservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.AppConstraint;

/**
 * Spring Data  repository for the AppConstraint entity.
 */
@Repository
public interface AppConstraintRepository extends JpaRepository<AppConstraint, Long> {
	@Query(value = "select appConstraint from AppConstraint appConstraint where appConstraint.serviceSource.app.serviceProvider.name =:serviceProviderName and appConstraint.serviceDestination.app.serviceProvider.name =:serviceProviderName")
	Page<AppConstraint> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select appConstraint from AppConstraint appConstraint where appConstraint.serviceDestination.id =:serviceDstID and appConstraint.category =:category and appConstraint.valueType =:valueType")
	List<AppConstraint> findByServiceDstCategoryAndValueType(@Param("serviceDstID") Long serviceDstID, @Param("category") String category, @Param("valueType") String valueType);

}
