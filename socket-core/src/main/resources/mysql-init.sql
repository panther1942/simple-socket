CREATE DATABASE db_development;
CREATE USER 'test'@'localhost'
  IDENTIFIED BY 'test';
GRANT ALL PRIVILEGES ON db_development.* TO 'test'@'localhost';
FLUSH PRIVILEGES;
USE db_development;
CREATE TABLE tb_account (
  `uuid`        VARCHAR(50) PRIMARY KEY NOT NULL DEFAULT uuid(),
  `username`    VARCHAR(50) UNIQUE KEY  NOT NULL,
  `password`    VARCHAR(255)            NOT NULL,
  `create_time` DATETIME                NOT NULL DEFAULT current_timestamp()
);
INSERT INTO tb_account (`username`, `password`)
VALUES ('admin', 'admin');
SELECT *
FROM tb_account;

DROP TABLE IF EXISTS tb_file_trans;
CREATE TABLE IF NOT EXISTS tb_file_trans (
  `uuid`        VARCHAR(255) PRIMARY KEY NOT NULL  DEFAULT UUID(),
  `filename`    VARCHAR(255)             NOT NULL,
  `filepath`    VARCHAR(255)             NOT NULL,
  `length`      LONG                     NOT NULL  DEFAULT '0',
  `sign`        VARCHAR(255)             NOT NULL,
  `algorithm`   VARCHAR(255)             NOT NULL,
  `threads`     INTEGER                  NOT NULL  DEFAULT '1',
  `sender`      VARCHAR(255)             NOT NULL,
  `receiver`    VARCHAR(255)             NOT NULL,
  `status`      INTEGER                  NOT NULL  DEFAULT '0',
  `create_time` LONG                     NOT NULL,
  `update_time` LONG                     NOT NULL
);

DROP TABLE IF EXISTS tb_file_trans_part;
CREATE TABLE IF NOT EXISTS tb_file_trans_part (
  `uuid`        VARCHAR(255) PRIMARY KEY NOT NULL  DEFAULT UUID(),
  `task_id`     VARCHAR(255)             NOT NULL,
  `filename`    VARCHAR(255)             NOT NULL,
  `length`      LONG                     NOT NULL  DEFAULT '0',
  `pos`         LONG                     NOT NULL  DEFAULT '0',
  `crc`         LONG                     NOT NULL  DEFAULT '0',
  `status`      INTEGER                  NOT NULL  DEFAULT '0',
  `create_time` LONG                     NOT NULL,
  `update_time` LONG                     NOT NULL
);
