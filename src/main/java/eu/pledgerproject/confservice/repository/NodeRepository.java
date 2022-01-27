package eu.pledgerproject.confservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Node;

/**
 * Spring Data  repository for the Node entity.
 */
@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {
	@Query(value = "select node from Node node where node.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	Page<Node> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select node from Node node where node.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	List<Node> findAllAuthorizedSP(@Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select node from Node node where node.infrastructure in (select project.infrastructure from Project project where project.serviceProvider.name =:serviceProviderName)")
	List<Node> findBySP(@Param("serviceProviderName") String serviceProviderName);

	@Query(value = "select node from Node node where node.infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	Page<Node> findAllAuthorizedIP(Pageable pageable, @Param("infrastructureProviderName") String infrastructureProviderName);
	
	@Query(value = "select node from Node node where node.infrastructure in (select project.infrastructure from Project project where project.infrastructure.infrastructureProvider.name =:infrastructureProviderName)")
	List<Node> findAllAuthorizedIP(@Param("infrastructureProviderName") String infrastructureProviderName);

	@Query(value = "select infrastructure.nodeSets from Infrastructure infrastructure where infrastructure.id = :infrastructureId")
	List<Node> findAllNodesByInfrastructureId(@Param("infrastructureId") Long infrastructureId);
	
	@Query(value = "select node from Node node where node.name = :name")
	Optional<Node> findByName(@Param("name") String name);
	
	@Query(value = "select node from Node node where node.ipaddress like :ipaddress")
	Optional<Node> findByIpaddress(@Param("ipaddress") String ipaddress);
	
	
}
