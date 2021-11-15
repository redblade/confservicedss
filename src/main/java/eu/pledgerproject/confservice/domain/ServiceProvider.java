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
import javax.validation.constraints.Size;

/**
 * A ServiceProvider.
 */
@Entity
@Table(name = "service_provider")
public class ServiceProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "organisation")
    private String organisation;

    @Size(max = 20000)
    @Column(name = "preferences", length = 20000)
    private String preferences;

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<Event> eventSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<CatalogApp> catalogAppSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<App> appSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<Project> projectSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<Sla> slaSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.REMOVE)
    private Set<Benchmark> benchmarkSets = new HashSet<>();

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

    public ServiceProvider name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisation() {
        return organisation;
    }

    public ServiceProvider organisation(String organisation) {
        this.organisation = organisation;
        return this;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getPreferences() {
        return preferences;
    }

    public ServiceProvider preferences(String preferences) {
        this.preferences = preferences;
        return this;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public Set<Event> getEventSets() {
        return eventSets;
    }

    public ServiceProvider eventSets(Set<Event> events) {
        this.eventSets = events;
        return this;
    }

    public ServiceProvider addEventSet(Event event) {
        this.eventSets.add(event);
        event.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeEventSet(Event event) {
        this.eventSets.remove(event);
        event.setServiceProvider(null);
        return this;
    }

    public void setEventSets(Set<Event> events) {
        this.eventSets = events;
    }

    public Set<CatalogApp> getCatalogAppSets() {
        return catalogAppSets;
    }

    public ServiceProvider catalogAppSets(Set<CatalogApp> catalogApps) {
        this.catalogAppSets = catalogApps;
        return this;
    }

    public ServiceProvider addCatalogAppSet(CatalogApp catalogApp) {
        this.catalogAppSets.add(catalogApp);
        catalogApp.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeCatalogAppSet(CatalogApp catalogApp) {
        this.catalogAppSets.remove(catalogApp);
        catalogApp.setServiceProvider(null);
        return this;
    }

    public void setCatalogAppSets(Set<CatalogApp> catalogApps) {
        this.catalogAppSets = catalogApps;
    }

    public Set<App> getAppSets() {
        return appSets;
    }

    public ServiceProvider appSets(Set<App> apps) {
        this.appSets = apps;
        return this;
    }

    public ServiceProvider addAppSet(App app) {
        this.appSets.add(app);
        app.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeAppSet(App app) {
        this.appSets.remove(app);
        app.setServiceProvider(null);
        return this;
    }

    public void setAppSets(Set<App> apps) {
        this.appSets = apps;
    }

    public Set<Project> getProjectSets() {
        return projectSets;
    }

    public ServiceProvider projectSets(Set<Project> projects) {
        this.projectSets = projects;
        return this;
    }

    public ServiceProvider addProjectSet(Project project) {
        this.projectSets.add(project);
        project.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeProjectSet(Project project) {
        this.projectSets.remove(project);
        project.setServiceProvider(null);
        return this;
    }

    public void setProjectSets(Set<Project> projects) {
        this.projectSets = projects;
    }

    public Set<Sla> getSlaSets() {
        return slaSets;
    }

    public ServiceProvider slaSets(Set<Sla> slas) {
        this.slaSets = slas;
        return this;
    }

    public ServiceProvider addSlaSet(Sla sla) {
        this.slaSets.add(sla);
        sla.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeSlaSet(Sla sla) {
        this.slaSets.remove(sla);
        sla.setServiceProvider(null);
        return this;
    }

    public void setSlaSets(Set<Sla> slas) {
        this.slaSets = slas;
    }

    public Set<Benchmark> getBenchmarkSets() {
        return benchmarkSets;
    }

    public ServiceProvider benchmarkSets(Set<Benchmark> benchmarks) {
        this.benchmarkSets = benchmarks;
        return this;
    }

    public ServiceProvider addBenchmarkSet(Benchmark benchmark) {
        this.benchmarkSets.add(benchmark);
        benchmark.setServiceProvider(this);
        return this;
    }

    public ServiceProvider removeBenchmarkSet(Benchmark benchmark) {
        this.benchmarkSets.remove(benchmark);
        benchmark.setServiceProvider(null);
        return this;
    }

    public void setBenchmarkSets(Set<Benchmark> benchmarks) {
        this.benchmarkSets = benchmarks;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceProvider)) {
            return false;
        }
        return id != null && id.equals(((ServiceProvider) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ServiceProvider{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", organisation='" + getOrganisation() + "'" +
            ", preferences='" + getPreferences() + "'" +
            "}";
    }
}
