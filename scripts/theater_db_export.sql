-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: theater_db
-- ------------------------------------------------------
-- Server version	8.0.26

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
-- Table structure for table `knowledge_edge`
--

DROP TABLE IF EXISTS `knowledge_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `label` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `relation_type` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `source_node_id` bigint NOT NULL,
  `target_node_id` bigint NOT NULL,
  `weight` double DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_edge`
--

LOCK TABLES `knowledge_edge` WRITE;
/*!40000 ALTER TABLE `knowledge_edge` DISABLE KEYS */;
INSERT INTO `knowledge_edge` VALUES (1,'2026-02-24 00:52:12.997603','饰 哈姆雷特','PERFORMS_IN',7,1,1),(2,'2026-02-24 00:52:13.001130','饰 李尔王','PERFORMS_IN',7,5,1),(3,'2026-02-24 00:52:13.002109','饰 常四爷','PERFORMS_IN',7,6,1),(4,'2026-02-24 00:52:13.004620','饰 奥菲利亚','PERFORMS_IN',8,1,1),(5,'2026-02-24 00:52:13.005249','饰 葛罗斯特','PERFORMS_IN',9,5,1),(6,'2026-02-24 00:52:13.006255','饰 松二爷','PERFORMS_IN',9,6,1),(7,'2026-02-24 00:52:13.007783',NULL,'HAS_TAG',1,10,1),(8,'2026-02-24 00:52:13.008806',NULL,'HAS_TAG',1,11,1),(9,'2026-02-24 00:52:13.009805',NULL,'HAS_TAG',1,13,1),(10,'2026-02-24 00:52:13.010804',NULL,'HAS_TAG',2,10,1),(11,'2026-02-24 00:52:13.012808',NULL,'HAS_TAG',2,12,1),(12,'2026-02-24 00:52:13.014312',NULL,'HAS_TAG',2,11,1),(13,'2026-02-24 00:52:13.015845',NULL,'HAS_TAG',3,14,1),(14,'2026-02-24 00:52:13.016852',NULL,'HAS_TAG',3,13,1),(15,'2026-02-24 00:52:13.018356',NULL,'HAS_TAG',3,12,1),(16,'2026-02-24 00:52:13.020368',NULL,'HAS_TAG',4,15,1),(17,'2026-02-24 00:52:13.021880',NULL,'HAS_TAG',4,12,1),(18,'2026-02-24 00:52:13.023933',NULL,'HAS_TAG',5,10,1),(19,'2026-02-24 00:52:13.025965',NULL,'HAS_TAG',5,11,1),(20,'2026-02-24 00:52:13.026943',NULL,'HAS_TAG',6,16,1),(21,'2026-02-24 00:52:13.027963',NULL,'HAS_TAG',6,13,1),(22,'2026-02-24 00:52:13.029943','「生存还是毁灭」经典独白','HAS_TERMINOLOGY',1,17,1),(23,'2026-02-24 00:52:13.030945',NULL,'HAS_TERMINOLOGY',1,18,1),(24,'2026-02-24 00:52:13.033973',NULL,'HAS_TERMINOLOGY',5,17,1),(25,'2026-02-24 00:52:13.034983','三幕话剧','HAS_TERMINOLOGY',6,21,1),(26,'2026-02-24 00:52:13.036983',NULL,'HAS_TERMINOLOGY',4,21,1),(27,'2026-02-24 00:52:13.038524','2024年演出季','PERFORMED_AT',1,23,1),(28,'2026-02-24 00:52:13.039531','驻场演出','PERFORMED_AT',6,24,1),(29,'2026-02-24 00:52:13.040539',NULL,'PERFORMED_AT',5,24,1),(30,'2026-02-24 00:52:13.040539',NULL,'PERFORMED_AT',3,23,1),(31,'2026-02-24 00:52:13.042048','同为莎翁四大悲剧','SIMILAR_TO',1,5,1),(32,'2026-02-24 00:52:13.043059','同为莎翁作品','SIMILAR_TO',1,2,1),(33,'2026-02-24 00:52:13.044056','同为莎翁作品','SIMILAR_TO',2,5,1),(34,'2026-02-24 00:52:13.044563',NULL,'WORKS_AT',7,23,1),(35,'2026-02-24 00:52:13.045610',NULL,'WORKS_AT',7,24,1),(36,'2026-02-24 00:52:13.046571',NULL,'WORKS_AT',9,24,1),(37,'2026-02-24 00:52:13.048094','舞台表演基础','RELATED_TO',19,20,1),(38,'2026-02-24 00:52:13.049101','均为角色独立发声','RELATED_TO',17,18,1),(39,'2026-02-24 00:52:13.050126','特殊场次谢幕','RELATED_TO',22,20,1);
/*!40000 ALTER TABLE `knowledge_edge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `knowledge_node`
--

DROP TABLE IF EXISTS `knowledge_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `extra_data` text COLLATE utf8mb4_general_ci,
  `name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `node_type` enum('PLAY','ACTOR','TERMINOLOGY','TAG','VENUE') COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_node`
--

LOCK TABLES `knowledge_node` WRITE;
/*!40000 ALTER TABLE `knowledge_node` DISABLE KEYS */;
INSERT INTO `knowledge_node` VALUES (1,'2026-02-24 00:52:12.916858','莎士比亚四大悲剧之一，丹麦王子复仇与人性抉择','{\"genre\":\"悲剧\",\"rating\":9.5}','哈姆雷特','PLAY'),(2,'2026-02-24 00:52:12.970381','莎士比亚经典爱情故事，两大家族下的禁忌之恋','{\"genre\":\"爱情悲剧\",\"rating\":9.3}','罗密欧与朱丽叶','PLAY'),(3,'2026-02-24 00:52:12.972370','柴可夫斯基芭蕾舞剧，王子与天鹅公主的童话','{\"genre\":\"芭蕾\",\"rating\":9.0}','天鹅湖','PLAY'),(4,'2026-02-24 00:52:12.972875','威尔第歌剧，巴黎名妓与青年贵族的爱情悲剧','{\"genre\":\"歌剧\",\"rating\":9.4}','茶花女','PLAY'),(5,'2026-02-24 00:52:12.973882','莎士比亚四大悲剧，权力、亲情与背叛','{\"genre\":\"悲剧\",\"rating\":9.1}','李尔王','PLAY'),(6,'2026-02-24 00:52:12.977404','老舍经典话剧，三幕写尽半个世纪的沧桑变迁','{\"genre\":\"话剧\",\"rating\":9.6}','茶馆','PLAY'),(7,'2026-02-24 00:52:12.979417','著名话剧演员，北京人艺','{\"awards\":[\"梅花奖\",\"文华奖\"],\"specialty\":\"话剧、莎士比亚\"}','濮存昕','ACTOR'),(8,'2026-02-24 00:52:12.980399','演员，话剧与影视','{\"awards\":[\"梅花奖\",\"金狮奖\"],\"specialty\":\"话剧、音乐剧\"}','袁泉','ACTOR'),(9,'2026-02-24 00:52:12.981437','北京人艺演员','{\"awards\":[\"梅花奖\"],\"specialty\":\"话剧、喜剧\"}','何冰','ACTOR'),(10,'2026-02-24 00:52:12.982399','英国文艺复兴时期剧作家',NULL,'莎士比亚','TAG'),(11,'2026-02-24 00:52:12.982903','以主人公的不幸结局引发观众的悲悯',NULL,'悲剧','TAG'),(12,'2026-02-24 00:52:12.983921','以爱情为主题的剧目',NULL,'爱情','TAG'),(13,'2026-02-24 00:52:12.984448','经久不衰的经典剧目',NULL,'经典','TAG'),(14,'2026-02-24 00:52:12.985467','以芭蕾舞蹈为表现形式',NULL,'芭蕾','TAG'),(15,'2026-02-24 00:52:12.987467','以歌唱和音乐为主要表现手段',NULL,'歌剧','TAG'),(16,'2026-02-24 00:52:12.987990','以对话为主要表现手段的戏剧',NULL,'话剧','TAG'),(17,'2026-02-24 00:52:12.987990','角色在舞台上独自说出内心想法，观众可闻但剧中其他角色不知',NULL,'独白','TERMINOLOGY'),(18,'2026-02-24 00:52:12.989026','角色对观众说的话，不被舞台上其他角色听到',NULL,'旁白','TERMINOLOGY'),(19,'2026-02-24 00:52:12.990009','演员在舞台上的行走方式与步法',NULL,'台步','TERMINOLOGY'),(20,'2026-02-24 00:52:12.991524','演出结束后演员向观众致谢的仪式',NULL,'谢幕','TERMINOLOGY'),(21,'2026-02-24 00:52:12.993055','演出中两幕之间的休息时间',NULL,'幕间','TERMINOLOGY'),(22,'2026-02-24 00:52:12.994061','Special Day，指特别场次（如首场、末场、生日场等）',NULL,'SD','TERMINOLOGY'),(23,'2026-02-24 00:52:12.995063','位于北京天安门广场西侧，国家级表演艺术中心','{\"city\":\"北京\",\"capacity\":5452}','国家大剧院','VENUE'),(24,'2026-02-24 00:52:12.996569','北京人民艺术剧院，中国话剧艺术殿堂','{\"city\":\"北京\",\"capacity\":1200}','北京人艺','VENUE');
/*!40000 ALTER TABLE `knowledge_node` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-21 19:32:39
