FROM eclipse-temurin:21-jre-alpine

ARG USER=app
ENV HOME=/home/$USER
ENV TZ=Europe/Oslo

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
 && apk update && apk upgrade --no-cache && rm -rf /var/cache/apk/* \
 && adduser -D $USER

USER $USER
WORKDIR $HOME

COPY --chown=$USER:$USER /target/user-api.jar app.jar

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
