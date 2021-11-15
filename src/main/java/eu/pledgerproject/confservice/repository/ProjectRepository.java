package eu.pledgerproject.confservice.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.Project;

/**
 * Spring Data  repository for the Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	@Query(value = "select project from Project project where project.serviceProvider.name =:serviceProviderName")
	Page<Project> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);
	
	@Query(value = "select projectSets from Infrastructure infrastructure where infrastructure.id = :infrastructureId")
	List<Project> getProjectListByInfrastructureId(@Param("infrastructureId") Long infrastructureId);
	
	@Query(value = "select projectSets from ServiceProvider serviceProvider where serviceProvider.id = :serviceProviderId")
	List<Project> getProjectListByServiceProviderId(@Param("serviceProviderId") Long serviceProviderId);
	
	@Query(value = "select project from Project project where project.serviceProvider.id = :serviceProviderId and project.infrastructure.id = :infrastructureId")
	Optional<Project> getProjectByServiceProviderIdAndInfrastructureId(@Param("serviceProviderId") Long serviceProviderId, @Param("infrastructureId") Long infrastructureId);

	@Query(value = "select distinct(project) from Project project where project.serviceProvider.id = :serviceProviderId and project.infrastructure in (select distinct(node.infrastructure) from Node node where node in :nodeSet)")
	List<Project> getProjectListByServiceProviderIdAndNodeSet(@Param("serviceProviderId") Long serviceProviderId, @Param("nodeSet") Collection<Node> nodeSet);


}
