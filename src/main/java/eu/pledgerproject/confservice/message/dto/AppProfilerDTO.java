package eu.pledgerproject.confservice.message.dto;

public class AppProfilerDTO {
	
	public Long service_id;
	public String benchmark_name;
	
	public AppProfilerDTO() {}

	public AppProfilerDTO(Long service_id, String benchmark_name) {
		super();
		this.service_id = service_id;
		this.benchmark_name = benchmark_name;
	}

	@Override
	public String toString() {
		return "SlaViolationDTO [service_id=" + service_id + ", benchmark_name=" + benchmark_name + "]";
	}

}

