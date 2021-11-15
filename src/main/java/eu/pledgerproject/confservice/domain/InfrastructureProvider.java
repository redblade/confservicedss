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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A InfrastructureProvider.
 */
@Entity
@Table(name = "infrastructure_provider")
public class InfrastructureProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "organisation")
    private String organisation;

    @OneToMany(mappedBy = "infrastructureProvider", cascade = CascadeType.REMOVE)
    private Set<Sla> slaSets = new HashSet<>();

    @OneToMany(mappedBy = "infrastructureProvider", cascade = CascadeType.REMOVE)
    private Set<Infrastructure> infrastructureSets = new HashSet<>();

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

    public InfrastructureProvider name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisation() {
        return organisation;
    }

    public InfrastructureProvider organisation(String organisation) {
        this.organisation = organisation;
        return this;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public Set<Sla> getSlaSets() {
        return slaSets;
    }

    public InfrastructureProvider slaSets(Set<Sla> slas) {
        this.slaSets = slas;
        return this;
    }

    public InfrastructureProvider addSlaSet(Sla sla) {
        this.slaSets.add(sla);
        sla.setInfrastructureProvider(this);
        return this;
    }

    public InfrastructureProvider removeSlaSet(Sla sla) {
        this.slaSets.remove(sla);
        sla.setInfrastructureProvider(null);
        return this;
    }

    public void setSlaSets(Set<Sla> slas) {
        this.slaSets = slas;
    }

    public Set<Infrastructure> getInfrastructureSets() {
        return infrastructureSets;
    }

    public InfrastructureProvider infrastructureSets(Set<Infrastructure> infrastructures) {
        this.infrastructureSets = infrastructures;
        return this;
    }

    public InfrastructureProvider addInfrastructureSet(Infrastructure infrastructure) {
        this.infrastructureSets.add(infrastructure);
        infrastructure.setInfrastructureProvider(this);
        return this;
    }

    public InfrastructureProvider removeInfrastructureSet(Infrastructure infrastructure) {
        this.infrastructureSets.remove(infrastructure);
        infrastructure.setInfrastructureProvider(null);
        return this;
    }

    public void setInfrastructureSets(Set<Infrastructure> infrastructures) {
        this.infrastructureSets = infrastructures;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InfrastructureProvider)) {
            return false;
        }
        return id != null && id.equals(((InfrastructureProvider) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InfrastructureProvider{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", organisation='" + getOrganisation() + "'" +
            "}";
    }
}
