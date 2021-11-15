package eu.pledgerproject.confservice.message.dto;

public class DeploymentFeedbackDTO {
	
	public Long id;
	public String entity;
	public String status;
	
	public DeploymentFeedbackDTO() {}

	public DeploymentFeedbackDTO(Long id, String entity, String status) {
		super();
		this.id = id;
		this.entity = entity;
		this.status = status;
	}

	@Override
	public String toString() {
		return "DeploymentFeedbackDTO [id=" + id + ", entity=" + entity + ", status=" + status + "]";
	}
	
}

