FROM centos:7

RUN yum install -y libaio-devel.x86_64 numactl.x86_64 dos2unix &&\
	yum clean all

ENV Mysql_HOME /usr/local/mysql/
ENV PATH $PATH:$Mysql_HOME/bin

ADD my.cnf /etc
ADD startup.sh /
ADD mysql-5.7.22-linux-glibc2.12-x86_64.tar.gz /usr/local

RUN mv /usr/local/mysql-5.7.22-linux-glibc2.12-x86_64 /usr/local/mysql &&\
	useradd mysql && \
	chmod 777 /startup.sh &&\
	chmod 664 /etc/my.cnf &&\
	dos2unix /startup.sh &&\
	mkdir $Mysql_HOME/data &&\
	chown mysql:mysql -R  /usr/local/mysql

EXPOSE 3306 3306
CMD ["/startup.sh"]