-- MySQL dump 10.13  Distrib 5.5.32, for Linux (x86_64)
--
-- Host: localhost    Database: lukasync
-- ------------------------------------------------------
-- Server version	5.5.32-cll

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `registry`
--

DROP TABLE IF EXISTS `registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(64) NOT NULL,
  `service_flow_id` int(11) NOT NULL,
  `value` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_service_flow_id_index` (`key`,`service_flow_id`),
  KEY `fk_service_flow` (`service_flow_id`),
  CONSTRAINT `fk_service_flow` FOREIGN KEY (`service_flow_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registry`
--

LOCK TABLES `registry` WRITE;
/*!40000 ALTER TABLE `registry` DISABLE KEYS */;
INSERT INTO `registry` VALUES (47,'copyNewUsers.lastModified',2,'2013-11-28 15:27:06.32'),(48,'copyNewContacts.lastModified',2,'2013-11-25 16:45:14.8'),(49,'copyContactRelations.lastModified',2,'2013-11-13 10:58:49.86'),(50,'copyNewUsers.lastModified',3,'2013-11-28 15:27:06.32'),(51,'copyNewTransactions.lastModified',2,'2013-11-13 10:58:49.86'),(52,'copyUpdatedContacts.lastModified',2,'2013-11-28 14:39:00.96'),(53,'copyUpdatedUsers.lastModified',2,'2013-11-28 15:59:13.793'),(54,'copyNewSales.latestCreatedAt',4,'2013-11-29 01:31:21');
/*!40000 ALTER TABLE `registry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `type` varchar(64) NOT NULL,
  `connection_type` varchar(64) NOT NULL,
  `address` varchar(64) NOT NULL,
  `database_name` varchar(64) DEFAULT NULL,
  `username` varchar(64) NOT NULL,
  `password` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` VALUES (1,'theinner','zurmo','rest','http://theinnercirclevip.com/app/index.php',NULL,'super','citylink'),(2,'jericho','evoposhq','jdbc:sqlserver','203.143.84.21','BBSMain','sa','bbs1955'),(3,'organi','evoposhq','jdbc:sqlserver','168.144.171.93','London_HQ','sa','bbs1955%E'),(4,'organiMagento','magento','soap','hardcoded',NULL,'lukasync','');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_flow`
--

DROP TABLE IF EXISTS `service_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_flow` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `source` int(11) NOT NULL,
  `destination` int(11) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `source` (`source`),
  KEY `destination` (`destination`),
  CONSTRAINT `fk_service_flow_destination` FOREIGN KEY (`destination`) REFERENCES `service` (`id`),
  CONSTRAINT `fk_service_flow_source` FOREIGN KEY (`source`) REFERENCES `service` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_flow`
--

LOCK TABLES `service_flow` WRITE;
/*!40000 ALTER TABLE `service_flow` DISABLE KEYS */;
INSERT INTO `service_flow` VALUES (1,2,1,1),(2,3,1,1),(3,3,4,1),(4,4,3,1);
/*!40000 ALTER TABLE `service_flow` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-11-29 11:59:50
