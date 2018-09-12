FROM centos
RUN yum install -y java
COPY ./target/soccer-0.0.1-SNAPSHOT.jar soccer.jar
ENTRYPOINT ["java" ,"-jar","soccer.jar"]