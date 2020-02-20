-- 2.0.0.2.sql

CREATE TABLE `history`(
`id`              BIGINT(20)    NOT NULL AUTO_INCREMENT,
`action`          VARCHAR(1000) NOT NULL,
`date`            DATETIME      NOT NULL,
`project_id`      BIGINT(20)    NULL     DEFAULT NULL,
`user_id`         BIGINT(20)    NOT NULL,
`contributor_id`  BIGINT(20)    NULL     DEFAULT NULL,
`term_lang_id`    BIGINT(20)    NULL     DEFAULT NULL,
`project_lang_id` BIGINT(20)    NULL     DEFAULT NULL,
`term_id`         BIGINT(20)    NULL     DEFAULT NULL,
`parent_id`       BIGINT(20)    NULL     DEFAULT NULL,
`is_deleted`      BIT(1)        NULL     DEFAULT 0,
`current_value`   VARCHAR(5000) NULL     DEFAULT NULL,
`new_value`       VARCHAR(5000) NULL     DEFAULT NULL,
`ref_value`       VARCHAR(5000) NULL     DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `FK_history_project_id_idx` (`project_id`),
KEY `IDX_history_action` (`action`),
KEY `FK_history_user_id_idx` (`user_id`),
KEY `FK_history_contributor_id_idx` (`contributor_id`),
KEY `FK_history_term_lang_id_idx` (`term_lang_id`),
KEY `FK_history_project_lang_id_idx` (`project_lang_id`),
KEY `FK_history_parent_id_idx` (`parent_id`),
KEY `FK_history_term_id_idx` (`term_id`),
CONSTRAINT `FK_history_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
CONSTRAINT `FK_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
CONSTRAINT `FK_history_term_id` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
CONSTRAINT `FK_history_project_contributor_id` FOREIGN KEY (`contributor_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
CONSTRAINT `FK_history_term_lang_id` FOREIGN KEY (`term_lang_id`) REFERENCES `term_lang` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
CONSTRAINT `FK_history_project_lang_id` FOREIGN KEY (`project_lang_id`) REFERENCES `project_lang` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
CONSTRAINT `FK_history_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `history` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

INSERT INTO history
SELECT a.id, a.action, a.date, a.project_id, a.user_id ,null, null, null, null, null, 0, null, null, null FROM `stats` a;

ALTER TABLE `project`
    ADD COLUMN `is_deleted` BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE `project_lang`
    ADD COLUMN `is_deleted` BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE `term`
    ADD COLUMN `is_deleted` BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE `project_contributor`
    ADD COLUMN `is_deleted` BIT(1) NOT NULL DEFAULT 0;

ALTER TABLE `term_lang`
    ADD UNIQUE INDEX `UNIQUE_term_lang_term_id` (`project_lang_id` ASC, `lang_id` ASC, `term_id` ASC);

ALTER TABLE `user`
    ADD UNIQUE INDEX `UNIQUE_user_username` (`username` ASC),
    ADD UNIQUE INDEX `UNIQUE_user_email` (`email` ASC),
    ADD COLUMN `creation_date` DATETIME NULL DEFAULT NULL;
;

UPDATE `user` SET creation_date = '2019-03-14 00:00:00';

DROP TABLE `stats`;
