package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.CriticalService;
import eu.pledgerproject.confservice.repository.CriticalServiceRepository;
import eu.pledgerproject.confservice.service.CriticalServiceService;

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
 * Integration tests for the {@link CriticalServiceResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CriticalServiceResourceIT {

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
    private CriticalServiceRepository criticalServiceRepository;

    @Autowired
    private CriticalServiceService criticalServiceService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCriticalServiceMockMvc;

    private CriticalService criticalService;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CriticalService createEntity(EntityManager em) {
        CriticalService criticalService = new CriticalService()
            .timestampCreated(DEFAULT_TIMESTAMP_CREATED)
            .timestampProcessed(DEFAULT_TIMESTAMP_PROCESSED)
            .actionTaken(DEFAULT_ACTION_TAKEN)
            .score(DEFAULT_SCORE)
            .details(DEFAULT_DETAILS)
            .monitoringPeriodSec(DEFAULT_MONITORING_PERIOD_SEC);
        return criticalService;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CriticalService createUpdatedEntity(EntityManager em) {
        CriticalService criticalService = new CriticalService()
            .timestampCreated(UPDATED_TIMESTAMP_CREATED)
            .timestampProcessed(UPDATED_TIMESTAMP_PROCESSED)
            .actionTaken(UPDATED_ACTION_TAKEN)
            .score(UPDATED_SCORE)
            .details(UPDATED_DETAILS)
            .monitoringPeriodSec(UPDATED_MONITORING_PERIOD_SEC);
        return criticalService;
    }

    @BeforeEach
    public void initTest() {
        criticalService = createEntity(em);
    }

    @Test
    @Transactional
    public void createCriticalService() throws Exception {
        int databaseSizeBeforeCreate = criticalServiceRepository.findAll().size();
        // Create the CriticalService
        restCriticalServiceMockMvc.perform(post("/api/critical-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(criticalService)))
            .andExpect(status().isCreated());

        // Validate the CriticalService in the database
        List<CriticalService> criticalServiceList = criticalServiceRepository.findAll();
        assertThat(criticalServiceList).hasSize(databaseSizeBeforeCreate + 1);
        CriticalService testCriticalService = criticalServiceList.get(criticalServiceList.size() - 1);
        assertThat(testCriticalService.getTimestampCreated()).isEqualTo(DEFAULT_TIMESTAMP_CREATED);
        assertThat(testCriticalService.getTimestampProcessed()).isEqualTo(DEFAULT_TIMESTAMP_PROCESSED);
        assertThat(testCriticalService.getActionTaken()).isEqualTo(DEFAULT_ACTION_TAKEN);
        assertThat(testCriticalService.getScore()).isEqualTo(DEFAULT_SCORE);
        assertThat(testCriticalService.getDetails()).isEqualTo(DEFAULT_DETAILS);
        assertThat(testCriticalService.getMonitoringPeriodSec()).isEqualTo(DEFAULT_MONITORING_PERIOD_SEC);
    }

    @Test
    @Transactional
    public void createCriticalServiceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = criticalServiceRepository.findAll().size();

        // Create the CriticalService with an existing ID
        criticalService.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCriticalServiceMockMvc.perform(post("/api/critical-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(criticalService)))
            .andExpect(status().isBadRequest());

        // Validate the CriticalService in the database
        List<CriticalService> criticalServiceList = criticalServiceRepository.findAll();
        assertThat(criticalServiceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCriticalServices() throws Exception {
        // Initialize the database
        criticalServiceRepository.saveAndFlush(criticalService);

        // Get all the criticalServiceList
        restCriticalServiceMockMvc.perform(get("/api/critical-services?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(criticalService.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestampCreated").value(hasItem(DEFAULT_TIMESTAMP_CREATED.toString())))
            .andExpect(jsonPath("$.[*].timestampProcessed").value(hasItem(DEFAULT_TIMESTAMP_PROCESSED.toString())))
            .andExpect(jsonPath("$.[*].actionTaken").value(hasItem(DEFAULT_ACTION_TAKEN)))
            .andExpect(jsonPath("$.[*].score").value(hasItem(DEFAULT_SCORE.intValue())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS)))
            .andExpect(jsonPath("$.[*].monitoringPeriodSec").value(hasItem(DEFAULT_MONITORING_PERIOD_SEC)));
    }
    
    @Test
    @Transactional
    public void getCriticalService() throws Exception {
        // Initialize the database
        criticalServiceRepository.saveAndFlush(criticalService);

        // Get the criticalService
        restCriticalServiceMockMvc.perform(get("/api/critical-services/{id}", criticalService.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(criticalService.getId().intValue()))
            .andExpect(jsonPath("$.timestampCreated").value(DEFAULT_TIMESTAMP_CREATED.toString()))
            .andExpect(jsonPath("$.timestampProcessed").value(DEFAULT_TIMESTAMP_PROCESSED.toString()))
            .andExpect(jsonPath("$.actionTaken").value(DEFAULT_ACTION_TAKEN))
            .andExpect(jsonPath("$.score").value(DEFAULT_SCORE.intValue()))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS))
            .andExpect(jsonPath("$.monitoringPeriodSec").value(DEFAULT_MONITORING_PERIOD_SEC));
    }
    @Test
    @Transactional
    public void getNonExistingCriticalService() throws Exception {
        // Get the criticalService
        restCriticalServiceMockMvc.perform(get("/api/critical-services/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCriticalService() throws Exception {
        // Initialize the database
        criticalServiceService.save(criticalService);

        int databaseSizeBeforeUpdate = criticalServiceRepository.findAll().size();

        // Update the criticalService
        CriticalService updatedCriticalService = criticalServiceRepository.findById(criticalService.getId()).get();
        // Disconnect from session so that the updates on updatedCriticalService are not directly saved in db
        em.detach(updatedCriticalService);
        updatedCriticalService
            .timestampCreated(UPDATED_TIMESTAMP_CREATED)
            .timestampProcessed(UPDATED_TIMESTAMP_PROCESSED)
            .actionTaken(UPDATED_ACTION_TAKEN)
            .score(UPDATED_SCORE)
            .details(UPDATED_DETAILS)
            .monitoringPeriodSec(UPDATED_MONITORING_PERIOD_SEC);

        restCriticalServiceMockMvc.perform(put("/api/critical-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCriticalService)))
            .andExpect(status().isOk());

        // Validate the CriticalService in the database
        List<CriticalService> criticalServiceList = criticalServiceRepository.findAll();
        assertThat(criticalServiceList).hasSize(databaseSizeBeforeUpdate);
        CriticalService testCriticalService = criticalServiceList.get(criticalServiceList.size() - 1);
        assertThat(testCriticalService.getTimestampCreated()).isEqualTo(UPDATED_TIMESTAMP_CREATED);
        assertThat(testCriticalService.getTimestampProcessed()).isEqualTo(UPDATED_TIMESTAMP_PROCESSED);
        assertThat(testCriticalService.getActionTaken()).isEqualTo(UPDATED_ACTION_TAKEN);
        assertThat(testCriticalService.getScore()).isEqualTo(UPDATED_SCORE);
        assertThat(testCriticalService.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testCriticalService.getMonitoringPeriodSec()).isEqualTo(UPDATED_MONITORING_PERIOD_SEC);
    }

    @Test
    @Transactional
    public void updateNonExistingCriticalService() throws Exception {
        int databaseSizeBeforeUpdate = criticalServiceRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCriticalServiceMockMvc.perform(put("/api/critical-services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(criticalService)))
            .andExpect(status().isBadRequest());

        // Validate the CriticalService in the database
        List<CriticalService> criticalServiceList = criticalServiceRepository.findAll();
        assertThat(criticalServiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCriticalService() throws Exception {
        // Initialize the database
        criticalServiceService.save(criticalService);

        int databaseSizeBeforeDelete = criticalServiceRepository.findAll().size();

        // Delete the criticalService
        restCriticalServiceMockMvc.perform(delete("/api/critical-services/{id}", criticalService.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CriticalService> criticalServiceList = criticalServiceRepository.findAll();
        assertThat(criticalServiceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
