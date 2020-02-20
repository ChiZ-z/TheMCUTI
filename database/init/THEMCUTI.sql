-- INITIALIZE DATABASE STRUCTURE

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activation_code` varchar(500) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `mailing_access` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `profile_photo` varchar(1000) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `email` varchar(40) NOT NULL,
  `refresh_token` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_user_first_name` (`first_name`),
  KEY `IDX_user_last_name` (`last_name`),
  KEY `IDX_user_username` (`username`),
  KEY `IDX_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `contacts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_type` varchar(500) NOT NULL,
  `contact_value` varchar(500) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_contacts_user_id_idx` (`user_id`),
  CONSTRAINT `FK_contacts_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `job_experience` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(500) NOT NULL,
  `position` varchar(500) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `work_place` varchar(500) NOT NULL,
  `working_period` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_job_experience_user_id_idx` (`user_id`),
  CONSTRAINT `FK_job_experience_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lang_def` varchar(500) NOT NULL,
  `lang_icon` varchar(500) NOT NULL,
  `lang_name` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_lang_lang_def` (`lang_def`),
  KEY `IDX_user_lang_name` (`lang_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_langs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `level` varchar(1000) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `lang_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_user_langs_user_id_idx` (`user_id`),
  KEY `FK_user_langs_lang_id_idx` (`lang_id`),
  CONSTRAINT `FK_user_langs_lang_id` FOREIGN KEY (`lang_id`) REFERENCES `lang` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_user_langs_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime NOT NULL,
  `description` varchar(5000) NOT NULL,
  `last_update` datetime DEFAULT NULL,
  `project_name` varchar(500) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_project_author_id_idx` (`author_id`),
  KEY `IDX_project_project_name` (`project_name`),
  CONSTRAINT `FK_project_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `project_contributor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  `role` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_project_contributor_project_id_idx` (`project_id`),
  KEY `FK_project_contributor_user_id_idx` (`user_id`),
  CONSTRAINT `FK_project_contributor_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_project_contributor_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `project_lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_default` bit(1) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `lang_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_project_lang_project_id_idx` (`project_id`),
  KEY `FK_project_lang_lang_id_idx` (`lang_id`),
  CONSTRAINT `FK_project_lang_lang_id` FOREIGN KEY (`lang_id`) REFERENCES `lang` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_project_lang_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `term` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  `term_value` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_term_project_id_idx` (`project_id`),
  KEY `IDX_term_term_value` (`term_value`),
  CONSTRAINT `FK_term_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `term_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `term_id` bigint(20) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  `text` varchar(3000) NOT NULL,
  `creation_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_term_comment_term_id_idx` (`term_id`),
  KEY `FK_term_comment_author_id_idx` (`author_id`),
  CONSTRAINT `FK_term_comment_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_term_comment_term_id` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `term_lang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified_date` datetime NOT NULL,
  `project_lang_id` bigint(20) NOT NULL,
  `status` int(11) NOT NULL,
  `value` varchar(5000) NOT NULL,
  `lang_id` bigint(20) NOT NULL,
  `modified_by` bigint(20) NOT NULL,
  `term_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_term_lang_lang_id_idx` (`lang_id`),
  KEY `FK_term_lang_term_id_idx` (`term_id`),
  KEY `FK_term_lang_modified_by_idx` (`modified_by`),
  KEY `IDX_term_lang_status` (`status`),
  KEY `FK_term_lang_project_lang_id_idx` (`project_lang_id`),
  CONSTRAINT `FK_term_lang_lang_id` FOREIGN KEY (`lang_id`) REFERENCES `lang` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_term_lang_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_term_lang_project_lang_id` FOREIGN KEY (`project_lang_id`) REFERENCES `project_lang` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_term_lang_term_id` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(1000) NOT NULL,
  `contributor` bit(1) NOT NULL,
  `date` date NOT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_stats_project_id_idx` (`project_id`),
  KEY `FK_stats_user_id_idx` (`user_id`),
  KEY `IDX_stats_action` (`action`),
  CONSTRAINT `FK_stats_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
  CONSTRAINT `FK_stats_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- INITIALIZE PREDEFINED LANGUAGES

INSERT INTO `lang` VALUES 
  (18,'SV','Sweden.png','Swedish'),
  (19,'TR','Turkey.png','Turkish'),
  (20,'UK','Ukraine.png','Ukrainian'),
  (27,'HY','Armenia.png','Armenian'),
  (32,'RO','Romania.png','Romanian'),
  (39,'JA','Japan.png','Japanese'),
  (40,'ET','Estonia.png','Estonian'),
  (55,'DE','Germany.png','German'),
  (58,'AZ','Azerbaijan.png','Azerbaijani'),
  (62,'FR','France.png','French'),
  (64,'ID','Indonesia.png','Indonesian'),
  (69,'KA','Georgia.png','Georgian'),
  (94,'ZH','China.png','Chinese'),
  (97,'CS','Czech-Republic.png','Czech'),
  (103,'SK','Slovakia.png','Slovak'),
  (105,'RU','Russia.png','Russian'),
  (106,'BE','Belarus.png','Belarusian'),
  (108,'SR','Serbia.png','Serbian'),
  (120,'PT','Portugal.png','Portuguese'),
  (124,'ES','Spain.png','Spanish'),
  (126,'TH','Thailand.png','Thai'),
  (127,'NO','Norway.png','Norwegian'),
  (135,'GA','Ireland.png','Irish'),
  (144,'GD','Scotland.png','Scottish'),
  (147,'IT','Italy.png','Italian'),
  (159,'FI','Finland.png','Finnish'),
  (161,'SQ','Albania.png','Albanian'),
  (167,'PL','Poland.png','Polish'),
  (172,'BG','Bulgaria.png','Bulgarian'),
  (173,'CY','Wales.png','Welsh'),
  (177,'HR','Croatia.png','Croatian'),
  (180,'EN','England.png','English'),
  (181,'DA','Denmark.png','Danish'),
  (184,'EL','Greece.png','Greek');
