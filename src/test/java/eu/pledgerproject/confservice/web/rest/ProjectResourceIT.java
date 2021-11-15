package eu.pledgerproject.confservice.web.rest;

import eu.pledgerproject.confservice.ConfserviceApp;
import eu.pledgerproject.confservice.domain.Project;
import eu.pledgerproject.confservice.repository.ProjectRepository;
import eu.pledgerproject.confservice.service.ProjectService;

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
 * Integration tests for the {@link ProjectResource} REST controller.
 */
@SpringBootTest(classes = ConfserviceApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ProjectResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GROUP = "AAAAAAAAAA";
    private static final String UPDATED_GROUP = "BBBBBBBBBB";

    private static final String DEFAULT_PROPERTIES = "AAAAAAAAAA";
    private static final String UPDATED_PROPERTIES = "BBBBBBBBBB";

    private static final Integer DEFAULT_QUOTA_CPU_MILLICORE = 1;
    private static final Integer UPDATED_QUOTA_CPU_MILLICORE = 2;

    private static final Integer DEFAULT_QUOTA_MEM_MB = 1;
    private static final Integer UPDATED_QUOTA_MEM_MB = 2;

    private static final Integer DEFAULT_QUOTA_DISK_GB = 1;
    private static final Integer UPDATED_QUOTA_DISK_GB = 2;

    private static final String DEFAULT_CREDENTIALS = "AAAAAAAAAA";
    private static final String UPDATED_CREDENTIALS = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ENABLE_BENCHMARK = false;
    private static final Boolean UPDATED_ENABLE_BENCHMARK = true;

    private static final Boolean DEFAULT_PRIVATE_BENCHMARK = false;
    private static final Boolean UPDATED_PRIVATE_BENCHMARK = true;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProjectMockMvc;

    private Project project;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createEntity(EntityManager em) {
        Project project = new Project()
            .name(DEFAULT_NAME)
            .group(DEFAULT_GROUP)
            .properties(DEFAULT_PROPERTIES)
            .quotaCpuMillicore(DEFAULT_QUOTA_CPU_MILLICORE)
            .quotaMemMB(DEFAULT_QUOTA_MEM_MB)
            .quotaDiskGB(DEFAULT_QUOTA_DISK_GB)
            .credentials(DEFAULT_CREDENTIALS)
            .enableBenchmark(DEFAULT_ENABLE_BENCHMARK)
            .privateBenchmark(DEFAULT_PRIVATE_BENCHMARK);
        return project;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createUpdatedEntity(EntityManager em) {
        Project project = new Project()
            .name(UPDATED_NAME)
            .group(UPDATED_GROUP)
            .properties(UPDATED_PROPERTIES)
            .quotaCpuMillicore(UPDATED_QUOTA_CPU_MILLICORE)
            .quotaMemMB(UPDATED_QUOTA_MEM_MB)
            .quotaDiskGB(UPDATED_QUOTA_DISK_GB)
            .credentials(UPDATED_CREDENTIALS)
            .enableBenchmark(UPDATED_ENABLE_BENCHMARK)
            .privateBenchmark(UPDATED_PRIVATE_BENCHMARK);
        return project;
    }

    @BeforeEach
    public void initTest() {
        project = createEntity(em);
    }

    @Test
    @Transactional
    public void createProject() throws Exception {
        int databaseSizeBeforeCreate = projectRepository.findAll().size();
        // Create the Project
        restProjectMockMvc.perform(post("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(project)))
            .andExpect(status().isCreated());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeCreate + 1);
        Project testProject = projectList.get(projectList.size() - 1);
        assertThat(testProject.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testProject.getGroup()).isEqualTo(DEFAULT_GROUP);
        assertThat(testProject.getProperties()).isEqualTo(DEFAULT_PROPERTIES);
        assertThat(testProject.getQuotaCpuMillicore()).isEqualTo(DEFAULT_QUOTA_CPU_MILLICORE);
        assertThat(testProject.getQuotaMemMB()).isEqualTo(DEFAULT_QUOTA_MEM_MB);
        assertThat(testProject.getQuotaDiskGB()).isEqualTo(DEFAULT_QUOTA_DISK_GB);
        assertThat(testProject.getCredentials()).isEqualTo(DEFAULT_CREDENTIALS);
        assertThat(testProject.isEnableBenchmark()).isEqualTo(DEFAULT_ENABLE_BENCHMARK);
        assertThat(testProject.isPrivateBenchmark()).isEqualTo(DEFAULT_PRIVATE_BENCHMARK);
    }

    @Test
    @Transactional
    public void createProjectWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = projectRepository.findAll().size();

        // Create the Project with an existing ID
        project.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc.perform(post("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(project)))
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllProjects() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);

        // Get all the projectList
        restProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP)))
            .andExpect(jsonPath("$.[*].properties").value(hasItem(DEFAULT_PROPERTIES)))
            .andExpect(jsonPath("$.[*].quotaCpuMillicore").value(hasItem(DEFAULT_QUOTA_CPU_MILLICORE)))
            .andExpect(jsonPath("$.[*].quotaMemMB").value(hasItem(DEFAULT_QUOTA_MEM_MB)))
            .andExpect(jsonPath("$.[*].quotaDiskGB").value(hasItem(DEFAULT_QUOTA_DISK_GB)))
            .andExpect(jsonPath("$.[*].credentials").value(hasItem(DEFAULT_CREDENTIALS)))
            .andExpect(jsonPath("$.[*].enableBenchmark").value(hasItem(DEFAULT_ENABLE_BENCHMARK.booleanValue())))
            .andExpect(jsonPath("$.[*].privateBenchmark").value(hasItem(DEFAULT_PRIVATE_BENCHMARK.booleanValue())));
    }
    
    @Test
    @Transactional
    public void getProject() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);

        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{id}", project.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(project.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.group").value(DEFAULT_GROUP))
            .andExpect(jsonPath("$.properties").value(DEFAULT_PROPERTIES))
            .andExpect(jsonPath("$.quotaCpuMillicore").value(DEFAULT_QUOTA_CPU_MILLICORE))
            .andExpect(jsonPath("$.quotaMemMB").value(DEFAULT_QUOTA_MEM_MB))
            .andExpect(jsonPath("$.quotaDiskGB").value(DEFAULT_QUOTA_DISK_GB))
            .andExpect(jsonPath("$.credentials").value(DEFAULT_CREDENTIALS))
            .andExpect(jsonPath("$.enableBenchmark").value(DEFAULT_ENABLE_BENCHMARK.booleanValue()))
            .andExpect(jsonPath("$.privateBenchmark").value(DEFAULT_PRIVATE_BENCHMARK.booleanValue()));
    }
    @Test
    @Transactional
    public void getNonExistingProject() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProject() throws Exception {
        // Initialize the database
        projectService.save(project);

        int databaseSizeBeforeUpdate = projectRepository.findAll().size();

        // Update the project
        Project updatedProject = projectRepository.findById(project.getId()).get();
        // Disconnect from session so that the updates on updatedProject are not directly saved in db
        em.detach(updatedProject);
        updatedProject
            .name(UPDATED_NAME)
            .group(UPDATED_GROUP)
            .properties(UPDATED_PROPERTIES)
            .quotaCpuMillicore(UPDATED_QUOTA_CPU_MILLICORE)
            .quotaMemMB(UPDATED_QUOTA_MEM_MB)
            .quotaDiskGB(UPDATED_QUOTA_DISK_GB)
            .credentials(UPDATED_CREDENTIALS)
            .enableBenchmark(UPDATED_ENABLE_BENCHMARK)
            .privateBenchmark(UPDATED_PRIVATE_BENCHMARK);

        restProjectMockMvc.perform(put("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedProject)))
            .andExpect(status().isOk());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate);
        Project testProject = projectList.get(projectList.size() - 1);
        assertThat(testProject.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProject.getGroup()).isEqualTo(UPDATED_GROUP);
        assertThat(testProject.getProperties()).isEqualTo(UPDATED_PROPERTIES);
        assertThat(testProject.getQuotaCpuMillicore()).isEqualTo(UPDATED_QUOTA_CPU_MILLICORE);
        assertThat(testProject.getQuotaMemMB()).isEqualTo(UPDATED_QUOTA_MEM_MB);
        assertThat(testProject.getQuotaDiskGB()).isEqualTo(UPDATED_QUOTA_DISK_GB);
        assertThat(testProject.getCredentials()).isEqualTo(UPDATED_CREDENTIALS);
        assertThat(testProject.isEnableBenchmark()).isEqualTo(UPDATED_ENABLE_BENCHMARK);
        assertThat(testProject.isPrivateBenchmark()).isEqualTo(UPDATED_PRIVATE_BENCHMARK);
    }

    @Test
    @Transactional
    public void updateNonExistingProject() throws Exception {
        int databaseSizeBeforeUpdate = projectRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc.perform(put("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(project)))
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProject() throws Exception {
        // Initialize the database
        projectService.save(project);

        int databaseSizeBeforeDelete = projectRepository.findAll().size();

        // Delete the project
        restProjectMockMvc.perform(delete("/api/projects/{id}", project.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
