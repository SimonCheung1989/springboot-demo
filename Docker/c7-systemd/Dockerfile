FROM centos:7
ENV container docker

RUN yum install -y mysql-community-server

COPY setup.sh /tmp/setup.sh
RUN chmod a+x /tmp/setup.sh

#ENTRYPOINT ["sh", "-c", "/tmp/setup.sh aaa"]
#CMD ["bb"]

EXPOSE 3306