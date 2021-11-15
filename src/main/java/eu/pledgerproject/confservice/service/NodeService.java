package eu.pledgerproject.confservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.pledgerproject.confservice.domain.Node;

/**
 * Service Interface for managing {@link Node}.
 */
public interface NodeService {

    /**
     * Save a node.
     *
     * @param node the entity to save.
     * @return the persisted entity.
     */
    Node save(Node node);

    /**
     * Get all the nodes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Node> findAll(Pageable pageable);


    /**
     * Get the "id" node.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Node> findOne(Long id);

    /**
     * Delete the "id" node.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
