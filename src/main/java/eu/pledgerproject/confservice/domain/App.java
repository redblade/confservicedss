package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;
import eu.pledgerproject.confservice.domain.enumeration.ManagementType;

/**
 * A App.
 */
@Entity
@Table(name = "app")
public class App implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "management_type")
    private ManagementType managementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExecStatus status;

    @Size(max = 40000)
    @Column(name = "app_descriptor", length = 40000)
    private String appDescriptor;

    @OneToMany(mappedBy = "app", cascade = CascadeType.REMOVE)
    private Set<Service> serviceSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "appSets", allowSetters = true)
    private ServiceProvider serviceProvider;

    @ManyToOne
    @JsonIgnoreProperties(value = "appSets", allowSetters = true)
    private CatalogApp catalogApp;

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

    public App name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ManagementType getManagementType() {
        return managementType;
    }

    public App managementType(ManagementType managementType) {
        this.managementType = managementType;
        return this;
    }

    public void setManagementType(ManagementType managementType) {
        this.managementType = managementType;
    }

    public ExecStatus getStatus() {
        return status;
    }

    public App status(ExecStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(ExecStatus status) {
        this.status = status;
    }

    public String getAppDescriptor() {
        return appDescriptor;
    }

    public App appDescriptor(String appDescriptor) {
        this.appDescriptor = appDescriptor;
        return this;
    }

    public void setAppDescriptor(String appDescriptor) {
        this.appDescriptor = appDescriptor;
    }

    public Set<Service> getServiceSets() {
        return serviceSets;
    }

    public App serviceSets(Set<Service> services) {
        this.serviceSets = services;
        return this;
    }

    public App addServiceSet(Service service) {
        this.serviceSets.add(service);
        service.setApp(this);
        return this;
    }

    public App removeServiceSet(Service service) {
        this.serviceSets.remove(service);
        service.setApp(null);
        return this;
    }

    public void setServiceSets(Set<Service> services) {
        this.serviceSets = services;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public App serviceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public CatalogApp getCatalogApp() {
        return catalogApp;
    }

    public App catalogApp(CatalogApp catalogApp) {
        this.catalogApp = catalogApp;
        return this;
    }

    public void setCatalogApp(CatalogApp catalogApp) {
        this.catalogApp = catalogApp;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof App)) {
            return false;
        }
        return id != null && id.equals(((App) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "App{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", managementType='" + getManagementType() + "'" +
            ", status='" + getStatus() + "'" +
            ", appDescriptor='" + getAppDescriptor() + "'" +
            "}";
    }
}
