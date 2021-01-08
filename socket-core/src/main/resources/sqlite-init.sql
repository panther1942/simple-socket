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

DROP TABLE IF EXISTS tb_file_trans;
CREATE TABLE IF NOT EXISTS tb_file_trans (
  `uuid`        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `filename`    VARCHAR(255)                      NOT NULL,
  `filepath`    VARCHAR(255)                      NOT NULL,
  `length`      INTEGER                           NOT NULL  DEFAULT '0',
  `sign`        VARCHAR(255)                      NOT NULL,
  `algorithm`   VARCHAR(255)                      NOT NULL,
  `threads`     INTEGER                           NOT NULL  DEFAULT '1',
  `sender`      VARCHAR(255)                      NOT NULL,
  `receiver`    VARCHAR(255)                      NOT NULL,
  `create_time` INTEGER                           NOT NULL,
  `update_time` INTEGER                           NOT NULL
);

DROP TABLE IF EXISTS tb_file_trans_part;
CREATE TABLE IF NOT EXISTS tb_file_trans_part (
  `uuid`        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `task_id`     INTEGER                           NOT NULL,
  `filename`    VARCHAR(255)                      NOT NULL,
  `length`      INTEGER                           NOT NULL  DEFAULT '0',
  `pos`         INTEGER                           NOT NULL  DEFAULT '0',
  `crc`         INTEGER                           NOT NULL  DEFAULT '0',
  `status`      INTEGER                           NOT NULL  DEFAULT '0',
  `create_time` INTEGER                           NOT NULL,
  `update_time` INTEGER                           NOT NULL
);
