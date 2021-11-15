package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Sla;
import eu.pledgerproject.confservice.repository.SlaRepository;
import eu.pledgerproject.confservice.service.SlaService;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link SlaResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class SlaResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATION = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_EXPIRATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRATION = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private SlaRepository slaRepository;

    @Autowired
    private SlaService slaService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSlaMockMvc;

    private Sla sla;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sla createEntity(EntityManager em) {
        Sla sla = new Sla()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .creation(DEFAULT_CREATION)
            .expiration(DEFAULT_EXPIRATION);
        return sla;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sla createUpdatedEntity(EntityManager em) {
        Sla sla = new Sla()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .creation(UPDATED_CREATION)
            .expiration(UPDATED_EXPIRATION);
        return sla;
    }

    @BeforeEach
    public void initTest() {
        sla = createEntity(em);
    }

    @Test
    @Transactional
    public void createSla() throws Exception {
        int databaseSizeBeforeCreate = slaRepository.findAll().size();
        // Create the Sla
        restSlaMockMvc.perform(post("/api/slas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sla)))
            .andExpect(status().isCreated());

        // Validate the Sla in the database
        List<Sla> slaList = slaRepository.findAll();
        assertThat(slaList).hasSize(databaseSizeBeforeCreate + 1);
        Sla testSla = slaList.get(slaList.size() - 1);
        assertThat(testSla.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSla.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testSla.getCreation()).isEqualTo(DEFAULT_CREATION);
        assertThat(testSla.getExpiration()).isEqualTo(DEFAULT_EXPIRATION);
    }

    @Test
    @Transactional
    public void createSlaWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = slaRepository.findAll().size();

        // Create the Sla with an existing ID
        sla.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSlaMockMvc.perform(post("/api/slas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sla)))
            .andExpect(status().isBadRequest());

        // Validate the Sla in the database
        List<Sla> slaList = slaRepository.findAll();
        assertThat(slaList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllSlas() throws Exception {
        // Initialize the database
        slaRepository.saveAndFlush(sla);

        // Get all the slaList
        restSlaMockMvc.perform(get("/api/slas?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sla.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].creation").value(hasItem(DEFAULT_CREATION.toString())))
            .andExpect(jsonPath("$.[*].expiration").value(hasItem(DEFAULT_EXPIRATION.toString())));
    }
    
    @Test
    @Transactional
    public void getSla() throws Exception {
        // Initialize the database
        slaRepository.saveAndFlush(sla);

        // Get the sla
        restSlaMockMvc.perform(get("/api/slas/{id}", sla.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sla.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.creation").value(DEFAULT_CREATION.toString()))
            .andExpect(jsonPath("$.expiration").value(DEFAULT_EXPIRATION.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingSla() throws Exception {
        // Get the sla
        restSlaMockMvc.perform(get("/api/slas/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSla() throws Exception {
        // Initialize the database
        slaService.save(sla);

        int databaseSizeBeforeUpdate = slaRepository.findAll().size();

        // Update the sla
        Sla updatedSla = slaRepository.findById(sla.getId()).get();
        // Disconnect from session so that the updates on updatedSla are not directly saved in db
        em.detach(updatedSla);
        updatedSla
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .creation(UPDATED_CREATION)
            .expiration(UPDATED_EXPIRATION);

        restSlaMockMvc.perform(put("/api/slas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedSla)))
            .andExpect(status().isOk());

        // Validate the Sla in the database
        List<Sla> slaList = slaRepository.findAll();
        assertThat(slaList).hasSize(databaseSizeBeforeUpdate);
        Sla testSla = slaList.get(slaList.size() - 1);
        assertThat(testSla.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSla.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testSla.getCreation()).isEqualTo(UPDATED_CREATION);
        assertThat(testSla.getExpiration()).isEqualTo(UPDATED_EXPIRATION);
    }

    @Test
    @Transactional
    public void updateNonExistingSla() throws Exception {
        int databaseSizeBeforeUpdate = slaRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSlaMockMvc.perform(put("/api/slas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sla)))
            .andExpect(status().isBadRequest());

        // Validate the Sla in the database
        List<Sla> slaList = slaRepository.findAll();
        assertThat(slaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSla() throws Exception {
        // Initialize the database
        slaService.save(sla);

        int databaseSizeBeforeDelete = slaRepository.findAll().size();

        // Delete the sla
        restSlaMockMvc.perform(delete("/api/slas/{id}", sla.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Sla> slaList = slaRepository.findAll();
        assertThat(slaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
