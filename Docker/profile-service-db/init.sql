create schema profile_schema default character set utf8 collate utf8_general_ci;

use profile_schema;

create TABLE T_PROFILE(
  ID VARCHAR(100) PRIMARY KEY,
  NAME VARCHAR(100),
  GENDER VARCHAR(100),
  AGE INT,
  LOGIN_NAME VARCHAR(100),
  PASSWORD VARCHAR(100)
);