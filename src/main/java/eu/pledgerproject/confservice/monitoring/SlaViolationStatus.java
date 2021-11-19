package eu.pledgerproject.confservice.monitoring;

public enum SlaViolationStatus {
	open,					  //just created by the Kafka consumer
	closed_suspend,			  //this means the SLA Violation is NOT resource dependent, it will suspend the elaboration of future SLA Violations on the same service for some time
	closed_skip,			  //this means the SLA Violation is resource dependent BUT it has been skipped as a SLA Violation of type suspend has been recently received
	closed_ignored,			  //this means the SLA Violation is completely ignored by the DSS
	elab_add_more_resources,  //this means the SLA Violation has been verified and more resources are needed
	elab_no_action_taken,     //this means the SLA Violation has been verified and NO actions are necessary and no resources are needed
	closed_critical,		  //this means the SLA Violation is considered in future DSS decisions
	closed_not_critical,	  //this means the SLA Violation is NOT considered in future DSS decisions
}
