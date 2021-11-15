package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Penalty;
import eu.pledgerproject.confservice.repository.PenaltyRepository;
import eu.pledgerproject.confservice.service.PenaltyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PenaltyResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class PenaltyResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final Double DEFAULT_VALUE = 1D;
    private static final Double UPDATED_VALUE = 2D;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private PenaltyService penaltyService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPenaltyMockMvc;

    private Penalty penalty;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Penalty createEntity(EntityManager em) {
        Penalty penalty = new Penalty()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .value(DEFAULT_VALUE);
        return penalty;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Penalty createUpdatedEntity(EntityManager em) {
        Penalty penalty = new Penalty()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);
        return penalty;
    }

    @BeforeEach
    public void initTest() {
        penalty = createEntity(em);
    }

    @Test
    @Transactional
    public void createPenalty() throws Exception {
        int databaseSizeBeforeCreate = penaltyRepository.findAll().size();
        // Create the Penalty
        restPenaltyMockMvc.perform(post("/api/penalties")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(penalty)))
            .andExpect(status().isCreated());

        // Validate the Penalty in the database
        List<Penalty> penaltyList = penaltyRepository.findAll();
        assertThat(penaltyList).hasSize(databaseSizeBeforeCreate + 1);
        Penalty testPenalty = penaltyList.get(penaltyList.size() - 1);
        assertThat(testPenalty.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPenalty.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testPenalty.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createPenaltyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = penaltyRepository.findAll().size();

        // Create the Penalty with an existing ID
        penalty.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPenaltyMockMvc.perform(post("/api/penalties")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(penalty)))
            .andExpect(status().isBadRequest());

        // Validate the Penalty in the database
        List<Penalty> penaltyList = penaltyRepository.findAll();
        assertThat(penaltyList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllPenalties() throws Exception {
        // Initialize the database
        penaltyRepository.saveAndFlush(penalty);

        // Get all the penaltyList
        restPenaltyMockMvc.perform(get("/api/penalties?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(penalty.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getPenalty() throws Exception {
        // Initialize the database
        penaltyRepository.saveAndFlush(penalty);

        // Get the penalty
        restPenaltyMockMvc.perform(get("/api/penalties/{id}", penalty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(penalty.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.doubleValue()));
    }
    @Test
    @Transactional
    public void getNonExistingPenalty() throws Exception {
        // Get the penalty
        restPenaltyMockMvc.perform(get("/api/penalties/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePenalty() throws Exception {
        // Initialize the database
        penaltyService.save(penalty);

        int databaseSizeBeforeUpdate = penaltyRepository.findAll().size();

        // Update the penalty
        Penalty updatedPenalty = penaltyRepository.findById(penalty.getId()).get();
        // Disconnect from session so that the updates on updatedPenalty are not directly saved in db
        em.detach(updatedPenalty);
        updatedPenalty
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .value(UPDATED_VALUE);

        restPenaltyMockMvc.perform(put("/api/penalties")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPenalty)))
            .andExpect(status().isOk());

        // Validate the Penalty in the database
        List<Penalty> penaltyList = penaltyRepository.findAll();
        assertThat(penaltyList).hasSize(databaseSizeBeforeUpdate);
        Penalty testPenalty = penaltyList.get(penaltyList.size() - 1);
        assertThat(testPenalty.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPenalty.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testPenalty.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingPenalty() throws Exception {
        int databaseSizeBeforeUpdate = penaltyRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPenaltyMockMvc.perform(put("/api/penalties")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(penalty)))
            .andExpect(status().isBadRequest());

        // Validate the Penalty in the database
        List<Penalty> penaltyList = penaltyRepository.findAll();
        assertThat(penaltyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePenalty() throws Exception {
        // Initialize the database
        penaltyService.save(penalty);

        int databaseSizeBeforeDelete = penaltyRepository.findAll().size();

        // Delete the penalty
        restPenaltyMockMvc.perform(delete("/api/penalties/{id}", penalty.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Penalty> penaltyList = penaltyRepository.findAll();
        assertThat(penaltyList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
