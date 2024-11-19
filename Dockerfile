FROM amazoncorretto:17-alpine

ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

VOLUME /tmp
COPY /target/user-api.jar app.jar

CMD ["sh", "-c", "java -jar $JAVA_OPTS app.jar"]
