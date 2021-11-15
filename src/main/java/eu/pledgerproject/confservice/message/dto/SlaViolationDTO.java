package eu.pledgerproject.confservice.message.dto;

public class SlaViolationDTO {
	
	public Long sla_id;
	public Long service_id;
	public Long guarantee_id;
	public String datetime; //2021-01-28T16:24:00Z
	public String importance_name;
	public String description;
	
	public SlaViolationDTO() {}

	public SlaViolationDTO(Long sla_id, Long service_id, Long guarantee_id, String datetime,
			String importance_name, String description) {
		super();
		this.sla_id = sla_id;
		this.service_id = service_id;
		this.guarantee_id = guarantee_id;
		this.datetime = datetime;
		this.importance_name = importance_name;
		this.description = description;
	}

	@Override
	public String toString() {
		return "SlaViolationDTO [sla_id=" + sla_id + ", service_id=" + service_id
				+ ", guarantee_id=" + guarantee_id + ", datetime=" + datetime + ", importance_name=" + importance_name
				+ ", description=" + description + "]";
	}

	
	
	
}

