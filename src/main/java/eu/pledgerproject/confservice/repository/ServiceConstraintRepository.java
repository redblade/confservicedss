package eu.pledgerproject.confservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.ServiceConstraint;

/**
 * Spring Data  repository for the ServiceConstraint entity.
 */
@Repository
public interface ServiceConstraintRepository extends JpaRepository<ServiceConstraint, Long> {
	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint order by serviceConstraint.service,serviceConstraint.priority")
	Page<ServiceConstraint> findAll(Pageable pageable);
	
	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.id =:serviceId and serviceConstraint.name = :constraintName and serviceConstraint.category = :constraintCategory")
	Optional<ServiceConstraint> findByServiceIdConstraintNameGroupCategory(@Param("serviceId") Long serviceId, @Param("constraintName") String constraintName, @Param("constraintCategory") String constraintCategory);

	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.app.id =:appId")
	List<ServiceConstraint> findByServiceConstraintListByAppId(@Param("appId") Long appId);

	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.id =:serviceId and serviceConstraint.category = 'rule' order by serviceConstraint.priority desc")
	List<ServiceConstraint> findByServiceConstraintListByServiceIdAndRuleCategoryOrderedByPriority(@Param("serviceId") Long serviceId);
	
	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.id =:serviceId and serviceConstraint.category =:category")
	List<ServiceConstraint> findByServiceConstraintListByServiceIdAndCategory(@Param("serviceId") Long serviceId, @Param("category") String category);

	@Query(value = "select serviceConstraint.priority from ServiceConstraint serviceConstraint where serviceConstraint.service.id =:serviceId and serviceConstraint.category =:category")
	List<Integer> findPrioritiesByServiceConstraintListByServiceIdAndCategory(@Param("serviceId") Long serviceId, @Param("category") String category);

	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.id =:serviceId and serviceConstraint.category =:category and serviceConstraint.priority =:priority")
	List<ServiceConstraint> findByServiceConstraintListByServiceIdAndCategoryAndPriority(@Param("serviceId") Long serviceId, @Param("category") String category, @Param("priority") Integer priority);

	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.app.serviceProvider.name =:serviceProviderName order by serviceConstraint.service,serviceConstraint.priority")
	Page<ServiceConstraint> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select serviceConstraint from ServiceConstraint serviceConstraint where serviceConstraint.service.id = :serviceId and serviceConstraint.category = :category order by serviceConstraint.id desc")
	List<ServiceConstraint> findServiceConstraintByServiceIdCategory(@Param("serviceId") long serviceId, @Param("category") String category);

}
