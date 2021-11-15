package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Service;
import eu.pledgerproject.confservice.repository.ServiceRepository;
import eu.pledgerproject.confservice.service.ServiceService;

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

import eu.pledgerproject.confservice.domain.enumeration.DeployType;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
/**
 * Integration tests for the {@link ServiceResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ServiceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PROFILE = "AAAAAAAAAA";
    private static final String UPDATED_PROFILE = "BBBBBBBBBB";

    private static final Integer DEFAULT_PRIORITY = 1;
    private static final Integer UPDATED_PRIORITY = 2;

    private static final String DEFAULT_INITIAL_CONFIGURATION = "AAAAAAAAAA";
    private static final String UPDATED_INITIAL_CONFIGURATION = "BBBBBBBBBB";

    private static final String DEFAULT_RUNTIME_CONFIGURATION = "AAAAAAAAAA";
    private static final String UPDATED_RUNTIME_CONFIGURATION = "BBBBBBBBBB";

    private static final DeployType DEFAULT_DEPLOY_TYPE = DeployType.KUBERNETES;
    private static final DeployType UPDATED_DEPLOY_TYPE = DeployType.DOCKER;

    private static final String DEFAULT_DEPLOY_DESCRIPTOR = "AAAAAAAAAA";
    private static final String UPDATED_DEPLOY_DESCRIPTOR = "BBBBBBBBBB";

    private static final ExecStatus DEFAULT_STATUS = ExecStatus.RUNNING;
    private static final ExecStatus UPDATED_STATUS = ExecStatus.STOPPED;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restServiceMockMvc;

    private Service service;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Service createEntity(EntityManager em) {
        Service service = new Service()
            .name(DEFAULT_NAME)
            .profile(DEFAULT_PROFILE)
            .priority(DEFAULT_PRIORITY)
            .initialConfiguration(DEFAULT_INITIAL_CONFIGURATION)
            .runtimeConfiguration(DEFAULT_RUNTIME_CONFIGURATION)
            .deployType(DEFAULT_DEPLOY_TYPE)
            .deployDescriptor(DEFAULT_DEPLOY_DESCRIPTOR)
            .status(DEFAULT_STATUS);
        return service;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Service createUpdatedEntity(EntityManager em) {
        Service service = new Service()
            .name(UPDATED_NAME)
            .profile(UPDATED_PROFILE)
            .priority(UPDATED_PRIORITY)
            .initialConfiguration(UPDATED_INITIAL_CONFIGURATION)
            .runtimeConfiguration(UPDATED_RUNTIME_CONFIGURATION)
            .deployType(UPDATED_DEPLOY_TYPE)
            .deployDescriptor(UPDATED_DEPLOY_DESCRIPTOR)
            .status(UPDATED_STATUS);
        return service;
    }

    @BeforeEach
    public void initTest() {
        service = createEntity(em);
    }

    @Test
    @Transactional
    public void createService() throws Exception {
        int databaseSizeBeforeCreate = serviceRepository.findAll().size();
        // Create the Service
        restServiceMockMvc.perform(post("/api/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(service)))
            .andExpect(status().isCreated());

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll();
        assertThat(serviceList).hasSize(databaseSizeBeforeCreate + 1);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testService.getProfile()).isEqualTo(DEFAULT_PROFILE);
        assertThat(testService.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testService.getInitialConfiguration()).isEqualTo(DEFAULT_INITIAL_CONFIGURATION);
        assertThat(testService.getRuntimeConfiguration()).isEqualTo(DEFAULT_RUNTIME_CONFIGURATION);
        assertThat(testService.getDeployType()).isEqualTo(DEFAULT_DEPLOY_TYPE);
        assertThat(testService.getDeployDescriptor()).isEqualTo(DEFAULT_DEPLOY_DESCRIPTOR);
        assertThat(testService.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createServiceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = serviceRepository.findAll().size();

        // Create the Service with an existing ID
        service.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restServiceMockMvc.perform(post("/api/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(service)))
            .andExpect(status().isBadRequest());

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll();
        assertThat(serviceList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllServices() throws Exception {
        // Initialize the database
        serviceRepository.saveAndFlush(service);

        // Get all the serviceList
        restServiceMockMvc.perform(get("/api/services?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(service.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].profile").value(hasItem(DEFAULT_PROFILE)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].initialConfiguration").value(hasItem(DEFAULT_INITIAL_CONFIGURATION)))
            .andExpect(jsonPath("$.[*].runtimeConfiguration").value(hasItem(DEFAULT_RUNTIME_CONFIGURATION)))
            .andExpect(jsonPath("$.[*].deployType").value(hasItem(DEFAULT_DEPLOY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].deployDescriptor").value(hasItem(DEFAULT_DEPLOY_DESCRIPTOR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
    
    @Test
    @Transactional
    public void getService() throws Exception {
        // Initialize the database
        serviceRepository.saveAndFlush(service);

        // Get the service
        restServiceMockMvc.perform(get("/api/services/{id}", service.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(service.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.profile").value(DEFAULT_PROFILE))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY))
            .andExpect(jsonPath("$.initialConfiguration").value(DEFAULT_INITIAL_CONFIGURATION))
            .andExpect(jsonPath("$.runtimeConfiguration").value(DEFAULT_RUNTIME_CONFIGURATION))
            .andExpect(jsonPath("$.deployType").value(DEFAULT_DEPLOY_TYPE.toString()))
            .andExpect(jsonPath("$.deployDescriptor").value(DEFAULT_DEPLOY_DESCRIPTOR))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingService() throws Exception {
        // Get the service
        restServiceMockMvc.perform(get("/api/services/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateService() throws Exception {
        // Initialize the database
        serviceService.save(service);

        int databaseSizeBeforeUpdate = serviceRepository.findAll().size();

        // Update the service
        Service updatedService = serviceRepository.findById(service.getId()).get();
        // Disconnect from session so that the updates on updatedService are not directly saved in db
        em.detach(updatedService);
        updatedService
            .name(UPDATED_NAME)
            .profile(UPDATED_PROFILE)
            .priority(UPDATED_PRIORITY)
            .initialConfiguration(UPDATED_INITIAL_CONFIGURATION)
            .runtimeConfiguration(UPDATED_RUNTIME_CONFIGURATION)
            .deployType(UPDATED_DEPLOY_TYPE)
            .deployDescriptor(UPDATED_DEPLOY_DESCRIPTOR)
            .status(UPDATED_STATUS);

        restServiceMockMvc.perform(put("/api/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedService)))
            .andExpect(status().isOk());

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testService.getProfile()).isEqualTo(UPDATED_PROFILE);
        assertThat(testService.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testService.getInitialConfiguration()).isEqualTo(UPDATED_INITIAL_CONFIGURATION);
        assertThat(testService.getRuntimeConfiguration()).isEqualTo(UPDATED_RUNTIME_CONFIGURATION);
        assertThat(testService.getDeployType()).isEqualTo(UPDATED_DEPLOY_TYPE);
        assertThat(testService.getDeployDescriptor()).isEqualTo(UPDATED_DEPLOY_DESCRIPTOR);
        assertThat(testService.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void updateNonExistingService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceMockMvc.perform(put("/api/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(service)))
            .andExpect(status().isBadRequest());

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteService() throws Exception {
        // Initialize the database
        serviceService.save(service);

        int databaseSizeBeforeDelete = serviceRepository.findAll().size();

        // Delete the service
        restServiceMockMvc.perform(delete("/api/services/{id}", service.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Service> serviceList = serviceRepository.findAll();
        assertThat(serviceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
