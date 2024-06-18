Main class:
io.vertx.core.Launcher
Arguments
run io.actor4j.cloud.demo.backend.starter.MainVerticle
run io.actor4j.cloud.demo.frontend.starter.MainVerticle


clean install -DskipTests "-Dmaven.javadoc.skip=true"



Update always in align with: https://github.com/vert-x3/vertx-dependencies
@See POM.XML: actor4j-cloud-demo-backend
	<dependency>
		<groupId>com.fasterxml.jackson.core</groupId>
		<artifactId>jackson-databind</artifactId>
		<version>2.13.3</version>
	</dependency>
	
	
@See POM.XML: actor4j-cloud-demo-backend
class AuthorizationServiceActor
	public static final String JWT_ISSUER    = "https://yourdomain/demo";
	
	
@See docker
nginx.conf
	location / {
      return 301 https://yourdomain;
    }