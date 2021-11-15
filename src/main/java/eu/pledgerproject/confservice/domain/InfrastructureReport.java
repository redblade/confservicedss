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
 * A InfrastructureReport.
 */
@Entity
@Table(name = "infrastructure_report")
public class InfrastructureReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "jhi_group")
    private String group;

    @Column(name = "category")
    private String category;

    @Column(name = "jhi_key")
    private String key;

    @Column(name = "value")
    private Double value;

    @ManyToOne
    @JsonIgnoreProperties(value = "infrastructureReportSets", allowSetters = true)
    private Infrastructure infrastructure;

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

    public InfrastructureReport timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getGroup() {
        return group;
    }

    public InfrastructureReport group(String group) {
        this.group = group;
        return this;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCategory() {
        return category;
    }

    public InfrastructureReport category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public InfrastructureReport key(String key) {
        this.key = key;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public InfrastructureReport value(Double value) {
        this.value = value;
        return this;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public InfrastructureReport infrastructure(Infrastructure infrastructure) {
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
        if (!(o instanceof InfrastructureReport)) {
            return false;
        }
        return id != null && id.equals(((InfrastructureReport) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InfrastructureReport{" +
            "id=" + getId() +
            ", timestamp='" + getTimestamp() + "'" +
            ", group='" + getGroup() + "'" +
            ", category='" + getCategory() + "'" +
            ", key='" + getKey() + "'" +
            ", value=" + getValue() +
            "}";
    }
}
