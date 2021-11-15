package eu.pledgerproject.confservice.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A AppConstraint.
 */
@Entity
@Table(name = "app_constraint")
public class AppConstraint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "value")
    private String value;

    @Column(name = "value_type")
    private String valueType;

    @ManyToOne
    @JsonIgnoreProperties(value = "appConstraintSourceSets", allowSetters = true)
    private Service serviceSource;

    @ManyToOne
    @JsonIgnoreProperties(value = "appConstraintDestinationSets", allowSetters = true)
    private Service serviceDestination;

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

    public AppConstraint name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public AppConstraint category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public AppConstraint value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public AppConstraint valueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Service getServiceSource() {
        return serviceSource;
    }

    public AppConstraint serviceSource(Service service) {
        this.serviceSource = service;
        return this;
    }

    public void setServiceSource(Service service) {
        this.serviceSource = service;
    }

    public Service getServiceDestination() {
        return serviceDestination;
    }

    public AppConstraint serviceDestination(Service service) {
        this.serviceDestination = service;
        return this;
    }

    public void setServiceDestination(Service service) {
        this.serviceDestination = service;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppConstraint)) {
            return false;
        }
        return id != null && id.equals(((AppConstraint) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppConstraint{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", category='" + getCategory() + "'" +
            ", value='" + getValue() + "'" +
            ", valueType='" + getValueType() + "'" +
            "}";
    }
}
