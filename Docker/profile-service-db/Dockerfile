FROM mysql:5.7

ENV AUTO_RUN_DIR /docker-entrypoint-initdb.d
ENV WORK_PATH /tmp
ENV MYSQL_ROOT_PASSWORD 123456

COPY init.sh $AUTO_RUN_DIR
RUN chmod a+x $AUTO_RUN_DIR/init.sh

COPY init.sql $WORK_PATH

EXPOSE 3306