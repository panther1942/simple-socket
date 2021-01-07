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
SELECT * FROM tb_account;