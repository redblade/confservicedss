package eu.pledgerproject.confservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eu.pledgerproject.confservice.domain.Event;

/**
 * Spring Data  repository for the Event entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	
	@Query(value = "select event from Event event where :severityFilter is null or :severityFilter = event.severity order by event.id desc")
	Page<Event> findAll(Pageable pageable, @Param("severityFilter") String severityFilter);
	
	@Query(value = "select event from Event event where (:severityFilter is null or :severityFilter = event.severity) and event.serviceProvider.name = :serviceProviderName order by event.id desc")
	Page<Event> findAllAuthorizedSP(Pageable pageable, @Param("serviceProviderName") String serviceProviderName, @Param("severityFilter") String severityFilter);

	@Modifying
	@Query(value = "delete from Event event where event.timestamp < :timestamp")
	void deleteOld(@Param("timestamp") Instant timestamp);
	
	@Modifying
	@Query(value = "delete from Event event")
	void deleteAll();
}
