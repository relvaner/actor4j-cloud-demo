FROM eclipse-temurin:21-jre-alpine

# expects next to this Dockerfile: backend.jar (the fat jar) and data/ (products.json, currency_conversion.json)
ENV APP_HOME=/usr/verticles
# the JVM uses up to 75 % of the memory limit of the container, can be overridden in docker-compose
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# own user without root rights
RUN addgroup -S actor4j && adduser -S actor4j -G actor4j

WORKDIR $APP_HOME
COPY --chown=actor4j:actor4j backend.jar ./
COPY --chown=actor4j:actor4j data ./data
# the working directory stays writable (e.g. file-uploads of the Vert.x BodyHandler)
RUN chown actor4j:actor4j $APP_HOME

USER actor4j
EXPOSE 8080

# exec form: the JVM gets the signals directly (docker stop)
ENTRYPOINT ["java", "-jar", "backend.jar"]
