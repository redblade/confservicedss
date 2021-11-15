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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Benchmark.
 */
@Entity
@Table(name = "benchmark")
public class Benchmark implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "benchmark", cascade = CascadeType.REMOVE)
    private Set<BenchmarkReport> benchmarkReportSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "benchmarkSets", allowSetters = true)
    private Infrastructure infrastructure;

    @ManyToOne
    @JsonIgnoreProperties(value = "benchmarkSets", allowSetters = true)
    private ServiceProvider serviceProvider;

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

    public Benchmark name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public Benchmark category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Set<BenchmarkReport> getBenchmarkReportSets() {
        return benchmarkReportSets;
    }

    public Benchmark benchmarkReportSets(Set<BenchmarkReport> benchmarkReports) {
        this.benchmarkReportSets = benchmarkReports;
        return this;
    }

    public Benchmark addBenchmarkReportSet(BenchmarkReport benchmarkReport) {
        this.benchmarkReportSets.add(benchmarkReport);
        benchmarkReport.setBenchmark(this);
        return this;
    }

    public Benchmark removeBenchmarkReportSet(BenchmarkReport benchmarkReport) {
        this.benchmarkReportSets.remove(benchmarkReport);
        benchmarkReport.setBenchmark(null);
        return this;
    }

    public void setBenchmarkReportSets(Set<BenchmarkReport> benchmarkReports) {
        this.benchmarkReportSets = benchmarkReports;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public Benchmark infrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
        return this;
    }

    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public Benchmark serviceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Benchmark)) {
            return false;
        }
        return id != null && id.equals(((Benchmark) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Benchmark{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", category='" + getCategory() + "'" +
            "}";
    }
}
