package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.service.NodeReportService;

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
 * Integration tests for the {@link NodeReportResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class NodeReportResourceIT {

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final Double DEFAULT_VALUE = 1D;
    private static final Double UPDATED_VALUE = 2D;

    @Autowired
    private NodeReportRepository nodeReportRepository;

    @Autowired
    private NodeReportService nodeReportService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNodeReportMockMvc;

    private NodeReport nodeReport;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NodeReport createEntity(EntityManager em) {
        NodeReport nodeReport = new NodeReport()
            .timestamp(DEFAULT_TIMESTAMP)
            .category(DEFAULT_CATEGORY)
            .key(DEFAULT_KEY)
            .value(DEFAULT_VALUE);
        return nodeReport;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NodeReport createUpdatedEntity(EntityManager em) {
        NodeReport nodeReport = new NodeReport()
            .timestamp(UPDATED_TIMESTAMP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);
        return nodeReport;
    }

    @BeforeEach
    public void initTest() {
        nodeReport = createEntity(em);
    }

    @Test
    @Transactional
    public void createNodeReport() throws Exception {
        int databaseSizeBeforeCreate = nodeReportRepository.findAll().size();
        // Create the NodeReport
        restNodeReportMockMvc.perform(post("/api/node-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(nodeReport)))
            .andExpect(status().isCreated());

        // Validate the NodeReport in the database
        List<NodeReport> nodeReportList = nodeReportRepository.findAll();
        assertThat(nodeReportList).hasSize(databaseSizeBeforeCreate + 1);
        NodeReport testNodeReport = nodeReportList.get(nodeReportList.size() - 1);
        assertThat(testNodeReport.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testNodeReport.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testNodeReport.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testNodeReport.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createNodeReportWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = nodeReportRepository.findAll().size();

        // Create the NodeReport with an existing ID
        nodeReport.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restNodeReportMockMvc.perform(post("/api/node-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(nodeReport)))
            .andExpect(status().isBadRequest());

        // Validate the NodeReport in the database
        List<NodeReport> nodeReportList = nodeReportRepository.findAll();
        assertThat(nodeReportList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllNodeReports() throws Exception {
        // Initialize the database
        nodeReportRepository.saveAndFlush(nodeReport);

        // Get all the nodeReportList
        restNodeReportMockMvc.perform(get("/api/node-reports?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(nodeReport.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getNodeReport() throws Exception {
        // Initialize the database
        nodeReportRepository.saveAndFlush(nodeReport);

        // Get the nodeReport
        restNodeReportMockMvc.perform(get("/api/node-reports/{id}", nodeReport.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(nodeReport.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.doubleValue()));
    }
    @Test
    @Transactional
    public void getNonExistingNodeReport() throws Exception {
        // Get the nodeReport
        restNodeReportMockMvc.perform(get("/api/node-reports/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNodeReport() throws Exception {
        // Initialize the database
        nodeReportService.save(nodeReport);

        int databaseSizeBeforeUpdate = nodeReportRepository.findAll().size();

        // Update the nodeReport
        NodeReport updatedNodeReport = nodeReportRepository.findById(nodeReport.getId()).get();
        // Disconnect from session so that the updates on updatedNodeReport are not directly saved in db
        em.detach(updatedNodeReport);
        updatedNodeReport
            .timestamp(UPDATED_TIMESTAMP)
            .category(UPDATED_CATEGORY)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE);

        restNodeReportMockMvc.perform(put("/api/node-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedNodeReport)))
            .andExpect(status().isOk());

        // Validate the NodeReport in the database
        List<NodeReport> nodeReportList = nodeReportRepository.findAll();
        assertThat(nodeReportList).hasSize(databaseSizeBeforeUpdate);
        NodeReport testNodeReport = nodeReportList.get(nodeReportList.size() - 1);
        assertThat(testNodeReport.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testNodeReport.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testNodeReport.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testNodeReport.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingNodeReport() throws Exception {
        int databaseSizeBeforeUpdate = nodeReportRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNodeReportMockMvc.perform(put("/api/node-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(nodeReport)))
            .andExpect(status().isBadRequest());

        // Validate the NodeReport in the database
        List<NodeReport> nodeReportList = nodeReportRepository.findAll();
        assertThat(nodeReportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteNodeReport() throws Exception {
        // Initialize the database
        nodeReportService.save(nodeReport);

        int databaseSizeBeforeDelete = nodeReportRepository.findAll().size();

        // Delete the nodeReport
        restNodeReportMockMvc.perform(delete("/api/node-reports/{id}", nodeReport.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<NodeReport> nodeReportList = nodeReportRepository.findAll();
        assertThat(nodeReportList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
