DROP DATABASE IF EXISTS job_portal;
CREATE DATABASE job_portal;
USE job_portal;

-- ==========================
-- USERS
-- ==========================
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `user_type` enum('ADMIN','EMPLOYER','JOB_SEEKER') NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('ACTIVE','PENDING','REJECTED','DELETED') DEFAULT 'PENDING',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`)
) ;

-- ==========================
-- ADMIN
-- ==========================
CREATE TABLE `admin` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ;

-- ==========================
-- SUBSCRIPTION MASTER
-- ==========================
CREATE TABLE `subscription` (
  `subscription_id` int NOT NULL AUTO_INCREMENT,
  `plan_type` enum('FREE','PREMIUM') NOT NULL,
  `user_type` enum('ADMIN','EMPLOYER','JOB_SEEKER') NOT NULL,
  `duration` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`subscription_id`)
) ;

-- ==========================
-- EMPLOYER
-- ==========================
CREATE TABLE `employer` (
  `employer_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  `approval_status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  `subscription_type` enum('FREE','PREMIUM') DEFAULT 'FREE',
  `premium_expiry` date DEFAULT NULL,
  `last_payment_id` int DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`employer_id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  CONSTRAINT `employer_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ;

-- ==========================
-- COMPANY PROFILE
-- ==========================
CREATE TABLE `company_profile` (
  `profile_id` int NOT NULL AUTO_INCREMENT,
  `employer_id` int NOT NULL,
  `company_name` varchar(150) DEFAULT NULL,
  `industry` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `description` text,
  `logo` varchar(255) DEFAULT NULL,
  `website` varchar(150) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `uc_employer1` (`employer_id`),
  CONSTRAINT `company_profile_ibfk_1` FOREIGN KEY (`employer_id`) REFERENCES `employer` (`employer_id`)
) ;

-- ==========================
-- JOB SEEKER
-- ==========================
CREATE TABLE `job_seeker` (
  `job_seeker_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `resume_file` varchar(255) DEFAULT NULL,
  `skills` json DEFAULT NULL,
  `subscription_type` enum('FREE','PREMIUM') DEFAULT 'FREE',
  `premium_expiry` date DEFAULT NULL,
  `last_payment_id` int DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`job_seeker_id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  CONSTRAINT `job_seeker_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ;

-- ==========================
-- JOB POST
-- ==========================
CREATE TABLE `job_post` (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `employer_id` int NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text,
  `requirements` text,
  `job_location` varchar(150) DEFAULT NULL,
  `salary` varchar(100) DEFAULT NULL,
  `job_type` enum('FULL_TIME','PART_TIME','INTERNSHIP') NOT NULL,
  `status` enum('ACTIVE','INACTIVE','DELETED') DEFAULT 'ACTIVE',
  `posted_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`job_id`),
  KEY `employer_id` (`employer_id`),
  CONSTRAINT `job_post_ibfk_1` FOREIGN KEY (`employer_id`) REFERENCES `employer` (`employer_id`)
) ;

-- ==========================
-- APPLICATION
-- ==========================
CREATE TABLE `application` (
  `application_id` int NOT NULL AUTO_INCREMENT,
  `job_id` int NOT NULL,
  `job_seeker_id` int NOT NULL,
  `cover_letter` text,
  `applied_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('APPLIED','REVIEWED','SHORTLISTED','REJECTED','SELECTED') DEFAULT 'APPLIED',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`application_id`),
  KEY `job_id` (`job_id`),
  KEY `job_seeker_id` (`job_seeker_id`),
  CONSTRAINT `application_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job_post` (`job_id`),
  CONSTRAINT `application_ibfk_2` FOREIGN KEY (`job_seeker_id`) REFERENCES `job_seeker` (`job_seeker_id`)
) ;

-- ==========================
-- SAVED JOB
-- ==========================
CREATE TABLE `saved_job` (
  `saved_id` int NOT NULL AUTO_INCREMENT,
  `job_seeker_id` int NOT NULL,
  `job_id` int NOT NULL,
  `saved_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`saved_id`),
  UNIQUE KEY `job_seeker_id` (`job_seeker_id`,`job_id`),
  KEY `job_id` (`job_id`),
  CONSTRAINT `saved_job_ibfk_1` FOREIGN KEY (`job_seeker_id`) REFERENCES `job_seeker` (`job_seeker_id`),
  CONSTRAINT `saved_job_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job_post` (`job_id`)
) ;

-- ==========================
-- SKILL MATCH
-- ==========================
CREATE TABLE `skill_match` (
  `match_id` int NOT NULL AUTO_INCREMENT,
  `job_seeker_id` int NOT NULL,
  `job_id` int NOT NULL,
  `match_percentage` double DEFAULT NULL,
  `calculated_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`match_id`),
  UNIQUE KEY `index4` (`job_seeker_id`,`job_id`),
  KEY `job_id` (`job_id`),
  CONSTRAINT `skill_match_ibfk_1` FOREIGN KEY (`job_seeker_id`) REFERENCES `job_seeker` (`job_seeker_id`),
  CONSTRAINT `skill_match_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job_post` (`job_id`)
) ;

-- ==========================
-- PAYMENTS
-- ==========================
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `subscription_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('RAZORPAY','UPI','CARD') NOT NULL,
  `order_id` varchar(100) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `status` enum('SUCCESS','FAILED','PENDING') DEFAULT 'PENDING',
  `payment_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  KEY `user_id` (`user_id`),
  KEY `subscription_id` (`subscription_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`subscription_id`) REFERENCES `subscription` (`subscription_id`)
) ;

-- ==========================
-- REPORTS
-- ==========================
CREATE TABLE `report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_type` varchar(100) DEFAULT NULL,
  `content` text,
  `generated_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `generated_by` int NOT NULL,
  PRIMARY KEY (`report_id`),
  KEY `generated_by` (`generated_by`),
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`generated_by`) REFERENCES `admin` (`admin_id`)
) ;

-- ==========================
-- ACTIVITY LOG
-- ==========================
CREATE TABLE `activity_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `activity_type` varchar(100) DEFAULT NULL,
  `activity_description` text,
  `activity_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `activity_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ;
SELECT * FROM users;
