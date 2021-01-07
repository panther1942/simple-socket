DROP TABLE IF EXISTS tb_account;
CREATE TABLE IF NOT EXISTS tb_account (
  `uuid`        INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL,
  `username`    VARCHAR(50) UNIQUE                 NOT NULL,
  `password`    VARCHAR(255)                       NOT NULL,
  `create_time` INTEGER                            NOT NULL,
  `update_time` INTEGER                            NOT NULL
);
INSERT INTO tb_account (`username`, `password`)
VALUES ('admin', 'admin');
SELECT *
FROM tb_account;

DROP TABLE IF EXISTS tb_file_frans;
CREATE TABLE IF NOT EXISTS tb_file_frans (
  `uuid`        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `filename`    VARCHAR(255)                      NOT NULL,
  `filepath`    VARCHAR(255)                      NOT NULL,
  `file_length` INTEGER                           NOT NULL  DEFAULT '0',
  `file_pos`    INTEGER                           NOT NULL  DEFAULT '0',
  `crc`         INTEGER                           NOT NULL,
  `sender`      VARCHAR(255)                      NOT NULL,
  `receiver`    VARCHAR(255)                      NOT NULL,
  `create_time` INTEGER                           NOT NULL,
  `update_time` INTEGER                           NOT NULL
)