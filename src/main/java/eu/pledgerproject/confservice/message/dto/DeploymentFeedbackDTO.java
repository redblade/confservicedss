package eu.pledgerproject.confservice.message.dto;

public class DeploymentFeedbackDTO {
	
	public Long id;
	public String status;
	
	public DeploymentFeedbackDTO() {}

	public DeploymentFeedbackDTO(Long id, String status) {
		super();
		this.id = id;
		this.status = status;
	}

	@Override
	public String toString() {
		return "DeploymentFeedbackDTO [id=" + id + ", status=" + status + "]";
	}
	
}

