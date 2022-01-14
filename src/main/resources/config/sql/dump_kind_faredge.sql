use confservice;
-- MySQL dump 10.13  Distrib 8.0.23, for osx10.15 (x86_64)
--
-- Host: localhost    Database: confservice
-- ------------------------------------------------------
-- Server version	5.6.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `app`
--

LOCK TABLES `app` WRITE;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
INSERT INTO `app` (`id`, `app_descriptor`, `name`, `management_type`, `status`, `service_provider_id`, `catalog_app_id`) VALUES
(8,LOAD_FILE_YAML('yaml/app.example-app-bash1.yaml'),'example-app-bash1','MANAGED','STOPPED',2,8),
(9,LOAD_FILE_YAML('yaml/app.example-app-bash2.yaml'),'example-app-bash2','MANAGED','STOPPED',2,9),
(10,LOAD_FILE_YAML('yaml/app.example-app-bash3.yaml'),'example-app-bash3','MANAGED','STOPPED',2,10),
(11,LOAD_FILE_YAML('yaml/app.example-app-bash4.yaml'),'example-app-bash4','MANAGED','STOPPED',2,11),
(12,LOAD_FILE_YAML('yaml/app.example-app-bash5.yaml'),'example-app-bash5','MANAGED','STOPPED',2,12),
(13,LOAD_FILE_YAML('yaml/app.example-app-bash6.yaml'),'example-app-bash6','MANAGED','STOPPED',2,13);
/*!40000 ALTER TABLE `app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `benchmark`
--

LOCK TABLES `benchmark` WRITE;
/*!40000 ALTER TABLE `benchmark` DISABLE KEYS */;
/*!40000 ALTER TABLE `benchmark` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `catalog_app`
--

LOCK TABLES `catalog_app` WRITE;
/*!40000 ALTER TABLE `catalog_app` DISABLE KEYS */;
INSERT INTO `catalog_app` (`id`, `app_descriptor`, `name`, `service_provider_id`) VALUES 
(8,LOAD_FILE_YAML('yaml/app.example-app-bash1.yaml'),'example-app-bash1',NULL),
(9,LOAD_FILE_YAML('yaml/app.example-app-bash2.yaml'),'example-app-bash2',NULL),
(10,LOAD_FILE_YAML('yaml/app.example-app-bash3.yaml'),'example-app-bash3',NULL),
(11,LOAD_FILE_YAML('yaml/app.example-app-bash4.yaml'),'example-app-bash4',NULL),
(12,LOAD_FILE_YAML('yaml/app.example-app-bash5.yaml'),'example-app-bash5',NULL),
(13,LOAD_FILE_YAML('yaml/app.example-app-bash6.yaml'),'example-app-bash6',NULL);
/*!40000 ALTER TABLE `catalog_app` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `guarantee`
--

LOCK TABLES `guarantee` WRITE;
/*!40000 ALTER TABLE `guarantee` DISABLE KEYS */;
INSERT INTO `guarantee` (`id`, `name`, `jhi_constraint`, `threshold_catastrophic`, `threshold_severe`, `threshold_serious`, `threshold_mild`, `threshold_warning`, `sla_id`) VALUES 
('12', 'responseTime for example-app-bash1', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '10'),
('13', 'responseTime for example-app-bash2', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '11'),
('14', 'responseTime for example-app-bash3', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '12'),
('15', 'responseTime for example-app-bash4', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '13'),
('16', 'responseTime for example-app-bash5', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '14'),
('17', 'responseTime for example-app-bash6', 'response_time', '>30000', '>3000', '>300', '>30', '>27', '15');

/*!40000 ALTER TABLE `guarantee` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `infrastructure`
--

LOCK TABLES `infrastructure` WRITE;
/*!40000 ALTER TABLE `infrastructure` DISABLE KEYS */;
INSERT INTO `infrastructure` (`id`, `endpoint`, `name`, `properties`, `infrastructure_provider_id`, `monitoring_plugin`, `type`, `total_resources`) VALUES 
(1,'https://localhost:44441','kind cluster1','{\'infrastructure_location\': \'localhost\'}',1,'{\'kubeconfig\': \'/var/tmp/kind-kubeconfig1.yaml\',\'monitoring_type\': \'metrics-server\',\'goldpinger_endpoint\': \'http://localhost:30091\'}','K8S','{\'cpu_millicore\': \'6200\',\'memory_mb\': \'6200\'}');
/*!40000 ALTER TABLE `infrastructure` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `infrastructure_provider`
--

LOCK TABLES `infrastructure_provider` WRITE;
/*!40000 ALTER TABLE `infrastructure_provider` DISABLE KEYS */;
INSERT INTO `infrastructure_provider` (`id`, `name`, `organisation`) VALUES 
(1,'testIP','testORG');
/*!40000 ALTER TABLE `infrastructure_provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `jhi_authority`
--

LOCK TABLES `jhi_authority` WRITE;
/*!40000 ALTER TABLE `jhi_authority` DISABLE KEYS */;
INSERT INTO `jhi_authority` (`name`) VALUES 
('ROLE_ADMIN'),('ROLE_IP'),('ROLE_SP'),('ROLE_ROAPI');
/*!40000 ALTER TABLE `jhi_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `jhi_user`
--

LOCK TABLES `jhi_user` WRITE;
/*!40000 ALTER TABLE `jhi_user` DISABLE KEYS */;
INSERT INTO `jhi_user` (`id`, `login`, `password_hash`, `first_name`, `last_name`, `email`, `image_url`, `activated`, `lang_key`, `activation_key`, `reset_key`, `created_by`, `created_date`, `reset_date`, `last_modified_by`, `last_modified_date`) VALUES 
(2,'testIP' ,'$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','testIP','testORG','ip@example.org',NULL,_binary '','en',NULL,NULL,'admin','2021-02-16 09:56:34','2021-02-16 09:56:34','admin','2021-02-16 09:56:34'),
(3,'admin'  ,'$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','Administrator','Administrator','admin@localhost','',_binary '','en',NULL,NULL,'system',NULL,NULL,'system',NULL),
(5,'api'    ,'$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','api','api','api@localhost','',_binary '','en',NULL,NULL,'system',NULL,NULL,'system',NULL),
(4,'root'   ,'$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','Root','root','root@localhost','',_binary '','en',NULL,NULL,'system',NULL,NULL,'system',NULL),
(8,'testSP1','$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','testSP1','organisation 1','sp1@example.org',NULL,_binary '','en',NULL,'n0lkGVwnM1lbqbhDj4uX','admin','2021-02-16 09:56:16','2021-02-16 09:56:16','admin','2021-02-16 09:56:16');

/*!40000 ALTER TABLE `jhi_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `jhi_user_authority`
--

LOCK TABLES `jhi_user_authority` WRITE;
/*!40000 ALTER TABLE `jhi_user_authority` DISABLE KEYS */;
INSERT INTO `jhi_user_authority` (`user_id`, `authority_name`) VALUES 
(2,'ROLE_IP'),
(3,'ROLE_ADMIN'),
(4,'ROLE_IP'),
(4,'ROLE_ADMIN'),
(4,'ROLE_SP'),
(5,'ROLE_ROAPI'),
(8,'ROLE_SP');

/*!40000 ALTER TABLE `jhi_user_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `node`
--

LOCK TABLES `node` WRITE;
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
INSERT INTO `node` (`id`, `features`,`ipaddress`,`name`,`properties`,`total_resources`,`infrastructure_id`) values
('1',NULL,NULL,'cluster1-control-plane','{\'location\': \'cluster1\', \'node_type\': \'cloud\',    \'node_master\': \'true\' }','{\'cpu_millicore\': \'400\', \'memory_mb\': \'400\'}', '1'),
('2',NULL,NULL,'cluster1-worker',       '{\'location\': \'cluster1\', \'node_type\': \'cloud\',    \'node_master\': \'false\'}','{\'cpu_millicore\': \'5000\',\'memory_mb\': \'5000\'}','1'),
('4',NULL,NULL,'cluster1-worker2',      '{\'location\': \'cluster1\', \'node_type\': \'edge\',     \'node_master\': \'false\'}','{\'cpu_millicore\': \'300\', \'memory_mb\': \'300\'}', '1'),
('5',NULL,NULL,'cluster1-worker3',      '{\'location\': \'cluster1\', \'node_type\': \'edge\',     \'node_master\': \'false\'}','{\'cpu_millicore\': \'300\', \'memory_mb\': \'300\'}', '1'),
('6',NULL,NULL,'cluster1-worker4',      '{\'location\': \'cluster1\', \'node_type\': \'faredge\', \'node_master\': \'false\'}','{\'cpu_millicore\': \'300\', \'memory_mb\': \'300\'}', '1'),
('7',NULL,NULL,'cluster1-worker5',      '{\'location\': \'cluster1\', \'node_type\': \'faredge\', \'node_master\': \'false\'}','{\'cpu_millicore\': \'300\', \'memory_mb\': \'300\'}', '1');

/*!40000 ALTER TABLE `node` ENABLE KEYS */;

UNLOCK TABLES;


--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` (`id`, `credentials`, `enable_benchmark`, `jhi_group`, `name`, `private_benchmark`, `properties`, `quota_cpu_millicore`, `quota_disk_gb`, `quota_mem_mb`, `infrastructure_id`, `service_provider_id`) VALUES   
(1,null, false,'testSP1 group','testSP1 on cluster1',false,'{\'namespace\': \'testsp1\'}',6200,0,6200,1,2);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` (`id`, `priority`, `profile`, `initial_configuration`, `runtime_configuration`, `deploy_descriptor`, `deploy_type`, `name`, `status`, `app_id`) VALUES
(9,  1, 'cpu-intensive','{\"initial_memory_mb\": \"250\", \"initial_cpu_millicore\": \"250\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash1.yaml'),         'KUBERNETES', 'example-app-bash1',    'STOPPED', '8'),
(10, 1, 'cpu-intensive','{\"initial_memory_mb\": \"300\", \"initial_cpu_millicore\": \"300\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash2.yaml'),         'KUBERNETES', 'example-app-bash2',    'STOPPED', '9'),
(11, 1, 'cpu-intensive','{\"initial_memory_mb\": \"300\", \"initial_cpu_millicore\": \"300\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash3.yaml'),         'KUBERNETES', 'example-app-bash3',    'STOPPED', '10'),
(12, 1, 'cpu-intensive','{\"initial_memory_mb\": \"200\", \"initial_cpu_millicore\": \"200\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash4.yaml'),         'KUBERNETES', 'example-app-bash4',    'STOPPED', '11'),
(13, 1, 'cpu-intensive','{\"initial_memory_mb\": \"200\", \"initial_cpu_millicore\": \"200\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash5.yaml'),         'KUBERNETES', 'example-app-bash5',    'STOPPED', '12'),
(14, 1, 'cpu-intensive','{\"initial_memory_mb\": \"200\", \"initial_cpu_millicore\": \"200\", \"min_memory_mb\": \"200\", \"min_cpu_millicore\": \"200\", \"scaling\": \"vertical\"}', '',                      LOAD_FILE_YAML('yaml/service.example-app-bash6.yaml'),         'KUBERNETES', 'example-app-bash6',    'STOPPED', '13');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
UNLOCK TABLES;



--
-- Dumping data for table `service_optimisation`
--

LOCK TABLES `service_optimisation` WRITE;
/*!40000 ALTER TABLE `service_optimisation` DISABLE KEYS */;
INSERT INTO `service_optimisation` (`id`, `name`, `optimisation`, `service_id`) VALUES
('9' ,'example-app-bash1','latency_faredge','9' ),
('10','example-app-bash2','latency_faredge','10'),
('11','example-app-bash3','latency_faredge','11'),
('12','example-app-bash4','latency_faredge','12'),
('13','example-app-bash5','latency_faredge','13'),
('14','example-app-bash6','latency_faredge','14');

/*!40000 ALTER TABLE `service_optimisation` ENABLE KEYS */;
UNLOCK TABLES;



--
-- Dumping data for table `service_constraint`
--

LOCK TABLES `service_constraint` WRITE;
/*!40000 ALTER TABLE `service_constraint` DISABLE KEYS */;
INSERT INTO `confservice`.`service_constraint` (`id`, `category`,`name`,`priority`,`value`,`value_type`,`service_id`)VALUES
('5',  'rule', 'example-app-bash1 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '9'),
('6',  'rule', 'example-app-bash1 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '9'),
('7',  'rule', 'example-app-bash1 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '9'),
('8',  'rule', 'example-app-bash2 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '10'),
('9',  'rule', 'example-app-bash2 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '10'),
('10', 'rule', 'example-app-bash2 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '10'),
('11', 'rule', 'example-app-bash3 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '11'),
('12', 'rule', 'example-app-bash3 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '11'),
('13', 'rule', 'example-app-bash3 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '11'),
('14', 'rule', 'example-app-bash4 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '12'),
('15', 'rule', 'example-app-bash4 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '12'),
('16', 'rule', 'example-app-bash4 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '12'),
('17', 'rule', 'example-app-bash5 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '13'),
('18', 'rule', 'example-app-bash5 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '13'),
('19', 'rule', 'example-app-bash5 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '13'),
('20', 'rule', 'example-app-bash6 faredge',  '0', 'node_type:\'faredge\'  AND node_master:\'false\'', 'text', '14'),
('21', 'rule', 'example-app-bash6 edge',     '1', 'node_type:\'edge\'     AND node_master:\'false\'', 'text', '14'),
('22', 'rule', 'example-app-bash6 cloud',    '2', 'node_type:\'cloud\'    AND node_master:\'false\'', 'text', '14');

/*!40000 ALTER TABLE `service_constraint` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `service_provider`
--

LOCK TABLES `service_provider` WRITE;
/*!40000 ALTER TABLE `service_provider` DISABLE KEYS */;
INSERT INTO `service_provider` (`id`, `name`, `organisation`, `preferences`) VALUES 
(2,'testSP1'  ,'organisation 1','{\n  \"monitoring.steadyServices.maxResourceUsedPercentage\": 70,\n  \"monitoring.criticalServices.maxResourceBufferPercentage\": 20,\n  \"monitoring.slaViolation.periodSec\": 300,\n   \"autoscale.percentage\": 10\n}');
/*!40000 ALTER TABLE `service_provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sla`
--

LOCK TABLES `sla` WRITE;
/*!40000 ALTER TABLE `sla` DISABLE KEYS */;
INSERT INTO `sla` (`id`, `creation`, `expiration`, `name`, `type`, `service_id`, `infrastructure_provider_id`, `service_provider_id`) VALUES 
(10,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash1','active',9 ,1,2),
(11,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash2','active',10,1,2),
(12,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash3','active',11,1,2),
(13,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash4','active',12,1,2),
(14,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash5','active',13,1,2),
(15,'2021-10-09 10:09:00',NULL,'SLA for example-app-bash6','active',14,1,2);

/*!40000 ALTER TABLE `sla` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sla_violation`
--

LOCK TABLES `sla_violation` WRITE;
/*!40000 ALTER TABLE `sla_violation` DISABLE KEYS */;
/*!40000 ALTER TABLE `sla_violation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `app_constraint`
--

LOCK TABLES `app_constraint` WRITE;
/*!40000 ALTER TABLE `app_constraint` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_constraint` ENABLE KEYS */;
UNLOCK TABLES;

