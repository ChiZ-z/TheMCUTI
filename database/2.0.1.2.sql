-- 2.0.1.2.sql

CREATE TABLE `glossary`
(
    `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `creation_date` DATETIME      NOT NULL,
    `description`   VARCHAR(5000) NULL DEFAULT NULL,
    `glossary_name` VARCHAR(1000) NOT NULL,
    `glossary_type` VARCHAR(255)  NOT NULL,
    `lang_id`       BIGINT(20)    NOT NULL,
    `author_id`     BIGINT(20)    NOT NULL,
    `parent_id`     BIGINT(20)    NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_glossary_author_id_idx` (`author_id`),
    KEY `IDX_glossary_glossary_name` (`glossary_name`),
    KEY `IDX_glossary_glossary_type` (`glossary_type`),
    KEY `FK_glossary_parent_id_idx` (`parent_id`),
    KEY `FK_glossary_lang_id_idx` (`lang_id`),
    CONSTRAINT `FK_glossary_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT `FK_glossary_lang_id` FOREIGN KEY (`lang_id`) REFERENCES `lang` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT `FK_glossary_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `glossary` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `group_item`
(
    `id`          BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `glossary_id` BIGINT(20)    NOT NULL,
    `comment`     VARCHAR(1000) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_group_glossary_id_idx` (`glossary_id`),
    CONSTRAINT `FK_group_glossary_id` FOREIGN KEY (`glossary_id`) REFERENCES `glossary` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `category`
(
    `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `glossary_id`   BIGINT(20)    NOT NULL,
    `category_name` VARCHAR(1000) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `IDX_category_category_name` (`category_name`),
    KEY `FK_category_glossary_id_idx` (`glossary_id`),
    CONSTRAINT `FK_category_glossary_id` FOREIGN KEY (`glossary_id`) REFERENCES `glossary` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `group_item_category`
(
    `group_item_id` BIGINT(20) NOT NULL,
    `category_id`   BIGINT(20) NOT NULL,
    KEY `FK_group_item_category_group_item_id_idx` (`group_item_id`),
    KEY `FK_group_item_category_category_id_idx` (`category_id`),
    CONSTRAINT `FK_group_item_category_group_item_id` FOREIGN KEY (`group_item_id`) REFERENCES `group_item` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT `FK_group_item_category_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `translation_item`
(
    `id`                     BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `group_item_id`          BIGINT(20)    NOT NULL,
    `lang_id`                BIGINT(20)    NOT NULL,
    `translation_item_value` VARCHAR(5000) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_translation_item_group_item_id_idx` (`group_item_id`),
    KEY `FK_translation_item_lang_id_idx` (`lang_id`),
    CONSTRAINT `FK_translation_item_group_item_id` FOREIGN KEY (`group_item_id`) REFERENCES `group_item` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT `FK_translation_item_lang_id` FOREIGN KEY (`lang_id`) REFERENCES `lang` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `follower`
(
    `id`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
    `glossary_id`     BIGINT(20)   NOT NULL,
    `user_id`         BIGINT(20)   NOT NULL,
    `role`            varchar(255) NOT NULL,
    `activation_code` varchar(500) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_follower_user_id_idx` (`user_id`),
    KEY `FK_follower_glossary_id_idx` (`glossary_id`),
    UNIQUE INDEX `UNIQUE_follower_user_id` (`glossary_id` ASC, `user_id` ASC),
    CONSTRAINT `FK_follower_glossary_id` FOREIGN KEY (`glossary_id`) REFERENCES `glossary` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT `FK_follower_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

ALTER TABLE `user`
    ADD COLUMN `provider` varchar(255) DEFAULT NULL,
    ADD COLUMN `provider_id` varchar(255) DEFAULT NULL;
