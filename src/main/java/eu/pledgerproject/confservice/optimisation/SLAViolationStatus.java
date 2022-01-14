package eu.pledgerproject.confservice.optimisation;

public enum SLAViolationStatus {
	open,					  //just created by the Kafka consumer
	closed_suspend,			  //this means the SLA Violation is NOT resource dependent, it will suspend the elaboration of future SLA Violations on the same service for some time
	closed_skip,			  //this means the SLA Violation is resource dependent BUT it has been skipped as a SLA Violation of type suspend has been recently received
	closed_ignored,			  //this means the SLA Violation is completely ignored by the DSS
	elab_resources_needed,    //this means the SLA Violation has been verified and more resources are needed
	elab_no_action_needed,    //this means the SLA Violation has been verified and NO actions are necessary and no resources are needed
	closed_critical,		  //this means the SLA Violation is considered in future DSS decisions
	closed_not_critical,	  //this means the SLA Violation is NOT considered in future DSS decisions
	closed_just_updated,	  //this means the SLA Violation is NOT considered as the Service has been recently updated
	closed_app_stop	          //this means the SLA Violation is closed because the Service was stopped
}
