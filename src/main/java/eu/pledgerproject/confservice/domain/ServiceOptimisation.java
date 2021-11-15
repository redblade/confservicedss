package eu.pledgerproject.confservice.domain;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A ServiceOptimisation.
 */
@Entity
@Table(name = "service_optimisation")
public class ServiceOptimisation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "optimisation")
    private String optimisation;

    @Column(name = "parameters")
    private String parameters;

    @OneToOne
    @JoinColumn(unique = true)
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

    public ServiceOptimisation name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOptimisation() {
        return optimisation;
    }

    public ServiceOptimisation optimisation(String optimisation) {
        this.optimisation = optimisation;
        return this;
    }

    public void setOptimisation(String optimisation) {
        this.optimisation = optimisation;
    }

    public String getParameters() {
        return parameters;
    }

    public ServiceOptimisation parameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Service getService() {
        return service;
    }

    public ServiceOptimisation service(Service service) {
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
        if (!(o instanceof ServiceOptimisation)) {
            return false;
        }
        return id != null && id.equals(((ServiceOptimisation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ServiceOptimisation{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", optimisation='" + getOptimisation() + "'" +
            ", parameters='" + getParameters() + "'" +
            "}";
    }
}
