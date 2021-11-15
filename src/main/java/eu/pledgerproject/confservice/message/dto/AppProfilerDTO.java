package eu.pledgerproject.confservice.message.dto;

public class AppProfilerDTO {
	
	public Long service_id;
	public Long benchmark_id;
	
	public AppProfilerDTO() {}

	public AppProfilerDTO(Long service_id, Long benchmark_id) {
		super();
		this.service_id = service_id;
		this.benchmark_id = benchmark_id;
	}

	@Override
	public String toString() {
		return "SlaViolationDTO [service_id=" + service_id + ", benchmark_id=" + benchmark_id + "]";
	}

}

