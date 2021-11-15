package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Infrastructure.
 */
@Entity
@Table(name = "infrastructure")
public class Infrastructure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "endpoint")
    private String endpoint;

    @Size(max = 2000)
    @Column(name = "credentials", length = 2000)
    private String credentials;

    @Size(max = 20000)
    @Column(name = "monitoring_plugin", length = 20000)
    private String monitoringPlugin;

    @Size(max = 2000)
    @Column(name = "properties", length = 2000)
    private String properties;

    @Size(max = 2000)
    @Column(name = "total_resources", length = 2000)
    private String totalResources;

    @OneToMany(mappedBy = "infrastructure", cascade = CascadeType.REMOVE)
    private Set<Node> nodeSets = new HashSet<>();

    @OneToMany(mappedBy = "infrastructure", cascade = CascadeType.REMOVE)
    private Set<Benchmark> benchmarkSets = new HashSet<>();

    @OneToMany(mappedBy = "infrastructure", cascade = CascadeType.REMOVE)
    private Set<InfrastructureReport> infrastructureReportSets = new HashSet<>();

    @OneToMany(mappedBy = "infrastructure", cascade = CascadeType.REMOVE)
    private Set<Project> projectSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "infrastructureSets", allowSetters = true)
    private InfrastructureProvider infrastructureProvider;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Infrastructure name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public Infrastructure type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Infrastructure endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCredentials() {
        return credentials;
    }

    public Infrastructure credentials(String credentials) {
        this.credentials = credentials;
        return this;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getMonitoringPlugin() {
        return monitoringPlugin;
    }

    public Infrastructure monitoringPlugin(String monitoringPlugin) {
        this.monitoringPlugin = monitoringPlugin;
        return this;
    }

    public void setMonitoringPlugin(String monitoringPlugin) {
        this.monitoringPlugin = monitoringPlugin;
    }

    public String getProperties() {
        return properties;
    }

    public Infrastructure properties(String properties) {
        this.properties = properties;
        return this;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getTotalResources() {
        return totalResources;
    }

    public Infrastructure totalResources(String totalResources) {
        this.totalResources = totalResources;
        return this;
    }

    public void setTotalResources(String totalResources) {
        this.totalResources = totalResources;
    }

    public Set<Node> getNodeSets() {
        return nodeSets;
    }

    public Infrastructure nodeSets(Set<Node> nodes) {
        this.nodeSets = nodes;
        return this;
    }

    public Infrastructure addNodeSet(Node node) {
        this.nodeSets.add(node);
        node.setInfrastructure(this);
        return this;
    }

    public Infrastructure removeNodeSet(Node node) {
        this.nodeSets.remove(node);
        node.setInfrastructure(null);
        return this;
    }

    public void setNodeSets(Set<Node> nodes) {
        this.nodeSets = nodes;
    }

    public Set<Benchmark> getBenchmarkSets() {
        return benchmarkSets;
    }

    public Infrastructure benchmarkSets(Set<Benchmark> benchmarks) {
        this.benchmarkSets = benchmarks;
        return this;
    }

    public Infrastructure addBenchmarkSet(Benchmark benchmark) {
        this.benchmarkSets.add(benchmark);
        benchmark.setInfrastructure(this);
        return this;
    }

    public Infrastructure removeBenchmarkSet(Benchmark benchmark) {
        this.benchmarkSets.remove(benchmark);
        benchmark.setInfrastructure(null);
        return this;
    }

    public void setBenchmarkSets(Set<Benchmark> benchmarks) {
        this.benchmarkSets = benchmarks;
    }

    public Set<InfrastructureReport> getInfrastructureReportSets() {
        return infrastructureReportSets;
    }

    public Infrastructure infrastructureReportSets(Set<InfrastructureReport> infrastructureReports) {
        this.infrastructureReportSets = infrastructureReports;
        return this;
    }

    public Infrastructure addInfrastructureReportSet(InfrastructureReport infrastructureReport) {
        this.infrastructureReportSets.add(infrastructureReport);
        infrastructureReport.setInfrastructure(this);
        return this;
    }

    public Infrastructure removeInfrastructureReportSet(InfrastructureReport infrastructureReport) {
        this.infrastructureReportSets.remove(infrastructureReport);
        infrastructureReport.setInfrastructure(null);
        return this;
    }

    public void setInfrastructureReportSets(Set<InfrastructureReport> infrastructureReports) {
        this.infrastructureReportSets = infrastructureReports;
    }

    public Set<Project> getProjectSets() {
        return projectSets;
    }

    public Infrastructure projectSets(Set<Project> projects) {
        this.projectSets = projects;
        return this;
    }

    public Infrastructure addProjectSet(Project project) {
        this.projectSets.add(project);
        project.setInfrastructure(this);
        return this;
    }

    public Infrastructure removeProjectSet(Project project) {
        this.projectSets.remove(project);
        project.setInfrastructure(null);
        return this;
    }

    public void setProjectSets(Set<Project> projects) {
        this.projectSets = projects;
    }

    public InfrastructureProvider getInfrastructureProvider() {
        return infrastructureProvider;
    }

    public Infrastructure infrastructureProvider(InfrastructureProvider infrastructureProvider) {
        this.infrastructureProvider = infrastructureProvider;
        return this;
    }

    public void setInfrastructureProvider(InfrastructureProvider infrastructureProvider) {
        this.infrastructureProvider = infrastructureProvider;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Infrastructure)) {
            return false;
        }
        return id != null && id.equals(((Infrastructure) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Infrastructure{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", endpoint='" + getEndpoint() + "'" +
            ", credentials='" + getCredentials() + "'" +
            ", monitoringPlugin='" + getMonitoringPlugin() + "'" +
            ", properties='" + getProperties() + "'" +
            ", totalResources='" + getTotalResources() + "'" +
            "}";
    }
}
