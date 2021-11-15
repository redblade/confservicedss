package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Guarantee;
import eu.pledgerproject.confservice.repository.GuaranteeRepository;
import eu.pledgerproject.confservice.service.GuaranteeService;

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
 * Integration tests for the {@link GuaranteeResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class GuaranteeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONSTRAINT = "AAAAAAAAAA";
    private static final String UPDATED_CONSTRAINT = "BBBBBBBBBB";

    private static final String DEFAULT_THRESHOLD_WARNING = "AAAAAAAAAA";
    private static final String UPDATED_THRESHOLD_WARNING = "BBBBBBBBBB";

    private static final String DEFAULT_THRESHOLD_MILD = "AAAAAAAAAA";
    private static final String UPDATED_THRESHOLD_MILD = "BBBBBBBBBB";

    private static final String DEFAULT_THRESHOLD_SERIOUS = "AAAAAAAAAA";
    private static final String UPDATED_THRESHOLD_SERIOUS = "BBBBBBBBBB";

    private static final String DEFAULT_THRESHOLD_SEVERE = "AAAAAAAAAA";
    private static final String UPDATED_THRESHOLD_SEVERE = "BBBBBBBBBB";

    private static final String DEFAULT_THRESHOLD_CATASTROPHIC = "AAAAAAAAAA";
    private static final String UPDATED_THRESHOLD_CATASTROPHIC = "BBBBBBBBBB";

    @Autowired
    private GuaranteeRepository guaranteeRepository;

    @Autowired
    private GuaranteeService guaranteeService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGuaranteeMockMvc;

    private Guarantee guarantee;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Guarantee createEntity(EntityManager em) {
        Guarantee guarantee = new Guarantee()
            .name(DEFAULT_NAME)
            .constraint(DEFAULT_CONSTRAINT)
            .thresholdWarning(DEFAULT_THRESHOLD_WARNING)
            .thresholdMild(DEFAULT_THRESHOLD_MILD)
            .thresholdSerious(DEFAULT_THRESHOLD_SERIOUS)
            .thresholdSevere(DEFAULT_THRESHOLD_SEVERE)
            .thresholdCatastrophic(DEFAULT_THRESHOLD_CATASTROPHIC);
        return guarantee;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Guarantee createUpdatedEntity(EntityManager em) {
        Guarantee guarantee = new Guarantee()
            .name(UPDATED_NAME)
            .constraint(UPDATED_CONSTRAINT)
            .thresholdWarning(UPDATED_THRESHOLD_WARNING)
            .thresholdMild(UPDATED_THRESHOLD_MILD)
            .thresholdSerious(UPDATED_THRESHOLD_SERIOUS)
            .thresholdSevere(UPDATED_THRESHOLD_SEVERE)
            .thresholdCatastrophic(UPDATED_THRESHOLD_CATASTROPHIC);
        return guarantee;
    }

    @BeforeEach
    public void initTest() {
        guarantee = createEntity(em);
    }

    @Test
    @Transactional
    public void createGuarantee() throws Exception {
        int databaseSizeBeforeCreate = guaranteeRepository.findAll().size();
        // Create the Guarantee
        restGuaranteeMockMvc.perform(post("/api/guarantees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(guarantee)))
            .andExpect(status().isCreated());

        // Validate the Guarantee in the database
        List<Guarantee> guaranteeList = guaranteeRepository.findAll();
        assertThat(guaranteeList).hasSize(databaseSizeBeforeCreate + 1);
        Guarantee testGuarantee = guaranteeList.get(guaranteeList.size() - 1);
        assertThat(testGuarantee.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testGuarantee.getConstraint()).isEqualTo(DEFAULT_CONSTRAINT);
        assertThat(testGuarantee.getThresholdWarning()).isEqualTo(DEFAULT_THRESHOLD_WARNING);
        assertThat(testGuarantee.getThresholdMild()).isEqualTo(DEFAULT_THRESHOLD_MILD);
        assertThat(testGuarantee.getThresholdSerious()).isEqualTo(DEFAULT_THRESHOLD_SERIOUS);
        assertThat(testGuarantee.getThresholdSevere()).isEqualTo(DEFAULT_THRESHOLD_SEVERE);
        assertThat(testGuarantee.getThresholdCatastrophic()).isEqualTo(DEFAULT_THRESHOLD_CATASTROPHIC);
    }

    @Test
    @Transactional
    public void createGuaranteeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = guaranteeRepository.findAll().size();

        // Create the Guarantee with an existing ID
        guarantee.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGuaranteeMockMvc.perform(post("/api/guarantees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(guarantee)))
            .andExpect(status().isBadRequest());

        // Validate the Guarantee in the database
        List<Guarantee> guaranteeList = guaranteeRepository.findAll();
        assertThat(guaranteeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllGuarantees() throws Exception {
        // Initialize the database
        guaranteeRepository.saveAndFlush(guarantee);

        // Get all the guaranteeList
        restGuaranteeMockMvc.perform(get("/api/guarantees?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(guarantee.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].constraint").value(hasItem(DEFAULT_CONSTRAINT)))
            .andExpect(jsonPath("$.[*].thresholdWarning").value(hasItem(DEFAULT_THRESHOLD_WARNING)))
            .andExpect(jsonPath("$.[*].thresholdMild").value(hasItem(DEFAULT_THRESHOLD_MILD)))
            .andExpect(jsonPath("$.[*].thresholdSerious").value(hasItem(DEFAULT_THRESHOLD_SERIOUS)))
            .andExpect(jsonPath("$.[*].thresholdSevere").value(hasItem(DEFAULT_THRESHOLD_SEVERE)))
            .andExpect(jsonPath("$.[*].thresholdCatastrophic").value(hasItem(DEFAULT_THRESHOLD_CATASTROPHIC)));
    }
    
    @Test
    @Transactional
    public void getGuarantee() throws Exception {
        // Initialize the database
        guaranteeRepository.saveAndFlush(guarantee);

        // Get the guarantee
        restGuaranteeMockMvc.perform(get("/api/guarantees/{id}", guarantee.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(guarantee.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.constraint").value(DEFAULT_CONSTRAINT))
            .andExpect(jsonPath("$.thresholdWarning").value(DEFAULT_THRESHOLD_WARNING))
            .andExpect(jsonPath("$.thresholdMild").value(DEFAULT_THRESHOLD_MILD))
            .andExpect(jsonPath("$.thresholdSerious").value(DEFAULT_THRESHOLD_SERIOUS))
            .andExpect(jsonPath("$.thresholdSevere").value(DEFAULT_THRESHOLD_SEVERE))
            .andExpect(jsonPath("$.thresholdCatastrophic").value(DEFAULT_THRESHOLD_CATASTROPHIC));
    }
    @Test
    @Transactional
    public void getNonExistingGuarantee() throws Exception {
        // Get the guarantee
        restGuaranteeMockMvc.perform(get("/api/guarantees/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGuarantee() throws Exception {
        // Initialize the database
        guaranteeService.save(guarantee);

        int databaseSizeBeforeUpdate = guaranteeRepository.findAll().size();

        // Update the guarantee
        Guarantee updatedGuarantee = guaranteeRepository.findById(guarantee.getId()).get();
        // Disconnect from session so that the updates on updatedGuarantee are not directly saved in db
        em.detach(updatedGuarantee);
        updatedGuarantee
            .name(UPDATED_NAME)
            .constraint(UPDATED_CONSTRAINT)
            .thresholdWarning(UPDATED_THRESHOLD_WARNING)
            .thresholdMild(UPDATED_THRESHOLD_MILD)
            .thresholdSerious(UPDATED_THRESHOLD_SERIOUS)
            .thresholdSevere(UPDATED_THRESHOLD_SEVERE)
            .thresholdCatastrophic(UPDATED_THRESHOLD_CATASTROPHIC);

        restGuaranteeMockMvc.perform(put("/api/guarantees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedGuarantee)))
            .andExpect(status().isOk());

        // Validate the Guarantee in the database
        List<Guarantee> guaranteeList = guaranteeRepository.findAll();
        assertThat(guaranteeList).hasSize(databaseSizeBeforeUpdate);
        Guarantee testGuarantee = guaranteeList.get(guaranteeList.size() - 1);
        assertThat(testGuarantee.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testGuarantee.getConstraint()).isEqualTo(UPDATED_CONSTRAINT);
        assertThat(testGuarantee.getThresholdWarning()).isEqualTo(UPDATED_THRESHOLD_WARNING);
        assertThat(testGuarantee.getThresholdMild()).isEqualTo(UPDATED_THRESHOLD_MILD);
        assertThat(testGuarantee.getThresholdSerious()).isEqualTo(UPDATED_THRESHOLD_SERIOUS);
        assertThat(testGuarantee.getThresholdSevere()).isEqualTo(UPDATED_THRESHOLD_SEVERE);
        assertThat(testGuarantee.getThresholdCatastrophic()).isEqualTo(UPDATED_THRESHOLD_CATASTROPHIC);
    }

    @Test
    @Transactional
    public void updateNonExistingGuarantee() throws Exception {
        int databaseSizeBeforeUpdate = guaranteeRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGuaranteeMockMvc.perform(put("/api/guarantees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(guarantee)))
            .andExpect(status().isBadRequest());

        // Validate the Guarantee in the database
        List<Guarantee> guaranteeList = guaranteeRepository.findAll();
        assertThat(guaranteeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGuarantee() throws Exception {
        // Initialize the database
        guaranteeService.save(guarantee);

        int databaseSizeBeforeDelete = guaranteeRepository.findAll().size();

        // Delete the guarantee
        restGuaranteeMockMvc.perform(delete("/api/guarantees/{id}", guarantee.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Guarantee> guaranteeList = guaranteeRepository.findAll();
        assertThat(guaranteeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
