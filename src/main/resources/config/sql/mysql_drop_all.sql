use confservice;
UNLOCK TABLES;

SET FOREIGN_KEY_CHECKS=0;

drop table if exists `app`;
drop table if exists `app_constraint`;
drop table if exists `benchmark`;
drop table if exists `benchmark_report`;
drop table if exists `catalog_app`;
drop table if exists `critical_service`;
drop table if exists `event`;
drop table if exists `guarantee`;
drop table if exists `infrastructure`;
drop table if exists `infrastructure_provider`;
drop table if exists `infrastructure_report`;
drop table if exists `jhi_authority`;
drop table if exists `jhi_persistent_audit_event`;
drop table if exists `jhi_persistent_audit_evt_data`;
drop table if exists `jhi_user`;
drop table if exists `jhi_user_authority`;
drop table if exists `node`;
drop table if exists `node_report`;
drop table if exists `project`;
drop table if exists `service`;
drop table if exists `service_constraint`;
drop table if exists `service_optimisation`;
drop table if exists `service_provider`;
drop table if exists `service_report`;
drop table if exists `app_constraint`;
drop table if exists `sla`;
drop table if exists `sla_violation`;
drop table if exists `steady_service`;

SET FOREIGN_KEY_CHECKS=1;