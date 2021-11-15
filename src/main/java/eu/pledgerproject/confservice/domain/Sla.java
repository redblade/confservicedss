package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.time.Instant;
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
 * A Sla.
 */
@Entity
@Table(name = "sla")
public class Sla implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "creation")
    private Instant creation;

    @Column(name = "expiration")
    private Instant expiration;

    @OneToMany(mappedBy = "sla", cascade = CascadeType.REMOVE)
    private Set<SlaViolation> slaViolationSets = new HashSet<>();

    @OneToMany(mappedBy = "sla", cascade = CascadeType.REMOVE)
    private Set<Guarantee> guaranteeSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "slaSets", allowSetters = true)
    private InfrastructureProvider infrastructureProvider;

    @ManyToOne
    @JsonIgnoreProperties(value = "slaSets", allowSetters = true)
    private ServiceProvider serviceProvider;

    @ManyToOne
    @JsonIgnoreProperties(value = "slaSets", allowSetters = true)
    private Service service;

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

    public Sla name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public Sla type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getCreation() {
        return creation;
    }

    public Sla creation(Instant creation) {
        this.creation = creation;
        return this;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public Sla expiration(Instant expiration) {
        this.expiration = expiration;
        return this;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public Set<SlaViolation> getSlaViolationSets() {
        return slaViolationSets;
    }

    public Sla slaViolationSets(Set<SlaViolation> slaViolations) {
        this.slaViolationSets = slaViolations;
        return this;
    }

    public Sla addSlaViolationSet(SlaViolation slaViolation) {
        this.slaViolationSets.add(slaViolation);
        slaViolation.setSla(this);
        return this;
    }

    public Sla removeSlaViolationSet(SlaViolation slaViolation) {
        this.slaViolationSets.remove(slaViolation);
        slaViolation.setSla(null);
        return this;
    }

    public void setSlaViolationSets(Set<SlaViolation> slaViolations) {
        this.slaViolationSets = slaViolations;
    }

    public Set<Guarantee> getGuaranteeSets() {
        return guaranteeSets;
    }

    public Sla guaranteeSets(Set<Guarantee> guarantees) {
        this.guaranteeSets = guarantees;
        return this;
    }

    public Sla addGuaranteeSet(Guarantee guarantee) {
        this.guaranteeSets.add(guarantee);
        guarantee.setSla(this);
        return this;
    }

    public Sla removeGuaranteeSet(Guarantee guarantee) {
        this.guaranteeSets.remove(guarantee);
        guarantee.setSla(null);
        return this;
    }

    public void setGuaranteeSets(Set<Guarantee> guarantees) {
        this.guaranteeSets = guarantees;
    }

    public InfrastructureProvider getInfrastructureProvider() {
        return infrastructureProvider;
    }

    public Sla infrastructureProvider(InfrastructureProvider infrastructureProvider) {
        this.infrastructureProvider = infrastructureProvider;
        return this;
    }

    public void setInfrastructureProvider(InfrastructureProvider infrastructureProvider) {
        this.infrastructureProvider = infrastructureProvider;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public Sla serviceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Service getService() {
        return service;
    }

    public Sla service(Service service) {
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
        if (!(o instanceof Sla)) {
            return false;
        }
        return id != null && id.equals(((Sla) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Sla{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", creation='" + getCreation() + "'" +
            ", expiration='" + getExpiration() + "'" +
            "}";
    }
}
