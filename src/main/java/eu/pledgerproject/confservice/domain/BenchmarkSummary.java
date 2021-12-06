package eu.pledgerproject.confservice.domain;

import java.io.Serializable;

/**
 * A BenchmarkSummary.
 */
public class BenchmarkSummary implements Serializable, Comparable<BenchmarkSummary> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Double score;
    private String metric;
    
    private Node node;
    private Benchmark benchmark;
    
    public BenchmarkSummary() {
    }
    
    public BenchmarkSummary(Long id, Double score, String metric, Node node, Benchmark benchmark) {
		super();
		this.id = id;
		this.score = score;
		this.metric = metric;
		this.node = node;
		this.benchmark = benchmark;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}

	public String getMetric() {
		return metric;
	}
	public void setMetric(String metric) {
		this.metric = metric;
	}

	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}

	public Benchmark getBenchmark() {
		return benchmark;
	}
	public void setBenchmark(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BenchmarkSummary)) {
            return false;
        }
        return id != null && id.equals(((BenchmarkSummary) o).id);
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
            ", score='" + getScore() + "'" +
            ", metric='" + getMetric() + "'" +
            ", node='" + getNode() + "'" +
            ", benchmark='" + getBenchmark() + "'" +
            "}";
    }

	public int compareTo(BenchmarkSummary other) {
		if(this.benchmark.getName().equals(other.benchmark.getName())) {
			int scoreDiff = (int) (other.getScore() - this.getScore());
			if(scoreDiff == 0) {
				return this.node.getName().compareTo(other.node.getName());
			}
			else {
				return scoreDiff;
			}
		}
		else {
			return this.benchmark.getName().compareTo(other.benchmark.getName());
		}
		
	}
}
