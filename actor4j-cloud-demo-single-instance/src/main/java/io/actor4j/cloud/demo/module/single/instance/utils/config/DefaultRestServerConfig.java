/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.actor4j.cloud.demo.module.single.instance.utils.config;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;

public class DefaultRestServerConfig {
	protected static void baseConfig(Router router) {
		router.get("/api/ping")
			.produces("application/json")
			.handler(routingContext -> routingContext 
					.response().end(new JsonObject().put("result", "pong").encodePrettily()));
		router.get("/api/version")
			.produces("application/json")
			.handler(routingContext -> routingContext 
					.response().end(new JsonObject().put("result", "1.0.0").encodePrettily()));
	}
	
	public static void config(Vertx vertx, Router router, String name) {
		baseConfig(router);
		
		// @See: http://vertx.io/docs/vertx-health-check/java/
		HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
		healthCheckHandler.register(name, 2000, future -> {
			future.complete(Status.OK());
		});
		router.get("/health").handler(healthCheckHandler);
	}
	
	public static void config(Vertx vertx, Router router, String name, Handler<Promise<Status>> healthCheckProcedure) {
		baseConfig(router);
		
		// @See: http://vertx.io/docs/vertx-health-check/java/
		HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
		healthCheckHandler.register(name, 2000, healthCheckProcedure);
		router.get("/health").handler(healthCheckHandler);
	}
}
