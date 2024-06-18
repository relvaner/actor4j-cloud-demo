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

package io.actor4j.cloud.demo.frontend.verticles;

import io.actor4j.cloud.demo.frontend.starter.Global;
import io.actor4j.cloud.demo.frontend.utils.config.RestServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ServerVerticle extends AbstractVerticle {
	public void start() throws Exception {
		Router mainRouter = null;
		
		Router router = Router.router(vertx);
		
		if (!Global.ACTOR4J_CLOUD_CONTEXT_PATH.getValue().isEmpty()) {
			mainRouter = Router.router(vertx);
			mainRouter.mountSubRouter(Global.ACTOR4J_CLOUD_CONTEXT_PATH.getValue(), router);
			
			// https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/test/java/io/vertx/ext/web/SubRouterTest.java
			// mainRouter.route(Global.ACTOR4J_CLOUD_CONTEXT_PATH.getValue()).subRouter(router);
		}
		
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
		router.route().handler(StaticHandler.create("webapp"));
		
		RestServerConfig.config(vertx, router, Global.ACTOR4J_CLOUD_VENDOR.getValue());
		
		vertx.createHttpServer(new HttpServerOptions())
			.requestHandler(mainRouter!=null ? mainRouter : router)
			.listen(Global.ACTOR4J_CLOUD_PORT.getValue());
	}
}
