

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
(8,'testSP' ,'$2a$10$1QqavCm/7OxkyoTNHCW2gepetuSd.9SR3HgAg6ExEw9c6isL/d072','testSP','organisation','sp@example.org',NULL,_binary '','en',NULL,'n0lkGVwnM1lbqbhDj4uX','admin','2021-02-16 09:56:16','2021-02-16 09:56:16','admin','2021-02-16 09:56:16');

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
-- Dumping data for table `service_provider`
--

LOCK TABLES `service_provider` WRITE;
/*!40000 ALTER TABLE `service_provider` DISABLE KEYS */;
INSERT INTO `service_provider` (`id`, `name`, `organisation`, `preferences`) VALUES 
(2,'testSP'  ,'organisation','{\n  \"monitoring.steadyServices.maxResourceUsedPercentage\": 70,\n  \"monitoring.criticalServices.maxResourceBufferPercentage\": 20,\n  \"monitoring.slaViolation.periodSec\": 300,\n   \"autoscale.percentage\": 10\n}');
/*!40000 ALTER TABLE `service_provider` ENABLE KEYS */;
UNLOCK TABLES;

