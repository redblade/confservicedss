package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A BenchmarkReport.
 */
@Entity
@Table(name = "benchmark_report")
public class BenchmarkReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private Instant time;

    @Column(name = "metric")
    private String metric;

    @Column(name = "tool")
    private String tool;

    @Column(name = "mean")
    private Double mean;

    @Column(name = "jhi_interval")
    private Integer interval;

    @Column(name = "stability_index")
    private Double stabilityIndex;

    @ManyToOne
    @JsonIgnoreProperties(value = "benchmarkReportSets", allowSetters = true)
    private Node node;

    @ManyToOne
    @JsonIgnoreProperties(value = "benchmarkReportSets", allowSetters = true)
    private Benchmark benchmark;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTime() {
        return time;
    }

    public BenchmarkReport time(Instant time) {
        this.time = time;
        return this;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getMetric() {
        return metric;
    }

    public BenchmarkReport metric(String metric) {
        this.metric = metric;
        return this;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTool() {
        return tool;
    }

    public BenchmarkReport tool(String tool) {
        this.tool = tool;
        return this;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Double getMean() {
        return mean;
    }

    public BenchmarkReport mean(Double mean) {
        this.mean = mean;
        return this;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Integer getInterval() {
        return interval;
    }

    public BenchmarkReport interval(Integer interval) {
        this.interval = interval;
        return this;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Double getStabilityIndex() {
        return stabilityIndex;
    }

    public BenchmarkReport stabilityIndex(Double stabilityIndex) {
        this.stabilityIndex = stabilityIndex;
        return this;
    }

    public void setStabilityIndex(Double stabilityIndex) {
        this.stabilityIndex = stabilityIndex;
    }

    public Node getNode() {
        return node;
    }

    public BenchmarkReport node(Node node) {
        this.node = node;
        return this;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Benchmark getBenchmark() {
        return benchmark;
    }

    public BenchmarkReport benchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
        return this;
    }

    public void setBenchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BenchmarkReport)) {
            return false;
        }
        return id != null && id.equals(((BenchmarkReport) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BenchmarkReport{" +
            "id=" + getId() +
            ", time='" + getTime() + "'" +
            ", metric='" + getMetric() + "'" +
            ", tool='" + getTool() + "'" +
            ", mean=" + getMean() +
            ", interval=" + getInterval() +
            ", stabilityIndex=" + getStabilityIndex() +
            "}";
    }
}
