package eu.pledgerproject.confservice.domain;


import java.io.Serializable;

/**
 * A OptimisationReport.
 */
public class OptimisationReport implements Serializable, Comparable<OptimisationReport> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String optimisationType;
    private String appName;
    private String serviceName;
    private Integer servicePriority;
    private Integer requestCpu;
    private Integer requestMem;
    private Integer startupTime;
    private String node;
    private String nodeCategory;
    private String optimisationScore;
    private String serviceProvider;
    

    
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOptimisationType() {
		return optimisationType;
	}

	public void setOptimisationType(String optimisationType) {
		this.optimisationType = optimisationType;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getServicePriority() {
		return servicePriority;
	}

	public void setServicePriority(Integer servicePriority) {
		this.servicePriority = servicePriority;
	}

	public Integer getRequestCpu() {
		return requestCpu;
	}

	public void setRequestCpu(Integer requestCpu) {
		this.requestCpu = requestCpu;
	}

	public Integer getRequestMem() {
		return requestMem;
	}

	public void setRequestMem(Integer requestMem) {
		this.requestMem = requestMem;
	}

	public Integer getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Integer startupTime) {
		this.startupTime = startupTime;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getNodeCategory() {
		return nodeCategory;
	}

	public void setNodeCategory(String nodeCategory) {
		this.nodeCategory = nodeCategory;
	}

	public String getOptimisationScore() {
		return optimisationScore;
	}

	public void setOptimisationScore(String optimisationScore) {
		this.optimisationScore = optimisationScore;
	}
	
	public String getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OptimisationReport)) {
            return false;
        }
        return id != null && id.equals(((OptimisationReport) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OptimisationReport{" +
            "id=" + getId() +
            "}";
    }

	@Override
	public int compareTo(OptimisationReport o) {
		if(this.optimisationScore == null && o.optimisationScore == null){
			return 0;
		}
		else if(this.optimisationScore != null && o.optimisationScore == null){
			return 1;
		}
		else if(this.optimisationScore == null && o.optimisationScore != null){
			return -1;
		}
		else {
			return (int) (Double.parseDouble(this.optimisationScore) - Double.parseDouble(o.optimisationScore));
		}

	}
}
