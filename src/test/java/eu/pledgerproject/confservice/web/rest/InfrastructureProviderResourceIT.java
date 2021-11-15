package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.InfrastructureProvider;
import eu.pledgerproject.confservice.repository.InfrastructureProviderRepository;
import eu.pledgerproject.confservice.service.InfrastructureProviderService;

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
 * Integration tests for the {@link InfrastructureProviderResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class InfrastructureProviderResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ORGANISATION = "AAAAAAAAAA";
    private static final String UPDATED_ORGANISATION = "BBBBBBBBBB";

    @Autowired
    private InfrastructureProviderRepository infrastructureProviderRepository;

    @Autowired
    private InfrastructureProviderService infrastructureProviderService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInfrastructureProviderMockMvc;

    private InfrastructureProvider infrastructureProvider;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InfrastructureProvider createEntity(EntityManager em) {
        InfrastructureProvider infrastructureProvider = new InfrastructureProvider()
            .name(DEFAULT_NAME)
            .organisation(DEFAULT_ORGANISATION);
        return infrastructureProvider;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InfrastructureProvider createUpdatedEntity(EntityManager em) {
        InfrastructureProvider infrastructureProvider = new InfrastructureProvider()
            .name(UPDATED_NAME)
            .organisation(UPDATED_ORGANISATION);
        return infrastructureProvider;
    }

    @BeforeEach
    public void initTest() {
        infrastructureProvider = createEntity(em);
    }

    @Test
    @Transactional
    public void createInfrastructureProvider() throws Exception {
        int databaseSizeBeforeCreate = infrastructureProviderRepository.findAll().size();
        // Create the InfrastructureProvider
        restInfrastructureProviderMockMvc.perform(post("/api/infrastructure-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureProvider)))
            .andExpect(status().isCreated());

        // Validate the InfrastructureProvider in the database
        List<InfrastructureProvider> infrastructureProviderList = infrastructureProviderRepository.findAll();
        assertThat(infrastructureProviderList).hasSize(databaseSizeBeforeCreate + 1);
        InfrastructureProvider testInfrastructureProvider = infrastructureProviderList.get(infrastructureProviderList.size() - 1);
        assertThat(testInfrastructureProvider.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testInfrastructureProvider.getOrganisation()).isEqualTo(DEFAULT_ORGANISATION);
    }

    @Test
    @Transactional
    public void createInfrastructureProviderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = infrastructureProviderRepository.findAll().size();

        // Create the InfrastructureProvider with an existing ID
        infrastructureProvider.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restInfrastructureProviderMockMvc.perform(post("/api/infrastructure-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureProvider)))
            .andExpect(status().isBadRequest());

        // Validate the InfrastructureProvider in the database
        List<InfrastructureProvider> infrastructureProviderList = infrastructureProviderRepository.findAll();
        assertThat(infrastructureProviderList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllInfrastructureProviders() throws Exception {
        // Initialize the database
        infrastructureProviderRepository.saveAndFlush(infrastructureProvider);

        // Get all the infrastructureProviderList
        restInfrastructureProviderMockMvc.perform(get("/api/infrastructure-providers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(infrastructureProvider.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].organisation").value(hasItem(DEFAULT_ORGANISATION)));
    }
    
    @Test
    @Transactional
    public void getInfrastructureProvider() throws Exception {
        // Initialize the database
        infrastructureProviderRepository.saveAndFlush(infrastructureProvider);

        // Get the infrastructureProvider
        restInfrastructureProviderMockMvc.perform(get("/api/infrastructure-providers/{id}", infrastructureProvider.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(infrastructureProvider.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.organisation").value(DEFAULT_ORGANISATION));
    }
    @Test
    @Transactional
    public void getNonExistingInfrastructureProvider() throws Exception {
        // Get the infrastructureProvider
        restInfrastructureProviderMockMvc.perform(get("/api/infrastructure-providers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInfrastructureProvider() throws Exception {
        // Initialize the database
        infrastructureProviderService.save(infrastructureProvider);

        int databaseSizeBeforeUpdate = infrastructureProviderRepository.findAll().size();

        // Update the infrastructureProvider
        InfrastructureProvider updatedInfrastructureProvider = infrastructureProviderRepository.findById(infrastructureProvider.getId()).get();
        // Disconnect from session so that the updates on updatedInfrastructureProvider are not directly saved in db
        em.detach(updatedInfrastructureProvider);
        updatedInfrastructureProvider
            .name(UPDATED_NAME)
            .organisation(UPDATED_ORGANISATION);

        restInfrastructureProviderMockMvc.perform(put("/api/infrastructure-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedInfrastructureProvider)))
            .andExpect(status().isOk());

        // Validate the InfrastructureProvider in the database
        List<InfrastructureProvider> infrastructureProviderList = infrastructureProviderRepository.findAll();
        assertThat(infrastructureProviderList).hasSize(databaseSizeBeforeUpdate);
        InfrastructureProvider testInfrastructureProvider = infrastructureProviderList.get(infrastructureProviderList.size() - 1);
        assertThat(testInfrastructureProvider.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testInfrastructureProvider.getOrganisation()).isEqualTo(UPDATED_ORGANISATION);
    }

    @Test
    @Transactional
    public void updateNonExistingInfrastructureProvider() throws Exception {
        int databaseSizeBeforeUpdate = infrastructureProviderRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInfrastructureProviderMockMvc.perform(put("/api/infrastructure-providers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(infrastructureProvider)))
            .andExpect(status().isBadRequest());

        // Validate the InfrastructureProvider in the database
        List<InfrastructureProvider> infrastructureProviderList = infrastructureProviderRepository.findAll();
        assertThat(infrastructureProviderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteInfrastructureProvider() throws Exception {
        // Initialize the database
        infrastructureProviderService.save(infrastructureProvider);

        int databaseSizeBeforeDelete = infrastructureProviderRepository.findAll().size();

        // Delete the infrastructureProvider
        restInfrastructureProviderMockMvc.perform(delete("/api/infrastructure-providers/{id}", infrastructureProvider.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<InfrastructureProvider> infrastructureProviderList = infrastructureProviderRepository.findAll();
        assertThat(infrastructureProviderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
