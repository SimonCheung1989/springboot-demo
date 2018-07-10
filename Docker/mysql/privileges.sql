use profile_schema;
-- select host, user from profile_schema;
create user profile_dba identified by 'password';
grant all on profile_schema.* to profile_dba@'%' identified by 'password' with grant option;
flush privileges;