FROM eclipse-temurin:21-alpine

ENV VERTICLE_FILE backend.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY $VERTICLE_FILE $VERTICLE_HOME/

# Copy webapp
RUN mkdir -p $VERTICLE_HOME/data
COPY data $VERTICLE_HOME/data

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]
