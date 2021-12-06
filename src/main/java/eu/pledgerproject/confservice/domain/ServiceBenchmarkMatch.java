package eu.pledgerproject.confservice.domain;

import java.io.Serializable;

/**
 * A BenchmarkSummary.
 */
public class ServiceBenchmarkMatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Service service;
    private Benchmark benchmark;
    
    private String rationale;
    
    public ServiceBenchmarkMatch() {
    }
    
	public ServiceBenchmarkMatch(Long id, Service service, Benchmark benchmark, String rationale) {
		super();
		this.id = id;
		this.service = service;
		this.benchmark = benchmark;
		this.rationale = rationale;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}

	public Benchmark getBenchmark() {
		return benchmark;
	}
	public void setBenchmark(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

	public String getRationale() {
		return rationale;
	}
	public void setRationale(String rationale) {
		this.rationale = rationale;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceBenchmarkMatch)) {
            return false;
        }
        return id != null && id.equals(((ServiceBenchmarkMatch) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Benchmark{" +
            "id=" + getId() +
            ", service='" + getService().getName() + "'" +
            ", benchmark='" + getBenchmark().getName() + "'" +
            ", rationale='" + getRationale() + "'" +
            "}";
    }

}
