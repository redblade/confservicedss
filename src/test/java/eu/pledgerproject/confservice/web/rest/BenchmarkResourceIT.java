package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Benchmark;
import eu.pledgerproject.confservice.repository.BenchmarkRepository;
import eu.pledgerproject.confservice.service.BenchmarkService;

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
 * Integration tests for the {@link BenchmarkResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class BenchmarkResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    @Autowired
    private BenchmarkRepository benchmarkRepository;

    @Autowired
    private BenchmarkService benchmarkService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBenchmarkMockMvc;

    private Benchmark benchmark;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Benchmark createEntity(EntityManager em) {
        Benchmark benchmark = new Benchmark()
            .name(DEFAULT_NAME)
            .category(DEFAULT_CATEGORY);
        return benchmark;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Benchmark createUpdatedEntity(EntityManager em) {
        Benchmark benchmark = new Benchmark()
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY);
        return benchmark;
    }

    @BeforeEach
    public void initTest() {
        benchmark = createEntity(em);
    }

    @Test
    @Transactional
    public void createBenchmark() throws Exception {
        int databaseSizeBeforeCreate = benchmarkRepository.findAll().size();
        // Create the Benchmark
        restBenchmarkMockMvc.perform(post("/api/benchmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmark)))
            .andExpect(status().isCreated());

        // Validate the Benchmark in the database
        List<Benchmark> benchmarkList = benchmarkRepository.findAll();
        assertThat(benchmarkList).hasSize(databaseSizeBeforeCreate + 1);
        Benchmark testBenchmark = benchmarkList.get(benchmarkList.size() - 1);
        assertThat(testBenchmark.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBenchmark.getCategory()).isEqualTo(DEFAULT_CATEGORY);
    }

    @Test
    @Transactional
    public void createBenchmarkWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = benchmarkRepository.findAll().size();

        // Create the Benchmark with an existing ID
        benchmark.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBenchmarkMockMvc.perform(post("/api/benchmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmark)))
            .andExpect(status().isBadRequest());

        // Validate the Benchmark in the database
        List<Benchmark> benchmarkList = benchmarkRepository.findAll();
        assertThat(benchmarkList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllBenchmarks() throws Exception {
        // Initialize the database
        benchmarkRepository.saveAndFlush(benchmark);

        // Get all the benchmarkList
        restBenchmarkMockMvc.perform(get("/api/benchmarks?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(benchmark.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)));
    }
    
    @Test
    @Transactional
    public void getBenchmark() throws Exception {
        // Initialize the database
        benchmarkRepository.saveAndFlush(benchmark);

        // Get the benchmark
        restBenchmarkMockMvc.perform(get("/api/benchmarks/{id}", benchmark.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(benchmark.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY));
    }
    @Test
    @Transactional
    public void getNonExistingBenchmark() throws Exception {
        // Get the benchmark
        restBenchmarkMockMvc.perform(get("/api/benchmarks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBenchmark() throws Exception {
        // Initialize the database
        benchmarkService.save(benchmark);

        int databaseSizeBeforeUpdate = benchmarkRepository.findAll().size();

        // Update the benchmark
        Benchmark updatedBenchmark = benchmarkRepository.findById(benchmark.getId()).get();
        // Disconnect from session so that the updates on updatedBenchmark are not directly saved in db
        em.detach(updatedBenchmark);
        updatedBenchmark
            .name(UPDATED_NAME)
            .category(UPDATED_CATEGORY);

        restBenchmarkMockMvc.perform(put("/api/benchmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedBenchmark)))
            .andExpect(status().isOk());

        // Validate the Benchmark in the database
        List<Benchmark> benchmarkList = benchmarkRepository.findAll();
        assertThat(benchmarkList).hasSize(databaseSizeBeforeUpdate);
        Benchmark testBenchmark = benchmarkList.get(benchmarkList.size() - 1);
        assertThat(testBenchmark.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBenchmark.getCategory()).isEqualTo(UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void updateNonExistingBenchmark() throws Exception {
        int databaseSizeBeforeUpdate = benchmarkRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBenchmarkMockMvc.perform(put("/api/benchmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(benchmark)))
            .andExpect(status().isBadRequest());

        // Validate the Benchmark in the database
        List<Benchmark> benchmarkList = benchmarkRepository.findAll();
        assertThat(benchmarkList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBenchmark() throws Exception {
        // Initialize the database
        benchmarkService.save(benchmark);

        int databaseSizeBeforeDelete = benchmarkRepository.findAll().size();

        // Delete the benchmark
        restBenchmarkMockMvc.perform(delete("/api/benchmarks/{id}", benchmark.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Benchmark> benchmarkList = benchmarkRepository.findAll();
        assertThat(benchmarkList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
