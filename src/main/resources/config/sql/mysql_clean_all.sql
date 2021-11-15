use confservice;
UNLOCK TABLES;

SET FOREIGN_KEY_CHECKS=0;

delete from `app`;
delete from `app_constraint`;
delete from `benchmark`;
delete from `benchmark_report`;
delete from `catalog_app`;
delete from `critical_service`;
delete from `event`;
delete from `guarantee`;
delete from `infrastructure`;
delete from `infrastructure_provider`;
delete from `infrastructure_report`;
delete from `jhi_authority`;
delete from `jhi_persistent_audit_event`;
delete from `jhi_persistent_audit_evt_data`;
delete from `jhi_user`;
delete from `jhi_user_authority`;
delete from `node`;
delete from `node_report`;
delete from `project`;
delete from `service`;
delete from `service_constraint`;
delete from `service_optimisation`;
delete from `service_provider`;
delete from `service_report`;
delete from `app_constraint`;
delete from `sla`;
delete from `sla_violation`;
delete from `steady_service`;

SET FOREIGN_KEY_CHECKS=1;