Actor4j Cloud Demo - notes

BUILD
  mvn clean install -DskipTests "-Dmaven.javadoc.skip=true"
  cd actor4j-cloud-demo-frontend/frontend && npm install && npm run build   (-> dist/)

RUN LOCALLY
  Backend : java -jar actor4j-cloud-demo-backend-server-2.0.0-fat.jar   (port 8080, working dir with data/)
            main class io.actor4j.cloud.demo.backend.server.starter.Launcher
  Frontend: npm run dev   (port 5173, proxy /backend -> localhost:8080)

DEPLOYMENT (docker/actor4j_cloud_demo)
  backend/ : backend.jar (fat jar) + data/
  frontend/: frontend.jar (fat jar) + webapp/ (= frontend/dist)
  .env     : ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR (see .env.example)

CONFIGURATION (environment)
  ACTOR4J_CLOUD_PORT, ACTOR4J_CLOUD_CONTEXT_PATH(_FRONTEND), ACTOR4J_CLOUD_DOCKER,
  ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR, ACTOR4J_LOGGER, ACTOR4J_SYSTEM_LOGGER

KEEP IN MIND
  - versions of Vert.x dependencies (e.g. Jackson) come from the BOM io.vertx:vertx-dependencies (root pom)
  - AuthorizationServiceActor.JWT_ISSUER = "https://yourdomain/demo" (placeholder)
