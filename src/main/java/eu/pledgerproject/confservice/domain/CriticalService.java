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
 * A CriticalService.
 */
@Entity
@Table(name = "critical_service")
public class CriticalService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp_created")
    private Instant timestampCreated;

    @Column(name = "timestamp_processed")
    private Instant timestampProcessed;

    @Column(name = "action_taken")
    private String actionTaken;

    @Column(name = "score")
    private Long score;

    @Column(name = "details")
    private String details;

    @Column(name = "monitoring_period_sec")
    private Integer monitoringPeriodSec;

    @ManyToOne
    @JsonIgnoreProperties(value = "criticalServiceSets", allowSetters = true)
    private Service service;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestampCreated() {
        return timestampCreated;
    }

    public CriticalService timestampCreated(Instant timestampCreated) {
        this.timestampCreated = timestampCreated;
        return this;
    }

    public void setTimestampCreated(Instant timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Instant getTimestampProcessed() {
        return timestampProcessed;
    }

    public CriticalService timestampProcessed(Instant timestampProcessed) {
        this.timestampProcessed = timestampProcessed;
        return this;
    }

    public void setTimestampProcessed(Instant timestampProcessed) {
        this.timestampProcessed = timestampProcessed;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public CriticalService actionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
        return this;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Long getScore() {
        return score;
    }

    public CriticalService score(Long score) {
        this.score = score;
        return this;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public String getDetails() {
        return details;
    }

    public CriticalService details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getMonitoringPeriodSec() {
        return monitoringPeriodSec;
    }

    public CriticalService monitoringPeriodSec(Integer monitoringPeriodSec) {
        this.monitoringPeriodSec = monitoringPeriodSec;
        return this;
    }

    public void setMonitoringPeriodSec(Integer monitoringPeriodSec) {
        this.monitoringPeriodSec = monitoringPeriodSec;
    }

    public Service getService() {
        return service;
    }

    public CriticalService service(Service service) {
        this.service = service;
        return this;
    }

    public void setService(Service service) {
        this.service = service;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CriticalService)) {
            return false;
        }
        return id != null && id.equals(((CriticalService) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CriticalService{" +
            "id=" + getId() +
            ", timestampCreated='" + getTimestampCreated() + "'" +
            ", timestampProcessed='" + getTimestampProcessed() + "'" +
            ", actionTaken='" + getActionTaken() + "'" +
            ", score=" + getScore() +
            ", details='" + getDetails() + "'" +
            ", monitoringPeriodSec=" + getMonitoringPeriodSec() +
            "}";
    }
}
