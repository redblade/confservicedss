package eu.pledgerproject.confservice.service;

import eu.pledgerproject.confservice.domain.Event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link Event}.
 */
public interface EventService {

    /**
     * Save a event.
     *
     * @param event the entity to save.
     * @return the persisted entity.
     */
    Event save(Event event);

    /**
     * Get all the events.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Event> findAll(Pageable pageable, String severityFilter);


    /**
     * Get the "id" event.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Event> findOne(Long id);

    /**
     * Delete the "id" event.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
    

    /**
     * Delete all the events.
     *
     */
    void deleteAll();
}
