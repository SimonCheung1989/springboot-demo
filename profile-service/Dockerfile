FROM centos:7
MAINTAINER Simon
ENV centos_version=7 tag=v13 container=docker
LABEL centos.version=${centos_version} \
      tag=${tag} \
      service.name="profile-service"

#RUN yum -y install mysql-server mysql
#RUN systemctl start mysqld.service
RUN yum -y install java

#CMD ["/usr/sbin/init"]
#RUN ["sh", "-c", "echo ${centos.version}"]
COPY target/profile-service-0.0.1-SNAPSHOT.jar /tmp
#RUN ["/bin/bash", "-c", "java -jar /tmp/profile-service-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=docker"]
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "/tmp/profile-service-0.0.1-SNAPSHOT.jar"]
#CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/tmp/profile-service-0.0.1-SNAPSHOT.jar"]

#CMD ["/usr/sbin/init"]
#RUN systemctl start mysqld.service
EXPOSE 8080