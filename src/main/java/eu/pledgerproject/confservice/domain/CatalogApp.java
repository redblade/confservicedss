package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A CatalogApp.
 */
@Entity
@Table(name = "catalog_app")
public class CatalogApp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Size(max = 40000)
    @Column(name = "app_descriptor", length = 40000)
    private String appDescriptor;

    @OneToMany(mappedBy = "catalogApp")
    private Set<App> appSets = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "catalogAppSets", allowSetters = true)
    private ServiceProvider serviceProvider;

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

    public CatalogApp name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppDescriptor() {
        return appDescriptor;
    }

    public CatalogApp appDescriptor(String appDescriptor) {
        this.appDescriptor = appDescriptor;
        return this;
    }

    public void setAppDescriptor(String appDescriptor) {
        this.appDescriptor = appDescriptor;
    }

    public Set<App> getAppSets() {
        return appSets;
    }

    public CatalogApp appSets(Set<App> apps) {
        this.appSets = apps;
        return this;
    }

    public CatalogApp addAppSet(App app) {
        this.appSets.add(app);
        app.setCatalogApp(this);
        return this;
    }

    public CatalogApp removeAppSet(App app) {
        this.appSets.remove(app);
        app.setCatalogApp(null);
        return this;
    }

    public void setAppSets(Set<App> apps) {
        this.appSets = apps;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public CatalogApp serviceProvider(ServiceProvider serviceProvider) {
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
        if (!(o instanceof CatalogApp)) {
            return false;
        }
        return id != null && id.equals(((CatalogApp) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CatalogApp{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", appDescriptor='" + getAppDescriptor() + "'" +
            "}";
    }
}
