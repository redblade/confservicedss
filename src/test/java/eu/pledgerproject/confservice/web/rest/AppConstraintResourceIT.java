package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.AppConstraint;
import eu.pledgerproject.confservice.repository.AppConstraintRepository;
import eu.pledgerproject.confservice.service.AppConstraintService;

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
 * Integration tests for the {@link AppConstraintResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class AppConstraintResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE_TYPE = "BBBBBBBBBB";

    @Autowired
    private AppConstraintRepository appConstraintRepository;

    @Autowired
    private AppConstraintService appConstraintService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAppConstraintMockMvc;

    private AppConstraint appConstraint;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppConstraint createEntity(EntityManager em) {
        AppConstraint appConstraint = new AppConstraint()
            .name(DEFAULT_NAME)
            .category(DEFAULT_CATEGORY)
            .value(DEFAULT_VALUE)
            .valueType(DEFAULT_VALUE_TYPE);
        return appConstraint;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppConstraint createUpdatedEntity(EntityManager em) {
        AppConstraint appConstraint = new AppConstraint()
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY)
            .value(UPDATED_VALUE)
            .valueType(UPDATED_VALUE_TYPE);
        return appConstraint;
    }

    @BeforeEach
    public void initTest() {
        appConstraint = createEntity(em);
    }

    @Test
    @Transactional
    public void createAppConstraint() throws Exception {
        int databaseSizeBeforeCreate = appConstraintRepository.findAll().size();
        // Create the AppConstraint
        restAppConstraintMockMvc.perform(post("/api/app-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(appConstraint)))
            .andExpect(status().isCreated());

        // Validate the AppConstraint in the database
        List<AppConstraint> appConstraintList = appConstraintRepository.findAll();
        assertThat(appConstraintList).hasSize(databaseSizeBeforeCreate + 1);
        AppConstraint testAppConstraint = appConstraintList.get(appConstraintList.size() - 1);
        assertThat(testAppConstraint.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAppConstraint.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testAppConstraint.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testAppConstraint.getValueType()).isEqualTo(DEFAULT_VALUE_TYPE);
    }

    @Test
    @Transactional
    public void createAppConstraintWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = appConstraintRepository.findAll().size();

        // Create the AppConstraint with an existing ID
        appConstraint.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppConstraintMockMvc.perform(post("/api/app-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(appConstraint)))
            .andExpect(status().isBadRequest());

        // Validate the AppConstraint in the database
        List<AppConstraint> appConstraintList = appConstraintRepository.findAll();
        assertThat(appConstraintList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllAppConstraints() throws Exception {
        // Initialize the database
        appConstraintRepository.saveAndFlush(appConstraint);

        // Get all the appConstraintList
        restAppConstraintMockMvc.perform(get("/api/app-constraints?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appConstraint.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].valueType").value(hasItem(DEFAULT_VALUE_TYPE)));
    }
    
    @Test
    @Transactional
    public void getAppConstraint() throws Exception {
        // Initialize the database
        appConstraintRepository.saveAndFlush(appConstraint);

        // Get the appConstraint
        restAppConstraintMockMvc.perform(get("/api/app-constraints/{id}", appConstraint.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(appConstraint.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.valueType").value(DEFAULT_VALUE_TYPE));
    }
    @Test
    @Transactional
    public void getNonExistingAppConstraint() throws Exception {
        // Get the appConstraint
        restAppConstraintMockMvc.perform(get("/api/app-constraints/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAppConstraint() throws Exception {
        // Initialize the database
        appConstraintService.save(appConstraint);

        int databaseSizeBeforeUpdate = appConstraintRepository.findAll().size();

        // Update the appConstraint
        AppConstraint updatedAppConstraint = appConstraintRepository.findById(appConstraint.getId()).get();
        // Disconnect from session so that the updates on updatedAppConstraint are not directly saved in db
        em.detach(updatedAppConstraint);
        updatedAppConstraint
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY)
            .value(UPDATED_VALUE)
            .valueType(UPDATED_VALUE_TYPE);

        restAppConstraintMockMvc.perform(put("/api/app-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedAppConstraint)))
            .andExpect(status().isOk());

        // Validate the AppConstraint in the database
        List<AppConstraint> appConstraintList = appConstraintRepository.findAll();
        assertThat(appConstraintList).hasSize(databaseSizeBeforeUpdate);
        AppConstraint testAppConstraint = appConstraintList.get(appConstraintList.size() - 1);
        assertThat(testAppConstraint.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAppConstraint.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testAppConstraint.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testAppConstraint.getValueType()).isEqualTo(UPDATED_VALUE_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingAppConstraint() throws Exception {
        int databaseSizeBeforeUpdate = appConstraintRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppConstraintMockMvc.perform(put("/api/app-constraints")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(appConstraint)))
            .andExpect(status().isBadRequest());

        // Validate the AppConstraint in the database
        List<AppConstraint> appConstraintList = appConstraintRepository.findAll();
        assertThat(appConstraintList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAppConstraint() throws Exception {
        // Initialize the database
        appConstraintService.save(appConstraint);

        int databaseSizeBeforeDelete = appConstraintRepository.findAll().size();

        // Delete the appConstraint
        restAppConstraintMockMvc.perform(delete("/api/app-constraints/{id}", appConstraint.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AppConstraint> appConstraintList = appConstraintRepository.findAll();
        assertThat(appConstraintList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
