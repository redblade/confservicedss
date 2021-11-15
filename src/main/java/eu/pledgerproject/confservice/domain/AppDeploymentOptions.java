package eu.pledgerproject.confservice.domain;


import java.io.Serializable;

/**
 * A AppDeploymentOptions.
 */
public class AppDeploymentOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String options;

    private App app;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptions() {
        return options;
    }

    public AppDeploymentOptions options(String options) {
        this.options = options;
        return this;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public App getApp() {
        return app;
    }

    public AppDeploymentOptions app(App app) {
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
        if (!(o instanceof AppDeploymentOptions)) {
            return false;
        }
        return id != null && id.equals(((AppDeploymentOptions) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppDeploymentOptions{" +
            "id=" + getId() +
            ", options='" + getOptions() + "'" +
            "}";
    }
}
