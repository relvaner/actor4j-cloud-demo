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

package io.actor4j.cloud.demo.single.instance.utils;

import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class PodRequestHandler implements Handler<RoutingContext> {
	protected Vertx vertx;
	protected PodAuthHandler podAuthHandler;
	protected JsonSanitizer jsonSanitizer;
	
	public PodRequestHandler(Vertx vertx, JsonSanitizer jsonSanitizer, PodAuthHandler podAuthHandler) {
		super();
		this.vertx = vertx;
		this.jsonSanitizer = jsonSanitizer;
		this.podAuthHandler = podAuthHandler;
	}
	
	@Override
	public void handle(RoutingContext rc) {
		JsonObject bodyAsJson = null;
		if (jsonSanitizer!=null)
			bodyAsJson = jsonSanitizer.sanitize(rc.body().asString());
		else
			bodyAsJson = rc.body().asJsonObject();
		
		RemotePodMessageDTO remotePodMessageDTO = null;
		try {
			remotePodMessageDTO = bodyAsJson.mapTo(RemotePodMessageDTO.class);
		}
		catch (IllegalArgumentException e) {
			rc.fail(e);
		}
		if (remotePodMessageDTO!=null && remotePodMessageDTO.alias()!=null && !remotePodMessageDTO.alias().isEmpty()) {
			if (rc.get(PodHttpHeaders.X_POD_DOMAIN).equals(remotePodMessageDTO.alias()) && rc.get(PodHttpHeaders.X_POD_REQUEST_METHOD).equals(remotePodMessageDTO.tag())) {
				bodyAsJson.put("principal", podAuthHandler.getPrincipal());
				
				if (remotePodMessageDTO.reply())
					vertx.eventBus().<Object>request("actors", bodyAsJson, response -> {
						if (response.succeeded()) {
							JsonObject obj = (JsonObject)response.result().body();
							int statusCode = obj.getInteger("status");
							
							rc.response().end(JsonObject.mapFrom(new RESTDefaultResponse(
									RESTDefaultResponse.SUCCESS, 200, new Pod(statusCode, PodStatus.getStatus(statusCode)), obj.getValue("payload"), "")).encodePrettily());
						}
						else
							rc.fail(((ReplyException)response.cause()).failureCode());
					});
				else {
					vertx.eventBus().send("actors", bodyAsJson);
					rc.response().setStatusCode(202);
					rc.response().end(JsonObject.mapFrom(new RESTDefaultResponse(
							RESTDefaultResponse.SUCCESS, 202, new Pod(), "", "The request was accepted and the actor will be called.")).encodePrettily());
				}
			}
			else
				rc.fail(400);
		}
		else
			rc.fail(400);
	}
	
	public static Handler<RoutingContext> createFailureHandler() {
		return rc -> {
			if (rc.statusCode()==404) {
				rc.response().setStatusCode(404);
				rc.response().end(JsonObject.mapFrom(new RESTDefaultResponse(
						RESTDefaultResponse.NO_SUCCESS, 404, new Pod(), "", "The requested actor was not found!")).encodePrettily());
			}
			else {
				rc.response().setStatusCode(400);
				rc.response().end(JsonObject.mapFrom(new RESTDefaultResponse(
						RESTDefaultResponse.ERROR, 400, new Pod(), rc.failure()!=null ? rc.failure().getMessage() : "", "The request was error prone.")).encodePrettily());
			}
		};
	}
}
