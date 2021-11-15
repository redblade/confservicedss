package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.SteadyService;
import eu.pledgerproject.confservice.repository.SteadyServiceRepository;
import eu.pledgerproject.confservice.service.SteadyServiceService;

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
 * Integration tests for the {@link SteadyServiceResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class SteadyServiceResourceIT {

    private static final Instant DEFAULT_TIMESTAMP_CREATED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP_CREATED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_TIMESTAMP_PROCESSED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP_PROCESSED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ACTION_TAKEN = "AAAAAAAAAA";
    private static final String UPDATED_ACTION_TAKEN = "BBBBBBBBBB";

    private static final Long DEFAULT_SCORE = 1L;
    private static final Long UPDATED_SCORE = 2L;

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final Integer DEFAULT_MONITORING_PERIOD_SEC = 1;
    private static final Integer UPDATED_MONITORING_PERIOD_SEC = 2;

    @Autowired
    private SteadyServiceRepository steadyServiceRepository;

    @Autowired
    private SteadyServiceService steadyServiceService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSteadyServiceMockMvc;

    private SteadyService steadyService;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SteadyService createEntity(EntityManager em) {
        SteadyService steadyService = new SteadyService()
            .timestampCreated(DEFAULT_TIMESTAMP_CREATED)
            .timestampProcessed(DEFAULT_TIMESTAMP_PROCESSED)
            .actionTaken(DEFAULT_ACTION_TAKEN)
            .score(DEFAULT_SCORE)
            .details(DEFAULT_DETAILS)
            .monitoringPeriodSec(DEFAULT_MONITORING_PERIOD_SEC);
        return steadyService;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SteadyService createUpdatedEntity(EntityManager em) {
        SteadyService steadyService = new SteadyService()
            .timestampCreated(UPDATED_TIMESTAMP_CREATED)
            .timestampProcessed(UPDATED_TIMESTAMP_PROCESSED)
            .actionTaken(UPDATED_ACTION_TAKEN)
            .score(UPDATED_SCORE)
            .details(UPDATED_DETAILS)
            .monitoringPeriodSec(UPDATED_MONITORING_PERIOD_SEC);
        return steadyService;
    }

    @BeforeEach
    public void initTest() {
        steadyService = createEntity(em);
    }

    @Test
    @Transactional
    public void createSteadyService() throws Exception {
        int databaseSizeBeforeCreate = steadyServiceRepository.findAll().size();
        // Create the SteadyService
        restSteadyServiceMockMvc.perform(post("/api/steady-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(steadyService)))
            .andExpect(status().isCreated());

        // Validate the SteadyService in the database
        List<SteadyService> steadyServiceList = steadyServiceRepository.findAll();
        assertThat(steadyServiceList).hasSize(databaseSizeBeforeCreate + 1);
        SteadyService testSteadyService = steadyServiceList.get(steadyServiceList.size() - 1);
        assertThat(testSteadyService.getTimestampCreated()).isEqualTo(DEFAULT_TIMESTAMP_CREATED);
        assertThat(testSteadyService.getTimestampProcessed()).isEqualTo(DEFAULT_TIMESTAMP_PROCESSED);
        assertThat(testSteadyService.getActionTaken()).isEqualTo(DEFAULT_ACTION_TAKEN);
        assertThat(testSteadyService.getScore()).isEqualTo(DEFAULT_SCORE);
        assertThat(testSteadyService.getDetails()).isEqualTo(DEFAULT_DETAILS);
        assertThat(testSteadyService.getMonitoringPeriodSec()).isEqualTo(DEFAULT_MONITORING_PERIOD_SEC);
    }

    @Test
    @Transactional
    public void createSteadyServiceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = steadyServiceRepository.findAll().size();

        // Create the SteadyService with an existing ID
        steadyService.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSteadyServiceMockMvc.perform(post("/api/steady-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(steadyService)))
            .andExpect(status().isBadRequest());

        // Validate the SteadyService in the database
        List<SteadyService> steadyServiceList = steadyServiceRepository.findAll();
        assertThat(steadyServiceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllSteadyServices() throws Exception {
        // Initialize the database
        steadyServiceRepository.saveAndFlush(steadyService);

        // Get all the steadyServiceList
        restSteadyServiceMockMvc.perform(get("/api/steady-services?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(steadyService.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestampCreated").value(hasItem(DEFAULT_TIMESTAMP_CREATED.toString())))
            .andExpect(jsonPath("$.[*].timestampProcessed").value(hasItem(DEFAULT_TIMESTAMP_PROCESSED.toString())))
            .andExpect(jsonPath("$.[*].actionTaken").value(hasItem(DEFAULT_ACTION_TAKEN)))
            .andExpect(jsonPath("$.[*].score").value(hasItem(DEFAULT_SCORE.intValue())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS)))
            .andExpect(jsonPath("$.[*].monitoringPeriodSec").value(hasItem(DEFAULT_MONITORING_PERIOD_SEC)));
    }
    
    @Test
    @Transactional
    public void getSteadyService() throws Exception {
        // Initialize the database
        steadyServiceRepository.saveAndFlush(steadyService);

        // Get the steadyService
        restSteadyServiceMockMvc.perform(get("/api/steady-services/{id}", steadyService.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(steadyService.getId().intValue()))
            .andExpect(jsonPath("$.timestampCreated").value(DEFAULT_TIMESTAMP_CREATED.toString()))
            .andExpect(jsonPath("$.timestampProcessed").value(DEFAULT_TIMESTAMP_PROCESSED.toString()))
            .andExpect(jsonPath("$.actionTaken").value(DEFAULT_ACTION_TAKEN))
            .andExpect(jsonPath("$.score").value(DEFAULT_SCORE.intValue()))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS))
            .andExpect(jsonPath("$.monitoringPeriodSec").value(DEFAULT_MONITORING_PERIOD_SEC));
    }
    @Test
    @Transactional
    public void getNonExistingSteadyService() throws Exception {
        // Get the steadyService
        restSteadyServiceMockMvc.perform(get("/api/steady-services/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSteadyService() throws Exception {
        // Initialize the database
        steadyServiceService.save(steadyService);

        int databaseSizeBeforeUpdate = steadyServiceRepository.findAll().size();

        // Update the steadyService
        SteadyService updatedSteadyService = steadyServiceRepository.findById(steadyService.getId()).get();
        // Disconnect from session so that the updates on updatedSteadyService are not directly saved in db
        em.detach(updatedSteadyService);
        updatedSteadyService
            .timestampCreated(UPDATED_TIMESTAMP_CREATED)
            .timestampProcessed(UPDATED_TIMESTAMP_PROCESSED)
            .actionTaken(UPDATED_ACTION_TAKEN)
            .score(UPDATED_SCORE)
            .details(UPDATED_DETAILS)
            .monitoringPeriodSec(UPDATED_MONITORING_PERIOD_SEC);

        restSteadyServiceMockMvc.perform(put("/api/steady-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedSteadyService)))
            .andExpect(status().isOk());

        // Validate the SteadyService in the database
        List<SteadyService> steadyServiceList = steadyServiceRepository.findAll();
        assertThat(steadyServiceList).hasSize(databaseSizeBeforeUpdate);
        SteadyService testSteadyService = steadyServiceList.get(steadyServiceList.size() - 1);
        assertThat(testSteadyService.getTimestampCreated()).isEqualTo(UPDATED_TIMESTAMP_CREATED);
        assertThat(testSteadyService.getTimestampProcessed()).isEqualTo(UPDATED_TIMESTAMP_PROCESSED);
        assertThat(testSteadyService.getActionTaken()).isEqualTo(UPDATED_ACTION_TAKEN);
        assertThat(testSteadyService.getScore()).isEqualTo(UPDATED_SCORE);
        assertThat(testSteadyService.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testSteadyService.getMonitoringPeriodSec()).isEqualTo(UPDATED_MONITORING_PERIOD_SEC);
    }

    @Test
    @Transactional
    public void updateNonExistingSteadyService() throws Exception {
        int databaseSizeBeforeUpdate = steadyServiceRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSteadyServiceMockMvc.perform(put("/api/steady-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(steadyService)))
            .andExpect(status().isBadRequest());

        // Validate the SteadyService in the database
        List<SteadyService> steadyServiceList = steadyServiceRepository.findAll();
        assertThat(steadyServiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSteadyService() throws Exception {
        // Initialize the database
        steadyServiceService.save(steadyService);

        int databaseSizeBeforeDelete = steadyServiceRepository.findAll().size();

        // Delete the steadyService
        restSteadyServiceMockMvc.perform(delete("/api/steady-services/{id}", steadyService.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SteadyService> steadyServiceList = steadyServiceRepository.findAll();
        assertThat(steadyServiceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
