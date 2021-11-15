package eu.pledgerproject.confservice.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Project.
 */
@Entity
@Table(name = "project", uniqueConstraints=@UniqueConstraint(columnNames = {"infrastructure_id", "service_provider_id"}))
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "jhi_group")
    private String group;

    @Size(max = 20000)
    @Column(name = "properties", length = 20000)
    private String properties;

    @Column(name = "quota_cpu_millicore")
    private Integer quotaCpuMillicore;

    @Column(name = "quota_mem_mb")
    private Integer quotaMemMB;

    @Column(name = "quota_disk_gb")
    private Integer quotaDiskGB;

    @Size(max = 20000)
    @Column(name = "credentials", length = 20000)
    private String credentials;

    @Column(name = "enable_benchmark")
    private Boolean enableBenchmark;

    @Column(name = "private_benchmark")
    private Boolean privateBenchmark;

    @ManyToOne
    @JsonIgnoreProperties(value = "projectSets", allowSetters = true)
    private Infrastructure infrastructure;

    @ManyToOne
    @JsonIgnoreProperties(value = "projectSets", allowSetters = true)
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

    public Project name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public Project group(String group) {
        this.group = group;
        return this;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProperties() {
        return properties;
    }

    public Project properties(String properties) {
        this.properties = properties;
        return this;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public Integer getQuotaCpuMillicore() {
        return quotaCpuMillicore;
    }

    public Project quotaCpuMillicore(Integer quotaCpuMillicore) {
        this.quotaCpuMillicore = quotaCpuMillicore;
        return this;
    }

    public void setQuotaCpuMillicore(Integer quotaCpuMillicore) {
        this.quotaCpuMillicore = quotaCpuMillicore;
    }

    public Integer getQuotaMemMB() {
        return quotaMemMB;
    }

    public Project quotaMemMB(Integer quotaMemMB) {
        this.quotaMemMB = quotaMemMB;
        return this;
    }

    public void setQuotaMemMB(Integer quotaMemMB) {
        this.quotaMemMB = quotaMemMB;
    }

    public Integer getQuotaDiskGB() {
        return quotaDiskGB;
    }

    public Project quotaDiskGB(Integer quotaDiskGB) {
        this.quotaDiskGB = quotaDiskGB;
        return this;
    }

    public void setQuotaDiskGB(Integer quotaDiskGB) {
        this.quotaDiskGB = quotaDiskGB;
    }

    public String getCredentials() {
        return credentials;
    }

    public Project credentials(String credentials) {
        this.credentials = credentials;
        return this;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public Boolean isEnableBenchmark() {
        return enableBenchmark;
    }

    public Project enableBenchmark(Boolean enableBenchmark) {
        this.enableBenchmark = enableBenchmark;
        return this;
    }

    public void setEnableBenchmark(Boolean enableBenchmark) {
        this.enableBenchmark = enableBenchmark;
    }

    public Boolean isPrivateBenchmark() {
        return privateBenchmark;
    }

    public Project privateBenchmark(Boolean privateBenchmark) {
        this.privateBenchmark = privateBenchmark;
        return this;
    }

    public void setPrivateBenchmark(Boolean privateBenchmark) {
        this.privateBenchmark = privateBenchmark;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public Project infrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
        return this;
    }

    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public Project serviceProvider(ServiceProvider serviceProvider) {
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
        if (!(o instanceof Project)) {
            return false;
        }
        return id != null && id.equals(((Project) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Project{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", group='" + getGroup() + "'" +
            ", properties='" + getProperties() + "'" +
            ", quotaCpuMillicore=" + getQuotaCpuMillicore() +
            ", quotaMemMB=" + getQuotaMemMB() +
            ", quotaDiskGB=" + getQuotaDiskGB() +
            ", credentials='" + getCredentials() + "'" +
            ", enableBenchmark='" + isEnableBenchmark() + "'" +
            ", privateBenchmark='" + isPrivateBenchmark() + "'" +
            "}";
    }
}
