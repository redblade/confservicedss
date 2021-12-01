package eu.pledgerproject.confservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A Guarantee.
 */
@Entity
@Table(name = "guarantee")
public class Guarantee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Size(max = 20000)
    @Column(name = "jhi_constraint", length = 20000)
    private String constraint;

    @Size(max = 200)
    @Column(name = "threshold_warning", length = 200)
    private String thresholdWarning;

    @Size(max = 200)
    @Column(name = "threshold_mild", length = 200)
    private String thresholdMild;

    @Size(max = 200)
    @Column(name = "threshold_serious", length = 200)
    private String thresholdSerious;

    @Size(max = 200)
    @Column(name = "threshold_severe", length = 200)
    private String thresholdSevere;

    @Size(max = 200)
    @Column(name = "threshold_catastrophic", length = 200)
    private String thresholdCatastrophic;

    @ManyToOne
    @JsonIgnoreProperties(value = "guaranteeSets", allowSetters = true)
    private Sla sla;

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

    public Guarantee name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConstraint() {
        return constraint;
    }

    public Guarantee constraint(String constraint) {
        this.constraint = constraint;
        return this;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getThresholdWarning() {
        return thresholdWarning;
    }

    public Guarantee thresholdWarning(String thresholdWarning) {
        this.thresholdWarning = thresholdWarning;
        return this;
    }

    public void setThresholdWarning(String thresholdWarning) {
        this.thresholdWarning = thresholdWarning;
    }

    public String getThresholdMild() {
        return thresholdMild;
    }

    public Guarantee thresholdMild(String thresholdMild) {
        this.thresholdMild = thresholdMild;
        return this;
    }

    public void setThresholdMild(String thresholdMild) {
        this.thresholdMild = thresholdMild;
    }

    public String getThresholdSerious() {
        return thresholdSerious;
    }

    public Guarantee thresholdSerious(String thresholdSerious) {
        this.thresholdSerious = thresholdSerious;
        return this;
    }

    public void setThresholdSerious(String thresholdSerious) {
        this.thresholdSerious = thresholdSerious;
    }

    public String getThresholdSevere() {
        return thresholdSevere;
    }

    public Guarantee thresholdSevere(String thresholdSevere) {
        this.thresholdSevere = thresholdSevere;
        return this;
    }

    public void setThresholdSevere(String thresholdSevere) {
        this.thresholdSevere = thresholdSevere;
    }

    public String getThresholdCatastrophic() {
        return thresholdCatastrophic;
    }

    public Guarantee thresholdCatastrophic(String thresholdCatastrophic) {
        this.thresholdCatastrophic = thresholdCatastrophic;
        return this;
    }

    public void setThresholdCatastrophic(String thresholdCatastrophic) {
        this.thresholdCatastrophic = thresholdCatastrophic;
    }

    public Sla getSla() {
        return sla;
    }

    public Guarantee sla(Sla sla) {
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
        if (!(o instanceof Guarantee)) {
            return false;
        }
        return id != null && id.equals(((Guarantee) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Guarantee{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", constraint='" + getConstraint() + "'" +
            ", thresholdWarning='" + getThresholdWarning() + "'" +
            ", thresholdMild='" + getThresholdMild() + "'" +
            ", thresholdSerious='" + getThresholdSerious() + "'" +
            ", thresholdSevere='" + getThresholdSevere() + "'" +
            ", thresholdCatastrophic='" + getThresholdCatastrophic() + "'" +
            "}";
    }
}
