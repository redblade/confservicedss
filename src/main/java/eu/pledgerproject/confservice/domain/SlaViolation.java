package eu.pledgerproject.confservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;

import eu.pledgerproject.confservice.domain.enumeration.SlaViolationType;

/**
 * A SlaViolation.
 */
@Entity
@Table(name = "sla_violation")
public class SlaViolation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "violation_name")
    private String violationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_type")
    private SlaViolationType severityType;

    @Size(max = 20000)
    @Column(name = "description", length = 20000)
    private String description;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JsonIgnoreProperties(value = "slaViolationSets", allowSetters = true)
    private Sla sla;

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

    public SlaViolation timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getViolationName() {
        return violationName;
    }

    public SlaViolation violationName(String violationName) {
        this.violationName = violationName;
        return this;
    }

    public void setViolationName(String violationName) {
        this.violationName = violationName;
    }

    public SlaViolationType getSeverityType() {
        return severityType;
    }

    public SlaViolation severityType(SlaViolationType severityType) {
        this.severityType = severityType;
        return this;
    }

    public void setSeverityType(SlaViolationType severityType) {
        this.severityType = severityType;
    }

    public String getDescription() {
        return description;
    }

    public SlaViolation description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public SlaViolation status(String status) {
        this.status = status;
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Sla getSla() {
        return sla;
    }

    public SlaViolation sla(Sla sla) {
        this.sla = sla;
        return this;
    }

    public void setSla(Sla sla) {
        this.sla = sla;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SlaViolation)) {
            return false;
        }
        return id != null && id.equals(((SlaViolation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SlaViolation{" +
            "id=" + getId() +
            ", timestamp='" + getTimestamp() + "'" +
            ", violationName='" + getViolationName() + "'" +
            ", severityType='" + getSeverityType() + "'" +
            ", description='" + getDescription() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
