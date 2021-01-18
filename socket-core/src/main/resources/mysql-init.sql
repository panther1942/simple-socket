CREATE DATABASE db_development;
CREATE USER 'test'@'localhost'
  IDENTIFIED BY 'test';
GRANT ALL PRIVILEGES ON db_development.* TO 'test'@'localhost';
FLUSH PRIVILEGES;
USE db_development;
DROP TABLE IF EXISTS tb_account;
CREATE TABLE tb_account (
  `uuid`        VARCHAR(36) PRIMARY KEY NOT NULL DEFAULT '',
  `username`    VARCHAR(50) UNIQUE KEY  NOT NULL,
  `password`    VARCHAR(255)            NOT NULL,
  `create_time` DATETIME                NOT NULL DEFAULT current_timestamp(),
  `update_time` DATETIME                NOT NULL DEFAULT current_timestamp()
);
DROP TRIGGER IF EXISTS `tri_before_account_insert`;
CREATE TRIGGER `tri_before_account_insert`
BEFORE INSERT ON `tb_account`
FOR EACH ROW
  BEGIN
    SET NEW.uuid = UUID();
  END;

INSERT INTO tb_account (`username`, `password`)
VALUES ('admin', 'admin');
SELECT *
FROM tb_account;

DROP TABLE IF EXISTS tb_file_trans;
CREATE TABLE IF NOT EXISTS tb_file_trans (
  `uuid`        VARCHAR(36) PRIMARY KEY NOT NULL  DEFAULT '',
  `filename`    VARCHAR(255)            NOT NULL,
  `filepath`    VARCHAR(255)            NOT NULL,
  `length`      INTEGER                 NOT NULL  DEFAULT '0',
  `sign`        VARCHAR(255)            NOT NULL,
  `algorithm`   VARCHAR(255)            NOT NULL,
  `threads`     INTEGER                 NOT NULL  DEFAULT '1',
  `sender`      VARCHAR(255)            NOT NULL,
  `receiver`    VARCHAR(255)            NOT NULL,
  `status`      INTEGER                 NOT NULL  DEFAULT '0',
  `create_time` DATETIME                NOT NULL,
  `update_time` DATETIME                NOT NULL
);
DROP TRIGGER IF EXISTS `tri_before_file_trans_insert`;
CREATE TRIGGER `tri_before_file_trans_insert`
BEFORE INSERT ON `tb_file_trans`
FOR EACH ROW
  BEGIN
    SET NEW.uuid = UUID();
  END;

DROP TABLE IF EXISTS tb_file_trans_part;
CREATE TABLE IF NOT EXISTS tb_file_trans_part (
  `uuid`        VARCHAR(36) PRIMARY KEY NOT NULL  DEFAULT '',
  `task_id`     VARCHAR(255)            NOT NULL,
  `filename`    VARCHAR(255)            NOT NULL,
  `length`      INTEGER                 NOT NULL  DEFAULT '0',
  `pos`         INTEGER                 NOT NULL  DEFAULT '0',
  `crc`         INTEGER                 NOT NULL  DEFAULT '0',
  `status`      INTEGER                 NOT NULL  DEFAULT '0',
  `create_time` DATETIME                NOT NULL,
  `update_time` DATETIME                NOT NULL
);
DROP TRIGGER IF EXISTS `tri_before_file_trans_part_insert`;
CREATE TRIGGER `tri_before_file_trans_part_insert`
BEFORE INSERT ON `tb_file_trans_part`
FOR EACH ROW
  BEGIN
    SET NEW.uuid = UUID();
  END;
