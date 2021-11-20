package eu.pledgerproject.confservice.domain;

import java.io.Serializable;
import java.time.Instant;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.pledgerproject.confservice.domain.enumeration.DeployType;
import eu.pledgerproject.confservice.domain.enumeration.ExecStatus;

/**
 * A Service.
 */
@Entity
@Table(name = "service")
public class Service implements Serializable {
	public static final int DEFAULT_PRIORITY = 1;
	public static String DEFAULT_SERVICE_PROFILE = "";
	public static String DEFAULT_SERVICE_INITIAL_CONF = "{\"initial_memory_mb\": \"200\", \"initial_cpu_millicore\": \"200\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"none\"}";
	public static String DEFAULT_SERVICE_RUNTIME_CONF = "";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "profile")
    private String profile;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "initial_configuration")
    private String initialConfiguration;

    @Column(name = "runtime_configuration")
    private String runtimeConfiguration;

    @Column(name = "last_changed_status")
    private Instant lastChangedStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deploy_type")
    private DeployType deployType;

    @Size(max = 40000)
    @Column(name = "deploy_descriptor", length = 40000)
    private String deployDescriptor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExecStatus status;

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE)
    private Set<CriticalService> criticalServiceSets = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE)
    private Set<SteadyService> steadyServiceSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceSource", cascade = CascadeType.REMOVE)
    private Set<AppConstraint> appConstraintSourceSets = new HashSet<>();

    @OneToMany(mappedBy = "serviceDestination", cascade = CascadeType.REMOVE)
    private Set<AppConstraint> appConstraintDestinationSets = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE)
    private Set<ServiceReport> serviceReportSets = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE)
    private Set<ServiceConstraint> serviceConstraintSets = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.REMOVE)
    private Set<Sla> slaSets = new HashSet<>();

    @OneToOne(mappedBy = "service", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private ServiceOptimisation serviceOptimisation;

    @ManyToOne
    @JsonIgnoreProperties(value = "serviceSets", allowSetters = true)
    private App app;
    
    public Service() {
    	this.priority = DEFAULT_PRIORITY;
    }

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

    public Service name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public Service profile(String profile) {
        this.profile = profile;
        return this;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Integer getPriority() {
        return priority;
    }

    public Service priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getInitialConfiguration() {
        return initialConfiguration;
    }

    public Service initialConfiguration(String initialConfiguration) {
        this.initialConfiguration = initialConfiguration;
        return this;
    }

    public void setInitialConfiguration(String initialConfiguration) {
        this.initialConfiguration = initialConfiguration;
    }

    public String getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    public Service runtimeConfiguration(String runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
        return this;
    }

    public void setRuntimeConfiguration(String runtimeConfiguration) {
        this.runtimeConfiguration = runtimeConfiguration;
    }
    
    public Instant getLastChangedStatus() {
        return lastChangedStatus;
    }

    public void setLastChangedStatus(Instant lastChangedStatus) {
        this.lastChangedStatus = lastChangedStatus;
    }

    public DeployType getDeployType() {
        return deployType;
    }

    public Service deployType(DeployType deployType) {
        this.deployType = deployType;
        return this;
    }

    public void setDeployType(DeployType deployType) {
        this.deployType = deployType;
    }

    public String getDeployDescriptor() {
        return deployDescriptor;
    }

    public Service deployDescriptor(String deployDescriptor) {
        this.deployDescriptor = deployDescriptor;
        return this;
    }

    public void setDeployDescriptor(String deployDescriptor) {
        this.deployDescriptor = deployDescriptor;
    }

    public ExecStatus getStatus() {
        return status;
    }

    public Service status(ExecStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(ExecStatus status) {
        this.status = status;
    }

    public Set<CriticalService> getCriticalServiceSets() {
        return criticalServiceSets;
    }

    public Service criticalServiceSets(Set<CriticalService> criticalServices) {
        this.criticalServiceSets = criticalServices;
        return this;
    }

    public Service addCriticalServiceSet(CriticalService criticalService) {
        this.criticalServiceSets.add(criticalService);
        criticalService.setService(this);
        return this;
    }

    public Service removeCriticalServiceSet(CriticalService criticalService) {
        this.criticalServiceSets.remove(criticalService);
        criticalService.setService(null);
        return this;
    }

    public void setCriticalServiceSets(Set<CriticalService> criticalServices) {
        this.criticalServiceSets = criticalServices;
    }

    public Set<SteadyService> getSteadyServiceSets() {
        return steadyServiceSets;
    }

    public Service steadyServiceSets(Set<SteadyService> steadyServices) {
        this.steadyServiceSets = steadyServices;
        return this;
    }

    public Service addSteadyServiceSet(SteadyService steadyService) {
        this.steadyServiceSets.add(steadyService);
        steadyService.setService(this);
        return this;
    }

    public Service removeSteadyServiceSet(SteadyService steadyService) {
        this.steadyServiceSets.remove(steadyService);
        steadyService.setService(null);
        return this;
    }

    public void setSteadyServiceSets(Set<SteadyService> steadyServices) {
        this.steadyServiceSets = steadyServices;
    }

    public Set<AppConstraint> getAppConstraintSourceSets() {
        return appConstraintSourceSets;
    }

    public Service appConstraintSourceSets(Set<AppConstraint> appConstraints) {
        this.appConstraintSourceSets = appConstraints;
        return this;
    }

    public Service addAppConstraintSourceSet(AppConstraint appConstraint) {
        this.appConstraintSourceSets.add(appConstraint);
        appConstraint.setServiceSource(this);
        return this;
    }

    public Service removeAppConstraintSourceSet(AppConstraint appConstraint) {
        this.appConstraintSourceSets.remove(appConstraint);
        appConstraint.setServiceSource(null);
        return this;
    }

    public void setAppConstraintSourceSets(Set<AppConstraint> appConstraints) {
        this.appConstraintSourceSets = appConstraints;
    }

    public Set<AppConstraint> getAppConstraintDestinationSets() {
        return appConstraintDestinationSets;
    }

    public Service appConstraintDestinationSets(Set<AppConstraint> appConstraints) {
        this.appConstraintDestinationSets = appConstraints;
        return this;
    }

    public Service addAppConstraintDestinationSet(AppConstraint appConstraint) {
        this.appConstraintDestinationSets.add(appConstraint);
        appConstraint.setServiceDestination(this);
        return this;
    }

    public Service removeAppConstraintDestinationSet(AppConstraint appConstraint) {
        this.appConstraintDestinationSets.remove(appConstraint);
        appConstraint.setServiceDestination(null);
        return this;
    }

    public void setAppConstraintDestinationSets(Set<AppConstraint> appConstraints) {
        this.appConstraintDestinationSets = appConstraints;
    }

    public Set<ServiceReport> getServiceReportSets() {
        return serviceReportSets;
    }

    public Service serviceReportSets(Set<ServiceReport> serviceReports) {
        this.serviceReportSets = serviceReports;
        return this;
    }

    public Service addServiceReportSet(ServiceReport serviceReport) {
        this.serviceReportSets.add(serviceReport);
        serviceReport.setService(this);
        return this;
    }

    public Service removeServiceReportSet(ServiceReport serviceReport) {
        this.serviceReportSets.remove(serviceReport);
        serviceReport.setService(null);
        return this;
    }

    public void setServiceReportSets(Set<ServiceReport> serviceReports) {
        this.serviceReportSets = serviceReports;
    }

    public Set<ServiceConstraint> getServiceConstraintSets() {
        return serviceConstraintSets;
    }

    public Service serviceConstraintSets(Set<ServiceConstraint> serviceConstraints) {
        this.serviceConstraintSets = serviceConstraints;
        return this;
    }

    public Service addServiceConstraintSet(ServiceConstraint serviceConstraint) {
        this.serviceConstraintSets.add(serviceConstraint);
        serviceConstraint.setService(this);
        return this;
    }

    public Service removeServiceConstraintSet(ServiceConstraint serviceConstraint) {
        this.serviceConstraintSets.remove(serviceConstraint);
        serviceConstraint.setService(null);
        return this;
    }

    public void setServiceConstraintSets(Set<ServiceConstraint> serviceConstraints) {
        this.serviceConstraintSets = serviceConstraints;
    }

    public Set<Sla> getSlaSets() {
        return slaSets;
    }

    public Service slaSets(Set<Sla> slas) {
        this.slaSets = slas;
        return this;
    }

    public Service addSlaSet(Sla sla) {
        this.slaSets.add(sla);
        sla.setService(this);
        return this;
    }

    public Service removeSlaSet(Sla sla) {
        this.slaSets.remove(sla);
        sla.setService(null);
        return this;
    }

    public void setSlaSets(Set<Sla> slas) {
        this.slaSets = slas;
    }

    public ServiceOptimisation getServiceOptimisation() {
        return serviceOptimisation;
    }

    public Service serviceOptimisation(ServiceOptimisation serviceOptimisation) {
        this.serviceOptimisation = serviceOptimisation;
        return this;
    }

    public void setServiceOptimisation(ServiceOptimisation serviceOptimisation) {
        this.serviceOptimisation = serviceOptimisation;
    }

    public App getApp() {
        return app;
    }

    public Service app(App app) {
        this.app = app;
        return this;
    }

    public void setApp(App app) {
        this.app = app;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Service)) {
            return false;
        }
        return id != null && id.equals(((Service) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Service{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", profile='" + getProfile() + "'" +
            ", priority=" + getPriority() +
            ", initialConfiguration='" + getInitialConfiguration() + "'" +
            ", runtimeConfiguration='" + getRuntimeConfiguration() + "'" +
            ", lastChangedStatus='" + getLastChangedStatus() + "'" +
            ", deployType='" + getDeployType() + "'" +
            ", deployDescriptor='" + getDeployDescriptor() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
