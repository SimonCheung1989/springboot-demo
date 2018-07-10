create schema profile_schema default character set utf8 collate utf8_general_ci;

use profile_schema;

create TABLE T_PROFILE(
  ID VARCHAR PRIMARY KEY,
  NAME VARCHAR,
  GENDER VARCHAR,
  AGE INT,
  LOGIN_NAME VARCHAR,
  PASSWORD VARCHAR
);