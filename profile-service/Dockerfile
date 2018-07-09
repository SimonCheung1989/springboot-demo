FROM centos
MAINTAINER Simon
ENV centos_version=7 tag=v13
LABEL centos.version=${centos_version} \
      tag=${tag} \
      msql="mysql"

#RUN yum -y install mysql
RUN yum -y install java
#CMD ["/usr/sbin/init"]
#RUN ["sh", "-c", "echo ${centos.version}"]
COPY target/profile-service-0.0.1-SNAPSHOT.jar /tmp
#RUN ["/bin/bash", "-c", "java -jar /tmp/profile-service-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=docker"]
#ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "/tmp/profile-service-0.0.1-SNAPSHOT.jar"]
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/tmp/profile-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080