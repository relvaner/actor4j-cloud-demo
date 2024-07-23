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

package io.actor4j.cloud.demo.single.instance.verticles;

import io.actor4j.cloud.demo.single.instance.starter.DefaultGlobal;
import io.actor4j.cloud.demo.single.instance.utils.JsonSanitizer;
import io.actor4j.cloud.demo.single.instance.utils.PodAuthHandler;
import io.actor4j.cloud.demo.single.instance.utils.PodWebsocketHandler;
import io.actor4j.cloud.demo.single.instance.utils.config.ActorsServerConfig;
import io.actor4j.cloud.demo.single.instance.utils.config.RestServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public abstract class AbstractServerVerticle extends AbstractVerticle {
	public void start() throws Exception {
		Router router = Router.router(vertx);
		
		serverConfig(router);
		createHttpServer(router);
	}
	
	public void serverConfig(Router router) {
		router.route().handler(CorsHandler.create("*")
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS)
				.allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.PATCH)
				.allowedMethod(HttpMethod.DELETE)
				
				.allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Methods")
				.allowedHeader("Access-Control-Allow-Headers")
				.allowedHeader("Content-Type")
				);
		
		// @See: https://www.javadoc.io/doc/io.vertx/vertx-web/3.9.10/io/vertx/ext/web/handler/CookieHandler.html
		// cookies are enabled by default and this handler is not required anymore
		// router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create()
				.setBodyLimit(20480000)
				.setHandleFileUploads(true)
				.setMergeFormAttributes(true)
				.setPreallocateBodyBuffer(false)
				.setDeleteUploadedFilesOnEnd(true)
				);
		
		RestServerConfig.config(vertx, router);
		ActorsServerConfig.config(vertx, router, createJsonSanitizer(), createPodAuthHandler());
		webServerConfig(router);
	}
	
	public void createHttpServer(Router router) {
		vertx.createHttpServer(new HttpServerOptions())
			.requestHandler(router)
			.listen(DefaultGlobal.ACTOR4J_CLOUD_PORT.getValue());
	}
	
	public void createWebsocket(Router router) {
		vertx.createHttpServer(new HttpServerOptions())
			.webSocketHandler(new PodWebsocketHandler(vertx, createJsonSanitizer(), createPodAuthHandler()))
			.listen(DefaultGlobal.ACTOR4J_CLOUD_WEBSOCKET_PORT.getValue());
	}
	
	public void webServerConfig(Router router) {
		router.get("/")
			.produces("application/xhtml+xml")
			.handler(rc ->  rc.response().end(
					DefaultGlobal.ACTOR4J_CLOUD_VENDOR.getValue()+"-instance"+ 
					(DefaultGlobal.ACTOR4J_CLOUD_COPYRIGHT.getValue().isEmpty() ? "" : " - "+DefaultGlobal.ACTOR4J_CLOUD_COPYRIGHT.getValue())
					));
	}	
	
	public abstract PodAuthHandler createPodAuthHandler();
	
	public JsonSanitizer createJsonSanitizer() {
		return null;
	}
}
