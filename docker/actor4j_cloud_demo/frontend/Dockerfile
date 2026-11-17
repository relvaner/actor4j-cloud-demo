FROM eclipse-temurin:21-jre-alpine

# expects next to this Dockerfile: frontend.jar (the fat jar) and webapp/ (the built Vue app, frontend/dist)
ENV APP_HOME=/usr/verticles
# the JVM uses up to 75 % of the memory limit of the container, can be overridden in docker-compose
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# own user without root rights
RUN addgroup -S actor4j && adduser -S actor4j -G actor4j

WORKDIR $APP_HOME
COPY --chown=actor4j:actor4j frontend.jar ./
COPY --chown=actor4j:actor4j webapp ./webapp
# the working directory stays writable (e.g. file-uploads of the Vert.x BodyHandler)
RUN chown actor4j:actor4j $APP_HOME

USER actor4j
EXPOSE 8080

# exec form: the JVM gets the signals directly (docker stop)
ENTRYPOINT ["java", "-jar", "frontend.jar"]
