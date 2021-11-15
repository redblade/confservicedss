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
 * A Node.
 */
@Entity
@Table(name = "node")
public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "ipaddress")
    private String ipaddress;

    @Size(max = 2000)
    @Column(name = "properties", length = 2000)
    private String properties;

    @Size(max = 20000)
    @Column(name = "features", length = 20000)
    private String features;

    @Size(max = 2000)
    @Column(name = "total_resources", length = 2000)
    private String totalResources;

    @OneToMany(mappedBy = "node", cascade = CascadeType.REMOVE)
    private Set<BenchmarkReport> benchmarkReportSets = new HashSet<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.REMOVE)
    private Set<NodeReport> nodeReportSets = new HashSet<>();

    @OneToMany(mappedBy = "nodeDestination", cascade = CascadeType.REMOVE)
    private Set<NodeReport> nodeReportDestinationSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "nodeSets", allowSetters = true)
    private Infrastructure infrastructure;

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

    public Node name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public Node ipaddress(String ipaddress) {
        this.ipaddress = ipaddress;
        return this;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getProperties() {
        return properties;
    }

    public Node properties(String properties) {
        this.properties = properties;
        return this;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getFeatures() {
        return features;
    }

    public Node features(String features) {
        this.features = features;
        return this;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getTotalResources() {
        return totalResources;
    }

    public Node totalResources(String totalResources) {
        this.totalResources = totalResources;
        return this;
    }

    public void setTotalResources(String totalResources) {
        this.totalResources = totalResources;
    }

    public Set<BenchmarkReport> getBenchmarkReportSets() {
        return benchmarkReportSets;
    }

    public Node benchmarkReportSets(Set<BenchmarkReport> benchmarkReports) {
        this.benchmarkReportSets = benchmarkReports;
        return this;
    }

    public Node addBenchmarkReportSet(BenchmarkReport benchmarkReport) {
        this.benchmarkReportSets.add(benchmarkReport);
        benchmarkReport.setNode(this);
        return this;
    }

    public Node removeBenchmarkReportSet(BenchmarkReport benchmarkReport) {
        this.benchmarkReportSets.remove(benchmarkReport);
        benchmarkReport.setNode(null);
        return this;
    }

    public void setBenchmarkReportSets(Set<BenchmarkReport> benchmarkReports) {
        this.benchmarkReportSets = benchmarkReports;
    }

    public Set<NodeReport> getNodeReportSets() {
        return nodeReportSets;
    }

    public Node nodeReportSets(Set<NodeReport> nodeReports) {
        this.nodeReportSets = nodeReports;
        return this;
    }

    public Node addNodeReportSet(NodeReport nodeReport) {
        this.nodeReportSets.add(nodeReport);
        nodeReport.setNode(this);
        return this;
    }

    public Node removeNodeReportSet(NodeReport nodeReport) {
        this.nodeReportSets.remove(nodeReport);
        nodeReport.setNode(null);
        return this;
    }

    public void setNodeReportSets(Set<NodeReport> nodeReports) {
        this.nodeReportSets = nodeReports;
    }

    public Set<NodeReport> getNodeReportDestinationSets() {
        return nodeReportDestinationSets;
    }

    public Node nodeReportDestinationSets(Set<NodeReport> nodeReports) {
        this.nodeReportDestinationSets = nodeReports;
        return this;
    }

    public Node addNodeReportDestinationSet(NodeReport nodeReport) {
        this.nodeReportDestinationSets.add(nodeReport);
        nodeReport.setNodeDestination(this);
        return this;
    }

    public Node removeNodeReportDestinationSet(NodeReport nodeReport) {
        this.nodeReportDestinationSets.remove(nodeReport);
        nodeReport.setNodeDestination(null);
        return this;
    }

    public void setNodeReportDestinationSets(Set<NodeReport> nodeReports) {
        this.nodeReportDestinationSets = nodeReports;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public Node infrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
        return this;
    }

    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }
        return id != null && id.equals(((Node) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Node{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", ipaddress='" + getIpaddress() + "'" +
            ", properties='" + getProperties() + "'" +
            ", features='" + getFeatures() + "'" +
            ", totalResources='" + getTotalResources() + "'" +
            "}";
    }
}
