package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.ServiceConstraint;
import eu.pledgerproject.confservice.repository.ServiceConstraintRepository;
import eu.pledgerproject.confservice.service.ServiceConstraintService;

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
 * Integration tests for the {@link ServiceConstraintResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ServiceConstraintResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE_TYPE = "BBBBBBBBBB";

    private static final Integer DEFAULT_PRIORITY = 1;
    private static final Integer UPDATED_PRIORITY = 2;

    @Autowired
    private ServiceConstraintRepository serviceConstraintRepository;

    @Autowired
    private ServiceConstraintService serviceConstraintService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restServiceConstraintMockMvc;

    private ServiceConstraint serviceConstraint;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceConstraint createEntity(EntityManager em) {
        ServiceConstraint serviceConstraint = new ServiceConstraint()
            .name(DEFAULT_NAME)
            .category(DEFAULT_CATEGORY)
            .value(DEFAULT_VALUE)
            .valueType(DEFAULT_VALUE_TYPE)
            .priority(DEFAULT_PRIORITY);
        return serviceConstraint;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceConstraint createUpdatedEntity(EntityManager em) {
        ServiceConstraint serviceConstraint = new ServiceConstraint()
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY)
            .value(UPDATED_VALUE)
            .valueType(UPDATED_VALUE_TYPE)
            .priority(UPDATED_PRIORITY);
        return serviceConstraint;
    }

    @BeforeEach
    public void initTest() {
        serviceConstraint = createEntity(em);
    }

    @Test
    @Transactional
    public void createServiceConstraint() throws Exception {
        int databaseSizeBeforeCreate = serviceConstraintRepository.findAll().size();
        // Create the ServiceConstraint
        restServiceConstraintMockMvc.perform(post("/api/service-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceConstraint)))
            .andExpect(status().isCreated());

        // Validate the ServiceConstraint in the database
        List<ServiceConstraint> serviceConstraintList = serviceConstraintRepository.findAll();
        assertThat(serviceConstraintList).hasSize(databaseSizeBeforeCreate + 1);
        ServiceConstraint testServiceConstraint = serviceConstraintList.get(serviceConstraintList.size() - 1);
        assertThat(testServiceConstraint.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testServiceConstraint.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testServiceConstraint.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testServiceConstraint.getValueType()).isEqualTo(DEFAULT_VALUE_TYPE);
        assertThat(testServiceConstraint.getPriority()).isEqualTo(DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void createServiceConstraintWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = serviceConstraintRepository.findAll().size();

        // Create the ServiceConstraint with an existing ID
        serviceConstraint.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restServiceConstraintMockMvc.perform(post("/api/service-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceConstraint)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceConstraint in the database
        List<ServiceConstraint> serviceConstraintList = serviceConstraintRepository.findAll();
        assertThat(serviceConstraintList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllServiceConstraints() throws Exception {
        // Initialize the database
        serviceConstraintRepository.saveAndFlush(serviceConstraint);

        // Get all the serviceConstraintList
        restServiceConstraintMockMvc.perform(get("/api/service-constraints?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(serviceConstraint.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].valueType").value(hasItem(DEFAULT_VALUE_TYPE)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)));
    }
    
    @Test
    @Transactional
    public void getServiceConstraint() throws Exception {
        // Initialize the database
        serviceConstraintRepository.saveAndFlush(serviceConstraint);

        // Get the serviceConstraint
        restServiceConstraintMockMvc.perform(get("/api/service-constraints/{id}", serviceConstraint.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(serviceConstraint.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.valueType").value(DEFAULT_VALUE_TYPE))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY));
    }
    @Test
    @Transactional
    public void getNonExistingServiceConstraint() throws Exception {
        // Get the serviceConstraint
        restServiceConstraintMockMvc.perform(get("/api/service-constraints/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateServiceConstraint() throws Exception {
        // Initialize the database
        serviceConstraintService.save(serviceConstraint);

        int databaseSizeBeforeUpdate = serviceConstraintRepository.findAll().size();

        // Update the serviceConstraint
        ServiceConstraint updatedServiceConstraint = serviceConstraintRepository.findById(serviceConstraint.getId()).get();
        // Disconnect from session so that the updates on updatedServiceConstraint are not directly saved in db
        em.detach(updatedServiceConstraint);
        updatedServiceConstraint
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY)
            .value(UPDATED_VALUE)
            .valueType(UPDATED_VALUE_TYPE)
            .priority(UPDATED_PRIORITY);

        restServiceConstraintMockMvc.perform(put("/api/service-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedServiceConstraint)))
            .andExpect(status().isOk());

        // Validate the ServiceConstraint in the database
        List<ServiceConstraint> serviceConstraintList = serviceConstraintRepository.findAll();
        assertThat(serviceConstraintList).hasSize(databaseSizeBeforeUpdate);
        ServiceConstraint testServiceConstraint = serviceConstraintList.get(serviceConstraintList.size() - 1);
        assertThat(testServiceConstraint.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testServiceConstraint.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testServiceConstraint.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testServiceConstraint.getValueType()).isEqualTo(UPDATED_VALUE_TYPE);
        assertThat(testServiceConstraint.getPriority()).isEqualTo(UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    public void updateNonExistingServiceConstraint() throws Exception {
        int databaseSizeBeforeUpdate = serviceConstraintRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceConstraintMockMvc.perform(put("/api/service-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceConstraint)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceConstraint in the database
        List<ServiceConstraint> serviceConstraintList = serviceConstraintRepository.findAll();
        assertThat(serviceConstraintList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteServiceConstraint() throws Exception {
        // Initialize the database
        serviceConstraintService.save(serviceConstraint);

        int databaseSizeBeforeDelete = serviceConstraintRepository.findAll().size();

        // Delete the serviceConstraint
        restServiceConstraintMockMvc.perform(delete("/api/service-constraints/{id}", serviceConstraint.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ServiceConstraint> serviceConstraintList = serviceConstraintRepository.findAll();
        assertThat(serviceConstraintList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
