package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.ServiceReport;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;
import eu.pledgerproject.confservice.service.ServiceReportService;

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
 * Integration tests for the {@link ServiceReportResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ServiceReportResourceIT {

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
    private ServiceReportRepository serviceReportRepository;

    @Autowired
    private ServiceReportService serviceReportService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restServiceReportMockMvc;

    private ServiceReport serviceReport;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceReport createEntity(EntityManager em) {
        ServiceReport serviceReport = new ServiceReport()
            .timestamp(DEFAULT_TIMESTAMP)
            .group(DEFAULT_GROUP)
            .category(DEFAULT_CATEGORY)
            .key(DEFAULT_KEY)
            .value(DEFAULT_VALUE);
        return serviceReport;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceReport createUpdatedEntity(EntityManager em) {
        ServiceReport serviceReport = new ServiceReport()
            .timestamp(UPDATED_TIMESTAMP)
            .group(UPDATED_GROUP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);
        return serviceReport;
    }

    @BeforeEach
    public void initTest() {
        serviceReport = createEntity(em);
    }

    @Test
    @Transactional
    public void createServiceReport() throws Exception {
        int databaseSizeBeforeCreate = serviceReportRepository.findAll().size();
        // Create the ServiceReport
        restServiceReportMockMvc.perform(post("/api/service-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceReport)))
            .andExpect(status().isCreated());

        // Validate the ServiceReport in the database
        List<ServiceReport> serviceReportList = serviceReportRepository.findAll();
        assertThat(serviceReportList).hasSize(databaseSizeBeforeCreate + 1);
        ServiceReport testServiceReport = serviceReportList.get(serviceReportList.size() - 1);
        assertThat(testServiceReport.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testServiceReport.getGroup()).isEqualTo(DEFAULT_GROUP);
        assertThat(testServiceReport.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testServiceReport.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testServiceReport.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createServiceReportWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = serviceReportRepository.findAll().size();

        // Create the ServiceReport with an existing ID
        serviceReport.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restServiceReportMockMvc.perform(post("/api/service-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceReport)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceReport in the database
        List<ServiceReport> serviceReportList = serviceReportRepository.findAll();
        assertThat(serviceReportList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllServiceReports() throws Exception {
        // Initialize the database
        serviceReportRepository.saveAndFlush(serviceReport);

        // Get all the serviceReportList
        restServiceReportMockMvc.perform(get("/api/service-reports?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(serviceReport.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getServiceReport() throws Exception {
        // Initialize the database
        serviceReportRepository.saveAndFlush(serviceReport);

        // Get the serviceReport
        restServiceReportMockMvc.perform(get("/api/service-reports/{id}", serviceReport.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(serviceReport.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.group").value(DEFAULT_GROUP))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.doubleValue()));
    }
    @Test
    @Transactional
    public void getNonExistingServiceReport() throws Exception {
        // Get the serviceReport
        restServiceReportMockMvc.perform(get("/api/service-reports/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateServiceReport() throws Exception {
        // Initialize the database
        serviceReportService.save(serviceReport);

        int databaseSizeBeforeUpdate = serviceReportRepository.findAll().size();

        // Update the serviceReport
        ServiceReport updatedServiceReport = serviceReportRepository.findById(serviceReport.getId()).get();
        // Disconnect from session so that the updates on updatedServiceReport are not directly saved in db
        em.detach(updatedServiceReport);
        updatedServiceReport
            .timestamp(UPDATED_TIMESTAMP)
            .group(UPDATED_GROUP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);

        restServiceReportMockMvc.perform(put("/api/service-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedServiceReport)))
            .andExpect(status().isOk());

        // Validate the ServiceReport in the database
        List<ServiceReport> serviceReportList = serviceReportRepository.findAll();
        assertThat(serviceReportList).hasSize(databaseSizeBeforeUpdate);
        ServiceReport testServiceReport = serviceReportList.get(serviceReportList.size() - 1);
        assertThat(testServiceReport.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testServiceReport.getGroup()).isEqualTo(UPDATED_GROUP);
        assertThat(testServiceReport.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testServiceReport.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testServiceReport.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingServiceReport() throws Exception {
        int databaseSizeBeforeUpdate = serviceReportRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceReportMockMvc.perform(put("/api/service-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceReport)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceReport in the database
        List<ServiceReport> serviceReportList = serviceReportRepository.findAll();
        assertThat(serviceReportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteServiceReport() throws Exception {
        // Initialize the database
        serviceReportService.save(serviceReport);

        int databaseSizeBeforeDelete = serviceReportRepository.findAll().size();

        // Delete the serviceReport
        restServiceReportMockMvc.perform(delete("/api/service-reports/{id}", serviceReport.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ServiceReport> serviceReportList = serviceReportRepository.findAll();
        assertThat(serviceReportList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
