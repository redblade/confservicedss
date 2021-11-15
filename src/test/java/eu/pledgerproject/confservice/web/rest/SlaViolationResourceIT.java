package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.SlaViolation;
import eu.pledgerproject.confservice.repository.SlaViolationRepository;
import eu.pledgerproject.confservice.service.SlaViolationService;

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

import eu.pledgerproject.confservice.domain.enumeration.SlaViolationType;
/**
 * Integration tests for the {@link SlaViolationResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class SlaViolationResourceIT {

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_VIOLATION_NAME = "AAAAAAAAAA";
    private static final String UPDATED_VIOLATION_NAME = "BBBBBBBBBB";

    private static final SlaViolationType DEFAULT_SEVERITY_TYPE = SlaViolationType.Warning;
    private static final SlaViolationType UPDATED_SEVERITY_TYPE = SlaViolationType.Mild;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    @Autowired
    private SlaViolationRepository slaViolationRepository;

    @Autowired
    private SlaViolationService slaViolationService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSlaViolationMockMvc;

    private SlaViolation slaViolation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SlaViolation createEntity(EntityManager em) {
        SlaViolation slaViolation = new SlaViolation()
            .timestamp(DEFAULT_TIMESTAMP)
            .violationName(DEFAULT_VIOLATION_NAME)
            .severityType(DEFAULT_SEVERITY_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .status(DEFAULT_STATUS);
        return slaViolation;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SlaViolation createUpdatedEntity(EntityManager em) {
        SlaViolation slaViolation = new SlaViolation()
            .timestamp(UPDATED_TIMESTAMP)
            .violationName(UPDATED_VIOLATION_NAME)
            .severityType(UPDATED_SEVERITY_TYPE)
            .description(UPDATED_DESCRIPTION)
            .status(UPDATED_STATUS);
        return slaViolation;
    }

    @BeforeEach
    public void initTest() {
        slaViolation = createEntity(em);
    }

    @Test
    @Transactional
    public void createSlaViolation() throws Exception {
        int databaseSizeBeforeCreate = slaViolationRepository.findAll().size();
        // Create the SlaViolation
        restSlaViolationMockMvc.perform(post("/api/sla-violations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(slaViolation)))
            .andExpect(status().isCreated());

        // Validate the SlaViolation in the database
        List<SlaViolation> slaViolationList = slaViolationRepository.findAll();
        assertThat(slaViolationList).hasSize(databaseSizeBeforeCreate + 1);
        SlaViolation testSlaViolation = slaViolationList.get(slaViolationList.size() - 1);
        assertThat(testSlaViolation.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testSlaViolation.getViolationName()).isEqualTo(DEFAULT_VIOLATION_NAME);
        assertThat(testSlaViolation.getSeverityType()).isEqualTo(DEFAULT_SEVERITY_TYPE);
        assertThat(testSlaViolation.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testSlaViolation.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createSlaViolationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = slaViolationRepository.findAll().size();

        // Create the SlaViolation with an existing ID
        slaViolation.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSlaViolationMockMvc.perform(post("/api/sla-violations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(slaViolation)))
            .andExpect(status().isBadRequest());

        // Validate the SlaViolation in the database
        List<SlaViolation> slaViolationList = slaViolationRepository.findAll();
        assertThat(slaViolationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllSlaViolations() throws Exception {
        // Initialize the database
        slaViolationRepository.saveAndFlush(slaViolation);

        // Get all the slaViolationList
        restSlaViolationMockMvc.perform(get("/api/sla-violations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(slaViolation.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].violationName").value(hasItem(DEFAULT_VIOLATION_NAME)))
            .andExpect(jsonPath("$.[*].severityType").value(hasItem(DEFAULT_SEVERITY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }
    
    @Test
    @Transactional
    public void getSlaViolation() throws Exception {
        // Initialize the database
        slaViolationRepository.saveAndFlush(slaViolation);

        // Get the slaViolation
        restSlaViolationMockMvc.perform(get("/api/sla-violations/{id}", slaViolation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(slaViolation.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.violationName").value(DEFAULT_VIOLATION_NAME))
            .andExpect(jsonPath("$.severityType").value(DEFAULT_SEVERITY_TYPE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }
    @Test
    @Transactional
    public void getNonExistingSlaViolation() throws Exception {
        // Get the slaViolation
        restSlaViolationMockMvc.perform(get("/api/sla-violations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSlaViolation() throws Exception {
        // Initialize the database
        slaViolationService.save(slaViolation);

        int databaseSizeBeforeUpdate = slaViolationRepository.findAll().size();

        // Update the slaViolation
        SlaViolation updatedSlaViolation = slaViolationRepository.findById(slaViolation.getId()).get();
        // Disconnect from session so that the updates on updatedSlaViolation are not directly saved in db
        em.detach(updatedSlaViolation);
        updatedSlaViolation
            .timestamp(UPDATED_TIMESTAMP)
            .violationName(UPDATED_VIOLATION_NAME)
            .severityType(UPDATED_SEVERITY_TYPE)
            .description(UPDATED_DESCRIPTION)
            .status(UPDATED_STATUS);

        restSlaViolationMockMvc.perform(put("/api/sla-violations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedSlaViolation)))
            .andExpect(status().isOk());

        // Validate the SlaViolation in the database
        List<SlaViolation> slaViolationList = slaViolationRepository.findAll();
        assertThat(slaViolationList).hasSize(databaseSizeBeforeUpdate);
        SlaViolation testSlaViolation = slaViolationList.get(slaViolationList.size() - 1);
        assertThat(testSlaViolation.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testSlaViolation.getViolationName()).isEqualTo(UPDATED_VIOLATION_NAME);
        assertThat(testSlaViolation.getSeverityType()).isEqualTo(UPDATED_SEVERITY_TYPE);
        assertThat(testSlaViolation.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testSlaViolation.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void updateNonExistingSlaViolation() throws Exception {
        int databaseSizeBeforeUpdate = slaViolationRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSlaViolationMockMvc.perform(put("/api/sla-violations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(slaViolation)))
            .andExpect(status().isBadRequest());

        // Validate the SlaViolation in the database
        List<SlaViolation> slaViolationList = slaViolationRepository.findAll();
        assertThat(slaViolationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSlaViolation() throws Exception {
        // Initialize the database
        slaViolationService.save(slaViolation);

        int databaseSizeBeforeDelete = slaViolationRepository.findAll().size();

        // Delete the slaViolation
        restSlaViolationMockMvc.perform(delete("/api/sla-violations/{id}", slaViolation.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SlaViolation> slaViolationList = slaViolationRepository.findAll();
        assertThat(slaViolationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
