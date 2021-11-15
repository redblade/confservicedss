package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.BenchmarkReport;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.service.BenchmarkReportService;

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
 * Integration tests for the {@link BenchmarkReportResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class BenchmarkReportResourceIT {

    private static final Instant DEFAULT_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_METRIC = "AAAAAAAAAA";
    private static final String UPDATED_METRIC = "BBBBBBBBBB";

    private static final String DEFAULT_TOOL = "AAAAAAAAAA";
    private static final String UPDATED_TOOL = "BBBBBBBBBB";

    private static final Double DEFAULT_MEAN = 1D;
    private static final Double UPDATED_MEAN = 2D;

    private static final Integer DEFAULT_INTERVAL = 1;
    private static final Integer UPDATED_INTERVAL = 2;

    private static final Double DEFAULT_STABILITY_INDEX = 1D;
    private static final Double UPDATED_STABILITY_INDEX = 2D;

    @Autowired
    private BenchmarkReportRepository benchmarkReportRepository;

    @Autowired
    private BenchmarkReportService benchmarkReportService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBenchmarkReportMockMvc;

    private BenchmarkReport benchmarkReport;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BenchmarkReport createEntity(EntityManager em) {
        BenchmarkReport benchmarkReport = new BenchmarkReport()
            .time(DEFAULT_TIME)
            .metric(DEFAULT_METRIC)
            .tool(DEFAULT_TOOL)
            .mean(DEFAULT_MEAN)
            .interval(DEFAULT_INTERVAL)
            .stabilityIndex(DEFAULT_STABILITY_INDEX);
        return benchmarkReport;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BenchmarkReport createUpdatedEntity(EntityManager em) {
        BenchmarkReport benchmarkReport = new BenchmarkReport()
            .time(UPDATED_TIME)
            .metric(UPDATED_METRIC)
            .tool(UPDATED_TOOL)
            .mean(UPDATED_MEAN)
            .interval(UPDATED_INTERVAL)
            .stabilityIndex(UPDATED_STABILITY_INDEX);
        return benchmarkReport;
    }

    @BeforeEach
    public void initTest() {
        benchmarkReport = createEntity(em);
    }

    @Test
    @Transactional
    public void createBenchmarkReport() throws Exception {
        int databaseSizeBeforeCreate = benchmarkReportRepository.findAll().size();
        // Create the BenchmarkReport
        restBenchmarkReportMockMvc.perform(post("/api/benchmark-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmarkReport)))
            .andExpect(status().isCreated());

        // Validate the BenchmarkReport in the database
        List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findAll();
        assertThat(benchmarkReportList).hasSize(databaseSizeBeforeCreate + 1);
        BenchmarkReport testBenchmarkReport = benchmarkReportList.get(benchmarkReportList.size() - 1);
        assertThat(testBenchmarkReport.getTime()).isEqualTo(DEFAULT_TIME);
        assertThat(testBenchmarkReport.getMetric()).isEqualTo(DEFAULT_METRIC);
        assertThat(testBenchmarkReport.getTool()).isEqualTo(DEFAULT_TOOL);
        assertThat(testBenchmarkReport.getMean()).isEqualTo(DEFAULT_MEAN);
        assertThat(testBenchmarkReport.getInterval()).isEqualTo(DEFAULT_INTERVAL);
        assertThat(testBenchmarkReport.getStabilityIndex()).isEqualTo(DEFAULT_STABILITY_INDEX);
    }

    @Test
    @Transactional
    public void createBenchmarkReportWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = benchmarkReportRepository.findAll().size();

        // Create the BenchmarkReport with an existing ID
        benchmarkReport.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBenchmarkReportMockMvc.perform(post("/api/benchmark-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmarkReport)))
            .andExpect(status().isBadRequest());

        // Validate the BenchmarkReport in the database
        List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findAll();
        assertThat(benchmarkReportList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllBenchmarkReports() throws Exception {
        // Initialize the database
        benchmarkReportRepository.saveAndFlush(benchmarkReport);

        // Get all the benchmarkReportList
        restBenchmarkReportMockMvc.perform(get("/api/benchmark-reports?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(benchmarkReport.getId().intValue())))
            .andExpect(jsonPath("$.[*].time").value(hasItem(DEFAULT_TIME.toString())))
            .andExpect(jsonPath("$.[*].metric").value(hasItem(DEFAULT_METRIC)))
            .andExpect(jsonPath("$.[*].tool").value(hasItem(DEFAULT_TOOL)))
            .andExpect(jsonPath("$.[*].mean").value(hasItem(DEFAULT_MEAN.doubleValue())))
            .andExpect(jsonPath("$.[*].interval").value(hasItem(DEFAULT_INTERVAL)))
            .andExpect(jsonPath("$.[*].stabilityIndex").value(hasItem(DEFAULT_STABILITY_INDEX.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getBenchmarkReport() throws Exception {
        // Initialize the database
        benchmarkReportRepository.saveAndFlush(benchmarkReport);

        // Get the benchmarkReport
        restBenchmarkReportMockMvc.perform(get("/api/benchmark-reports/{id}", benchmarkReport.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(benchmarkReport.getId().intValue()))
            .andExpect(jsonPath("$.time").value(DEFAULT_TIME.toString()))
            .andExpect(jsonPath("$.metric").value(DEFAULT_METRIC))
            .andExpect(jsonPath("$.tool").value(DEFAULT_TOOL))
            .andExpect(jsonPath("$.mean").value(DEFAULT_MEAN.doubleValue()))
            .andExpect(jsonPath("$.interval").value(DEFAULT_INTERVAL))
            .andExpect(jsonPath("$.stabilityIndex").value(DEFAULT_STABILITY_INDEX.doubleValue()));
    }
    @Test
    @Transactional
    public void getNonExistingBenchmarkReport() throws Exception {
        // Get the benchmarkReport
        restBenchmarkReportMockMvc.perform(get("/api/benchmark-reports/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBenchmarkReport() throws Exception {
        // Initialize the database
        benchmarkReportService.save(benchmarkReport);

        int databaseSizeBeforeUpdate = benchmarkReportRepository.findAll().size();

        // Update the benchmarkReport
        BenchmarkReport updatedBenchmarkReport = benchmarkReportRepository.findById(benchmarkReport.getId()).get();
        // Disconnect from session so that the updates on updatedBenchmarkReport are not directly saved in db
        em.detach(updatedBenchmarkReport);
        updatedBenchmarkReport
            .time(UPDATED_TIME)
            .metric(UPDATED_METRIC)
            .tool(UPDATED_TOOL)
            .mean(UPDATED_MEAN)
            .interval(UPDATED_INTERVAL)
            .stabilityIndex(UPDATED_STABILITY_INDEX);

        restBenchmarkReportMockMvc.perform(put("/api/benchmark-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedBenchmarkReport)))
            .andExpect(status().isOk());

        // Validate the BenchmarkReport in the database
        List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findAll();
        assertThat(benchmarkReportList).hasSize(databaseSizeBeforeUpdate);
        BenchmarkReport testBenchmarkReport = benchmarkReportList.get(benchmarkReportList.size() - 1);
        assertThat(testBenchmarkReport.getTime()).isEqualTo(UPDATED_TIME);
        assertThat(testBenchmarkReport.getMetric()).isEqualTo(UPDATED_METRIC);
        assertThat(testBenchmarkReport.getTool()).isEqualTo(UPDATED_TOOL);
        assertThat(testBenchmarkReport.getMean()).isEqualTo(UPDATED_MEAN);
        assertThat(testBenchmarkReport.getInterval()).isEqualTo(UPDATED_INTERVAL);
        assertThat(testBenchmarkReport.getStabilityIndex()).isEqualTo(UPDATED_STABILITY_INDEX);
    }

    @Test
    @Transactional
    public void updateNonExistingBenchmarkReport() throws Exception {
        int databaseSizeBeforeUpdate = benchmarkReportRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBenchmarkReportMockMvc.perform(put("/api/benchmark-reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmarkReport)))
            .andExpect(status().isBadRequest());

        // Validate the BenchmarkReport in the database
        List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findAll();
        assertThat(benchmarkReportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBenchmarkReport() throws Exception {
        // Initialize the database
        benchmarkReportService.save(benchmarkReport);

        int databaseSizeBeforeDelete = benchmarkReportRepository.findAll().size();

        // Delete the benchmarkReport
        restBenchmarkReportMockMvc.perform(delete("/api/benchmark-reports/{id}", benchmarkReport.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BenchmarkReport> benchmarkReportList = benchmarkReportRepository.findAll();
        assertThat(benchmarkReportList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
