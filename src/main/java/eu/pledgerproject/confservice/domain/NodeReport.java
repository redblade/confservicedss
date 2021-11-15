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
 * A NodeReport.
 */
@Entity
@Table(name = "node_report")
public class NodeReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "category")
    private String category;

    @Column(name = "jhi_key")
    private String key;

    @Column(name = "value")
    private Double value;

    @ManyToOne
    @JsonIgnoreProperties(value = "nodeReportSets", allowSetters = true)
    private Node node;

    @ManyToOne
    @JsonIgnoreProperties(value = "nodeReportDestinationSets", allowSetters = true)
    private Node nodeDestination;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public NodeReport timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public NodeReport category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public NodeReport key(String key) {
        this.key = key;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public NodeReport value(Double value) {
        this.value = value;
        return this;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Node getNode() {
        return node;
    }

    public NodeReport node(Node node) {
        this.node = node;
        return this;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNodeDestination() {
        return nodeDestination;
    }

    public NodeReport nodeDestination(Node node) {
        this.nodeDestination = node;
        return this;
    }

    public void setNodeDestination(Node node) {
        this.nodeDestination = node;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeReport)) {
            return false;
        }
        return id != null && id.equals(((NodeReport) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NodeReport{" +
            "id=" + getId() +
            ", timestamp='" + getTimestamp() + "'" +
            ", category='" + getCategory() + "'" +
            ", key='" + getKey() + "'" +
            ", value=" + getValue() +
            "}";
    }
}
