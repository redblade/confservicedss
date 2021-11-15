package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.CatalogApp;
import eu.pledgerproject.confservice.repository.CatalogAppRepository;
import eu.pledgerproject.confservice.service.CatalogAppService;

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
 * Integration tests for the {@link CatalogAppResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CatalogAppResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_APP_DESCRIPTOR = "AAAAAAAAAA";
    private static final String UPDATED_APP_DESCRIPTOR = "BBBBBBBBBB";

    @Autowired
    private CatalogAppRepository catalogAppRepository;

    @Autowired
    private CatalogAppService catalogAppService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCatalogAppMockMvc;

    private CatalogApp catalogApp;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CatalogApp createEntity(EntityManager em) {
        CatalogApp catalogApp = new CatalogApp()
            .name(DEFAULT_NAME)
            .appDescriptor(DEFAULT_APP_DESCRIPTOR);
        return catalogApp;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CatalogApp createUpdatedEntity(EntityManager em) {
        CatalogApp catalogApp = new CatalogApp()
            .name(UPDATED_NAME)
            .appDescriptor(UPDATED_APP_DESCRIPTOR);
        return catalogApp;
    }

    @BeforeEach
    public void initTest() {
        catalogApp = createEntity(em);
    }

    @Test
    @Transactional
    public void createCatalogApp() throws Exception {
        int databaseSizeBeforeCreate = catalogAppRepository.findAll().size();
        // Create the CatalogApp
        restCatalogAppMockMvc.perform(post("/api/catalog-apps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogApp)))
            .andExpect(status().isCreated());

        // Validate the CatalogApp in the database
        List<CatalogApp> catalogAppList = catalogAppRepository.findAll();
        assertThat(catalogAppList).hasSize(databaseSizeBeforeCreate + 1);
        CatalogApp testCatalogApp = catalogAppList.get(catalogAppList.size() - 1);
        assertThat(testCatalogApp.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCatalogApp.getAppDescriptor()).isEqualTo(DEFAULT_APP_DESCRIPTOR);
    }

    @Test
    @Transactional
    public void createCatalogAppWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = catalogAppRepository.findAll().size();

        // Create the CatalogApp with an existing ID
        catalogApp.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCatalogAppMockMvc.perform(post("/api/catalog-apps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogApp)))
            .andExpect(status().isBadRequest());

        // Validate the CatalogApp in the database
        List<CatalogApp> catalogAppList = catalogAppRepository.findAll();
        assertThat(catalogAppList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCatalogApps() throws Exception {
        // Initialize the database
        catalogAppRepository.saveAndFlush(catalogApp);

        // Get all the catalogAppList
        restCatalogAppMockMvc.perform(get("/api/catalog-apps?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(catalogApp.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].appDescriptor").value(hasItem(DEFAULT_APP_DESCRIPTOR)));
    }
    
    @Test
    @Transactional
    public void getCatalogApp() throws Exception {
        // Initialize the database
        catalogAppRepository.saveAndFlush(catalogApp);

        // Get the catalogApp
        restCatalogAppMockMvc.perform(get("/api/catalog-apps/{id}", catalogApp.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(catalogApp.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.appDescriptor").value(DEFAULT_APP_DESCRIPTOR));
    }
    @Test
    @Transactional
    public void getNonExistingCatalogApp() throws Exception {
        // Get the catalogApp
        restCatalogAppMockMvc.perform(get("/api/catalog-apps/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCatalogApp() throws Exception {
        // Initialize the database
        catalogAppService.save(catalogApp);

        int databaseSizeBeforeUpdate = catalogAppRepository.findAll().size();

        // Update the catalogApp
        CatalogApp updatedCatalogApp = catalogAppRepository.findById(catalogApp.getId()).get();
        // Disconnect from session so that the updates on updatedCatalogApp are not directly saved in db
        em.detach(updatedCatalogApp);
        updatedCatalogApp
            .name(UPDATED_NAME)
            .appDescriptor(UPDATED_APP_DESCRIPTOR);

        restCatalogAppMockMvc.perform(put("/api/catalog-apps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCatalogApp)))
            .andExpect(status().isOk());

        // Validate the CatalogApp in the database
        List<CatalogApp> catalogAppList = catalogAppRepository.findAll();
        assertThat(catalogAppList).hasSize(databaseSizeBeforeUpdate);
        CatalogApp testCatalogApp = catalogAppList.get(catalogAppList.size() - 1);
        assertThat(testCatalogApp.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCatalogApp.getAppDescriptor()).isEqualTo(UPDATED_APP_DESCRIPTOR);
    }

    @Test
    @Transactional
    public void updateNonExistingCatalogApp() throws Exception {
        int databaseSizeBeforeUpdate = catalogAppRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCatalogAppMockMvc.perform(put("/api/catalog-apps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogApp)))
            .andExpect(status().isBadRequest());

        // Validate the CatalogApp in the database
        List<CatalogApp> catalogAppList = catalogAppRepository.findAll();
        assertThat(catalogAppList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCatalogApp() throws Exception {
        // Initialize the database
        catalogAppService.save(catalogApp);

        int databaseSizeBeforeDelete = catalogAppRepository.findAll().size();

        // Delete the catalogApp
        restCatalogAppMockMvc.perform(delete("/api/catalog-apps/{id}", catalogApp.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CatalogApp> catalogAppList = catalogAppRepository.findAll();
        assertThat(catalogAppList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
