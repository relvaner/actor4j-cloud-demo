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
package io.actor4j.cloud.demo.module.single.instance.utils;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

public class WebsocketManager {
	public static final String INTERNAL_WEBSOCKET_CONSUMER_ADDRESS = "websocket";
	
	protected static final Set<ServerWebSocket> INTERNAL_SERVER_WEBSOCKETS;
	
	static {
		INTERNAL_SERVER_WEBSOCKETS = ConcurrentHashMap.newKeySet();
	}
	
	public static void initialize(Vertx vertx) {
		vertx.eventBus().consumer(INTERNAL_WEBSOCKET_CONSUMER_ADDRESS).handler(message -> {
			Iterator<ServerWebSocket> iterator = INTERNAL_SERVER_WEBSOCKETS.iterator();
			while (iterator.hasNext())
				iterator.next().writeTextMessage(message.body().toString());
		});
	}
	
	public static void add(ServerWebSocket ws) {
		INTERNAL_SERVER_WEBSOCKETS.add(ws);
	}
	
	public static void remove(ServerWebSocket ws) {
		INTERNAL_SERVER_WEBSOCKETS.remove(ws);
	}
}
