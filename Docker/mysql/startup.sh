#!/bin/sh
#初始化
mysqld --initialize --user=mysql
#--basedir=/usr/local/mysql --datadir=/usr/local/mysql/data

#启动服务
mysqld
#mysqld --user=mysql

/bin/bash

#安装，免安装版本不需要
#mysqld -install --user=mysql