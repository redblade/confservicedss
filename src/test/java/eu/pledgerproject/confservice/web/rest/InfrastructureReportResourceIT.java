package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.InfrastructureReport;
import eu.pledgerproject.confservice.repository.InfrastructureReportRepository;
import eu.pledgerproject.confservice.service.InfrastructureReportService;

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
 * Integration tests for the {@link InfrastructureReportResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class InfrastructureReportResourceIT {

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_GROUP = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final Double DEFAULT_VALUE = 1D;
    private static final Double UPDATED_VALUE = 2D;

    @Autowired
    private InfrastructureReportRepository infrastructureReportRepository;

    @Autowired
    private InfrastructureReportService infrastructureReportService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInfrastructureReportMockMvc;

    private InfrastructureReport infrastructureReport;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InfrastructureReport createEntity(EntityManager em) {
        InfrastructureReport infrastructureReport = new InfrastructureReport()
            .timestamp(DEFAULT_TIMESTAMP)
            .group(DEFAULT_GROUP)
            .category(DEFAULT_CATEGORY)
            .key(DEFAULT_KEY)
            .value(DEFAULT_VALUE);
        return infrastructureReport;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InfrastructureReport createUpdatedEntity(EntityManager em) {
        InfrastructureReport infrastructureReport = new InfrastructureReport()
            .timestamp(UPDATED_TIMESTAMP)
            .group(UPDATED_GROUP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);
        return infrastructureReport;
    }

    @BeforeEach
    public void initTest() {
        infrastructureReport = createEntity(em);
    }

    @Test
    @Transactional
    public void createInfrastructureReport() throws Exception {
        int databaseSizeBeforeCreate = infrastructureReportRepository.findAll().size();
        // Create the InfrastructureReport
        restInfrastructureReportMockMvc.perform(post("/api/infrastructure-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureReport)))
            .andExpect(status().isCreated());

        // Validate the InfrastructureReport in the database
        List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findAll();
        assertThat(infrastructureReportList).hasSize(databaseSizeBeforeCreate + 1);
        InfrastructureReport testInfrastructureReport = infrastructureReportList.get(infrastructureReportList.size() - 1);
        assertThat(testInfrastructureReport.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testInfrastructureReport.getGroup()).isEqualTo(DEFAULT_GROUP);
        assertThat(testInfrastructureReport.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testInfrastructureReport.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testInfrastructureReport.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createInfrastructureReportWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = infrastructureReportRepository.findAll().size();

        // Create the InfrastructureReport with an existing ID
        infrastructureReport.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restInfrastructureReportMockMvc.perform(post("/api/infrastructure-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureReport)))
            .andExpect(status().isBadRequest());

        // Validate the InfrastructureReport in the database
        List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findAll();
        assertThat(infrastructureReportList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllInfrastructureReports() throws Exception {
        // Initialize the database
        infrastructureReportRepository.saveAndFlush(infrastructureReport);

        // Get all the infrastructureReportList
        restInfrastructureReportMockMvc.perform(get("/api/infrastructure-reports?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(infrastructureReport.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getInfrastructureReport() throws Exception {
        // Initialize the database
        infrastructureReportRepository.saveAndFlush(infrastructureReport);

        // Get the infrastructureReport
        restInfrastructureReportMockMvc.perform(get("/api/infrastructure-reports/{id}", infrastructureReport.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(infrastructureReport.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.group").value(DEFAULT_GROUP))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.doubleValue()));
    }
    @Test
    @Transactional
    public void getNonExistingInfrastructureReport() throws Exception {
        // Get the infrastructureReport
        restInfrastructureReportMockMvc.perform(get("/api/infrastructure-reports/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInfrastructureReport() throws Exception {
        // Initialize the database
        infrastructureReportService.save(infrastructureReport);

        int databaseSizeBeforeUpdate = infrastructureReportRepository.findAll().size();

        // Update the infrastructureReport
        InfrastructureReport updatedInfrastructureReport = infrastructureReportRepository.findById(infrastructureReport.getId()).get();
        // Disconnect from session so that the updates on updatedInfrastructureReport are not directly saved in db
        em.detach(updatedInfrastructureReport);
        updatedInfrastructureReport
            .timestamp(UPDATED_TIMESTAMP)
            .group(UPDATED_GROUP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);

        restInfrastructureReportMockMvc.perform(put("/api/infrastructure-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedInfrastructureReport)))
            .andExpect(status().isOk());

        // Validate the InfrastructureReport in the database
        List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findAll();
        assertThat(infrastructureReportList).hasSize(databaseSizeBeforeUpdate);
        InfrastructureReport testInfrastructureReport = infrastructureReportList.get(infrastructureReportList.size() - 1);
        assertThat(testInfrastructureReport.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testInfrastructureReport.getGroup()).isEqualTo(UPDATED_GROUP);
        assertThat(testInfrastructureReport.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testInfrastructureReport.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testInfrastructureReport.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingInfrastructureReport() throws Exception {
        int databaseSizeBeforeUpdate = infrastructureReportRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInfrastructureReportMockMvc.perform(put("/api/infrastructure-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureReport)))
            .andExpect(status().isBadRequest());

        // Validate the InfrastructureReport in the database
        List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findAll();
        assertThat(infrastructureReportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteInfrastructureReport() throws Exception {
        // Initialize the database
        infrastructureReportService.save(infrastructureReport);

        int databaseSizeBeforeDelete = infrastructureReportRepository.findAll().size();

        // Delete the infrastructureReport
        restInfrastructureReportMockMvc.perform(delete("/api/infrastructure-reports/{id}", infrastructureReport.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<InfrastructureReport> infrastructureReportList = infrastructureReportRepository.findAll();
        assertThat(infrastructureReportList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
