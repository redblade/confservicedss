package eu.pledgerproject.confservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.pledgerproject.confservice.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
