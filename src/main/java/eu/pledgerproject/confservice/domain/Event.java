package eu.pledgerproject.confservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * A Event.
 */
@Entity
@Table(name = "event")
public class Event implements Serializable {
	public static final int MAX_DETAIL_LENGTH = 255;
	
	public static final String INFO = "info";
	public static final String WARNING = "warning";
	public static final String ERROR = "error";

    private static final long serialVersionUID = 1L;

    public Event() {
    	this.timestamp = Instant.now();
    	this.severity = INFO;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "severity")
    private String severity;

    @Column(name = "category")
    private String category;

    @Column(name = "details")
    private String details;

    @ManyToOne
    @JsonIgnoreProperties(value = "eventSets", allowSetters = true)
    private ServiceProvider serviceProvider;

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

    public Event timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public Event severity(String severity) {
        this.severity = severity;
        return this;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public Event category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetails() {
        return details;
    }

    public Event details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
    	this.details = details;
    	if(this.details != null && this.details.length() > MAX_DETAIL_LENGTH) {
    		this.details = this.details.substring(0, MAX_DETAIL_LENGTH);
    	}
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public Event serviceProvider(ServiceProvider serviceProvider) {
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
        if (!(o instanceof Event)) {
            return false;
        }
        return id != null && id.equals(((Event) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Event{" +
            "id=" + getId() +
            ", timestamp='" + getTimestamp() + "'" +
            ", severity='" + getSeverity() + "'" +
            ", category='" + getCategory() + "'" +
            ", details='" + getDetails() + "'" +
            "}";
    }
}
