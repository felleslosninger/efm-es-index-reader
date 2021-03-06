FROM openjdk:11.0.14-jre

RUN groupadd -o -g 1000 java \
    && useradd -o -r -m -u 1000 -g 1000 java

EXPOSE 9086

ENV APP_DIR=/opt/es-index-reader \
    JAVA_OPTS="-Xmx6G"

ADD /target/es-index-reader.jar ${APP_DIR}/es-index-reader.jar

RUN apt install -y curl
RUN chown -R java:java ${APP_DIR}
RUN chmod +x ${APP_DIR}/

WORKDIR ${APP_DIR}
USER java


ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -jar es-index-reader.jar ${0} ${@}" ]