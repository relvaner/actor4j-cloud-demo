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

import io.actor4j.cloud.demo.module.shared.utils.User;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class PodWebsocketHandler implements Handler<ServerWebSocket> {
	protected Vertx vertx;
	protected PodAuthHandler podAuthHandler;
	protected JsonSanitizer jsonSanitizer;
	
	
	public PodWebsocketHandler(Vertx vertx, JsonSanitizer jsonSanitizer, PodAuthHandler podAuthHandler) {
		super();
		this.vertx = vertx;
		this.jsonSanitizer = jsonSanitizer;
		this.podAuthHandler = podAuthHandler;
	}
	
	@Override
	public void handle(ServerWebSocket ws) {
		systemLogger().log(DEBUG, "Websocket connection established...");
		WebsocketManager.add(ws);
		
		ws.textMessageHandler(json -> {
			JsonObject bodyAsJson = null;
			if (jsonSanitizer!=null)
				bodyAsJson = jsonSanitizer.sanitize(json);
			else
				bodyAsJson = new JsonObject(json);
			
			RemotePodMessageDTO remotePodMessageDTO = null;
			try {
				remotePodMessageDTO = bodyAsJson.mapTo(RemotePodMessageDTO.class);
			}
			catch (IllegalArgumentException e) {
				ws_fail(ws, 400, e);
			}
			if (remotePodMessageDTO!=null && remotePodMessageDTO.alias()!=null && !remotePodMessageDTO.alias().isEmpty()) {
				JsonObject principal = podAuthHandler!=null ? podAuthHandler.authenticate(remotePodMessageDTO.auth()) : null;
				String authority = null;
				if (principal!=null && principal!=null && principal.containsKey("authority")) {
					authority = principal.getString("authority");
					bodyAsJson.put("principal", principal);
				}
				else
					authority = User.ROLE_UNAUTHENTICATED;
				
				if (PodAuthHandler.isAuthorized(remotePodMessageDTO.alias(), authority, remotePodMessageDTO.tag())) {
					if (remotePodMessageDTO.reply())
						vertx.eventBus().<Object>request("actors", bodyAsJson, response -> {
							if (response.succeeded()) {
								JsonObject obj = (JsonObject)response.result().body();
								int statusCode = obj.getInteger("status");
								
								ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
										RESTDefaultResponse.SUCCESS, 200, new Pod(statusCode, PodStatus.getStatus(statusCode)), obj.getValue("payload"), "")).encodePrettily());
							}
							else
								ws_fail(ws, ((ReplyException)response.cause()).failureCode(), null);
						});
					else {
						vertx.eventBus().send("actors", bodyAsJson);
						ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
								RESTDefaultResponse.SUCCESS, 202, new Pod(), "", "The request was accepted and the actor will be called.")).encodePrettily());
					}
				}
				else {
					if (authority.equalsIgnoreCase(User.ROLE_UNAUTHENTICATED))
						// Unauthenticated and unauthorized!!!
						ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
							RESTDefaultResponse.NO_SUCCESS, 401, new Pod(), "", "The request was rejected!")).encodePrettily());
					else
						// Authenticated but unauthorized!!!
						ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
							RESTDefaultResponse.NO_SUCCESS, 403, new Pod(), "", "The request was rejected!")).encodePrettily());
				}
			}
			else
				ws_fail(ws, 400, null);
		});		
		
		ws.closeHandler(handler -> {
			WebsocketManager.remove(ws);
			systemLogger().log(DEBUG, "Websocket connection closed...");
		});
	}

	public void ws_end(ServerWebSocket ws, String json) {
		ws.writeTextMessage(json);
	}

	public void ws_fail(ServerWebSocket ws, int status, Exception e) {
		if (status==404)
			ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
					RESTDefaultResponse.NO_SUCCESS, 404, new Pod(), "", "The requested actor was not found!")).encodePrettily());
		else
			ws_end(ws, JsonObject.mapFrom(new RESTDefaultResponse(
					RESTDefaultResponse.ERROR, 400, new Pod(), e!=null ? e.getMessage() : "", "The request was error prone.")).encodePrettily());
	}
	
	public void ws_reject(ServerWebSocket ws, int status) {
		ws.reject(status);
	}
}
