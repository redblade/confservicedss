package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.ServiceOptimisation;
import eu.pledgerproject.confservice.repository.ServiceOptimisationRepository;
import eu.pledgerproject.confservice.service.ServiceOptimisationService;

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
 * Integration tests for the {@link ServiceOptimisationResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ServiceOptimisationResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_OPTIMISATION = "AAAAAAAAAA";
    private static final String UPDATED_OPTIMISATION = "BBBBBBBBBB";

    private static final String DEFAULT_PARAMETERS = "AAAAAAAAAA";
    private static final String UPDATED_PARAMETERS = "BBBBBBBBBB";

    @Autowired
    private ServiceOptimisationRepository serviceOptimisationRepository;

    @Autowired
    private ServiceOptimisationService serviceOptimisationService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restServiceOptimisationMockMvc;

    private ServiceOptimisation serviceOptimisation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceOptimisation createEntity(EntityManager em) {
        ServiceOptimisation serviceOptimisation = new ServiceOptimisation()
            .name(DEFAULT_NAME)
            .optimisation(DEFAULT_OPTIMISATION)
            .parameters(DEFAULT_PARAMETERS);
        return serviceOptimisation;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ServiceOptimisation createUpdatedEntity(EntityManager em) {
        ServiceOptimisation serviceOptimisation = new ServiceOptimisation()
            .name(UPDATED_NAME)
            .optimisation(UPDATED_OPTIMISATION)
            .parameters(UPDATED_PARAMETERS);
        return serviceOptimisation;
    }

    @BeforeEach
    public void initTest() {
        serviceOptimisation = createEntity(em);
    }

    @Test
    @Transactional
    public void createServiceOptimisation() throws Exception {
        int databaseSizeBeforeCreate = serviceOptimisationRepository.findAll().size();
        // Create the ServiceOptimisation
        restServiceOptimisationMockMvc.perform(post("/api/service-optimisations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceOptimisation)))
            .andExpect(status().isCreated());

        // Validate the ServiceOptimisation in the database
        List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findAll();
        assertThat(serviceOptimisationList).hasSize(databaseSizeBeforeCreate + 1);
        ServiceOptimisation testServiceOptimisation = serviceOptimisationList.get(serviceOptimisationList.size() - 1);
        assertThat(testServiceOptimisation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testServiceOptimisation.getOptimisation()).isEqualTo(DEFAULT_OPTIMISATION);
        assertThat(testServiceOptimisation.getParameters()).isEqualTo(DEFAULT_PARAMETERS);
    }

    @Test
    @Transactional
    public void createServiceOptimisationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = serviceOptimisationRepository.findAll().size();

        // Create the ServiceOptimisation with an existing ID
        serviceOptimisation.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restServiceOptimisationMockMvc.perform(post("/api/service-optimisations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceOptimisation)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceOptimisation in the database
        List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findAll();
        assertThat(serviceOptimisationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllServiceOptimisations() throws Exception {
        // Initialize the database
        serviceOptimisationRepository.saveAndFlush(serviceOptimisation);

        // Get all the serviceOptimisationList
        restServiceOptimisationMockMvc.perform(get("/api/service-optimisations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(serviceOptimisation.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].optimisation").value(hasItem(DEFAULT_OPTIMISATION)))
            .andExpect(jsonPath("$.[*].parameters").value(hasItem(DEFAULT_PARAMETERS)));
    }
    
    @Test
    @Transactional
    public void getServiceOptimisation() throws Exception {
        // Initialize the database
        serviceOptimisationRepository.saveAndFlush(serviceOptimisation);

        // Get the serviceOptimisation
        restServiceOptimisationMockMvc.perform(get("/api/service-optimisations/{id}", serviceOptimisation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(serviceOptimisation.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.optimisation").value(DEFAULT_OPTIMISATION))
            .andExpect(jsonPath("$.parameters").value(DEFAULT_PARAMETERS));
    }
    @Test
    @Transactional
    public void getNonExistingServiceOptimisation() throws Exception {
        // Get the serviceOptimisation
        restServiceOptimisationMockMvc.perform(get("/api/service-optimisations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateServiceOptimisation() throws Exception {
        // Initialize the database
        serviceOptimisationService.save(serviceOptimisation);

        int databaseSizeBeforeUpdate = serviceOptimisationRepository.findAll().size();

        // Update the serviceOptimisation
        ServiceOptimisation updatedServiceOptimisation = serviceOptimisationRepository.findById(serviceOptimisation.getId()).get();
        // Disconnect from session so that the updates on updatedServiceOptimisation are not directly saved in db
        em.detach(updatedServiceOptimisation);
        updatedServiceOptimisation
            .name(UPDATED_NAME)
            .optimisation(UPDATED_OPTIMISATION)
            .parameters(UPDATED_PARAMETERS);

        restServiceOptimisationMockMvc.perform(put("/api/service-optimisations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedServiceOptimisation)))
            .andExpect(status().isOk());

        // Validate the ServiceOptimisation in the database
        List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findAll();
        assertThat(serviceOptimisationList).hasSize(databaseSizeBeforeUpdate);
        ServiceOptimisation testServiceOptimisation = serviceOptimisationList.get(serviceOptimisationList.size() - 1);
        assertThat(testServiceOptimisation.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testServiceOptimisation.getOptimisation()).isEqualTo(UPDATED_OPTIMISATION);
        assertThat(testServiceOptimisation.getParameters()).isEqualTo(UPDATED_PARAMETERS);
    }

    @Test
    @Transactional
    public void updateNonExistingServiceOptimisation() throws Exception {
        int databaseSizeBeforeUpdate = serviceOptimisationRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServiceOptimisationMockMvc.perform(put("/api/service-optimisations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(serviceOptimisation)))
            .andExpect(status().isBadRequest());

        // Validate the ServiceOptimisation in the database
        List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findAll();
        assertThat(serviceOptimisationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteServiceOptimisation() throws Exception {
        // Initialize the database
        serviceOptimisationService.save(serviceOptimisation);

        int databaseSizeBeforeDelete = serviceOptimisationRepository.findAll().size();

        // Delete the serviceOptimisation
        restServiceOptimisationMockMvc.perform(delete("/api/service-optimisations/{id}", serviceOptimisation.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ServiceOptimisation> serviceOptimisationList = serviceOptimisationRepository.findAll();
        assertThat(serviceOptimisationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
