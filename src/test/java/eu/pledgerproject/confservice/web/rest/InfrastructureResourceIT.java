package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;
import eu.pledgerproject.confservice.service.InfrastructureService;

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
 * Integration tests for the {@link InfrastructureResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class InfrastructureResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_ENDPOINT = "AAAAAAAAAA";
    private static final String UPDATED_ENDPOINT = "BBBBBBBBBB";

    private static final String DEFAULT_CREDENTIALS = "AAAAAAAAAA";
    private static final String UPDATED_CREDENTIALS = "BBBBBBBBBB";

    private static final String DEFAULT_MONITORING_PLUGIN = "AAAAAAAAAA";
    private static final String UPDATED_MONITORING_PLUGIN = "BBBBBBBBBB";

    private static final String DEFAULT_PROPERTIES = "AAAAAAAAAA";
    private static final String UPDATED_PROPERTIES = "BBBBBBBBBB";

    private static final String DEFAULT_TOTAL_RESOURCES = "AAAAAAAAAA";
    private static final String UPDATED_TOTAL_RESOURCES = "BBBBBBBBBB";

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInfrastructureMockMvc;

    private Infrastructure infrastructure;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Infrastructure createEntity(EntityManager em) {
        Infrastructure infrastructure = new Infrastructure()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .endpoint(DEFAULT_ENDPOINT)
            .credentials(DEFAULT_CREDENTIALS)
            .monitoringPlugin(DEFAULT_MONITORING_PLUGIN)
            .properties(DEFAULT_PROPERTIES)
            .totalResources(DEFAULT_TOTAL_RESOURCES);
        return infrastructure;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Infrastructure createUpdatedEntity(EntityManager em) {
        Infrastructure infrastructure = new Infrastructure()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .endpoint(UPDATED_ENDPOINT)
            .credentials(UPDATED_CREDENTIALS)
            .monitoringPlugin(UPDATED_MONITORING_PLUGIN)
            .properties(UPDATED_PROPERTIES)
            .totalResources(UPDATED_TOTAL_RESOURCES);
        return infrastructure;
    }

    @BeforeEach
    public void initTest() {
        infrastructure = createEntity(em);
    }

    @Test
    @Transactional
    public void createInfrastructure() throws Exception {
        int databaseSizeBeforeCreate = infrastructureRepository.findAll().size();
        // Create the Infrastructure
        restInfrastructureMockMvc.perform(post("/api/infrastructures")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructure)))
            .andExpect(status().isCreated());

        // Validate the Infrastructure in the database
        List<Infrastructure> infrastructureList = infrastructureRepository.findAll();
        assertThat(infrastructureList).hasSize(databaseSizeBeforeCreate + 1);
        Infrastructure testInfrastructure = infrastructureList.get(infrastructureList.size() - 1);
        assertThat(testInfrastructure.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testInfrastructure.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testInfrastructure.getEndpoint()).isEqualTo(DEFAULT_ENDPOINT);
        assertThat(testInfrastructure.getCredentials()).isEqualTo(DEFAULT_CREDENTIALS);
        assertThat(testInfrastructure.getMonitoringPlugin()).isEqualTo(DEFAULT_MONITORING_PLUGIN);
        assertThat(testInfrastructure.getProperties()).isEqualTo(DEFAULT_PROPERTIES);
        assertThat(testInfrastructure.getTotalResources()).isEqualTo(DEFAULT_TOTAL_RESOURCES);
    }

    @Test
    @Transactional
    public void createInfrastructureWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = infrastructureRepository.findAll().size();

        // Create the Infrastructure with an existing ID
        infrastructure.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restInfrastructureMockMvc.perform(post("/api/infrastructures")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructure)))
            .andExpect(status().isBadRequest());

        // Validate the Infrastructure in the database
        List<Infrastructure> infrastructureList = infrastructureRepository.findAll();
        assertThat(infrastructureList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllInfrastructures() throws Exception {
        // Initialize the database
        infrastructureRepository.saveAndFlush(infrastructure);

        // Get all the infrastructureList
        restInfrastructureMockMvc.perform(get("/api/infrastructures?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(infrastructure.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].endpoint").value(hasItem(DEFAULT_ENDPOINT)))
            .andExpect(jsonPath("$.[*].credentials").value(hasItem(DEFAULT_CREDENTIALS)))
            .andExpect(jsonPath("$.[*].monitoringPlugin").value(hasItem(DEFAULT_MONITORING_PLUGIN)))
            .andExpect(jsonPath("$.[*].properties").value(hasItem(DEFAULT_PROPERTIES)))
            .andExpect(jsonPath("$.[*].totalResources").value(hasItem(DEFAULT_TOTAL_RESOURCES)));
    }
    
    @Test
    @Transactional
    public void getInfrastructure() throws Exception {
        // Initialize the database
        infrastructureRepository.saveAndFlush(infrastructure);

        // Get the infrastructure
        restInfrastructureMockMvc.perform(get("/api/infrastructures/{id}", infrastructure.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(infrastructure.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.endpoint").value(DEFAULT_ENDPOINT))
            .andExpect(jsonPath("$.credentials").value(DEFAULT_CREDENTIALS))
            .andExpect(jsonPath("$.monitoringPlugin").value(DEFAULT_MONITORING_PLUGIN))
            .andExpect(jsonPath("$.properties").value(DEFAULT_PROPERTIES))
            .andExpect(jsonPath("$.totalResources").value(DEFAULT_TOTAL_RESOURCES));
    }
    @Test
    @Transactional
    public void getNonExistingInfrastructure() throws Exception {
        // Get the infrastructure
        restInfrastructureMockMvc.perform(get("/api/infrastructures/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInfrastructure() throws Exception {
        // Initialize the database
        infrastructureService.save(infrastructure);

        int databaseSizeBeforeUpdate = infrastructureRepository.findAll().size();

        // Update the infrastructure
        Infrastructure updatedInfrastructure = infrastructureRepository.findById(infrastructure.getId()).get();
        // Disconnect from session so that the updates on updatedInfrastructure are not directly saved in db
        em.detach(updatedInfrastructure);
        updatedInfrastructure
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .endpoint(UPDATED_ENDPOINT)
            .credentials(UPDATED_CREDENTIALS)
            .monitoringPlugin(UPDATED_MONITORING_PLUGIN)
            .properties(UPDATED_PROPERTIES)
            .totalResources(UPDATED_TOTAL_RESOURCES);

        restInfrastructureMockMvc.perform(put("/api/infrastructures")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedInfrastructure)))
            .andExpect(status().isOk());

        // Validate the Infrastructure in the database
        List<Infrastructure> infrastructureList = infrastructureRepository.findAll();
        assertThat(infrastructureList).hasSize(databaseSizeBeforeUpdate);
        Infrastructure testInfrastructure = infrastructureList.get(infrastructureList.size() - 1);
        assertThat(testInfrastructure.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testInfrastructure.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testInfrastructure.getEndpoint()).isEqualTo(UPDATED_ENDPOINT);
        assertThat(testInfrastructure.getCredentials()).isEqualTo(UPDATED_CREDENTIALS);
        assertThat(testInfrastructure.getMonitoringPlugin()).isEqualTo(UPDATED_MONITORING_PLUGIN);
        assertThat(testInfrastructure.getProperties()).isEqualTo(UPDATED_PROPERTIES);
        assertThat(testInfrastructure.getTotalResources()).isEqualTo(UPDATED_TOTAL_RESOURCES);
    }

    @Test
    @Transactional
    public void updateNonExistingInfrastructure() throws Exception {
        int databaseSizeBeforeUpdate = infrastructureRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInfrastructureMockMvc.perform(put("/api/infrastructures")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructure)))
            .andExpect(status().isBadRequest());

        // Validate the Infrastructure in the database
        List<Infrastructure> infrastructureList = infrastructureRepository.findAll();
        assertThat(infrastructureList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteInfrastructure() throws Exception {
        // Initialize the database
        infrastructureService.save(infrastructure);

        int databaseSizeBeforeDelete = infrastructureRepository.findAll().size();

        // Delete the infrastructure
        restInfrastructureMockMvc.perform(delete("/api/infrastructures/{id}", infrastructure.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Infrastructure> infrastructureList = infrastructureRepository.findAll();
        assertThat(infrastructureList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
