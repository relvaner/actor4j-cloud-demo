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

import io.actor4j.cloud.demo.module.shared.utils.User;
import io.actor4j.cloud.demo.single.instance.service.Actor4jService;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class Actor4jVerticle extends AbstractVerticle {
	public void start() {
		vertx.eventBus().<JsonObject>consumer("actors").handler(message -> {
			/*
			RemotePodMessageDTO remotePodMessageDTO = message.body().mapTo(RemotePodMessageDTO.class);
			
			if (!Actor4jService.getService().sendViaAliasAsServer(new ActorMessage<RemotePodMessage>(
					new RemotePodMessage(remotePodMessageDTO, message.replyAddress()), 0, Actor4jService.getService().SYSTEM_ID, null), remotePodMessageDTO.alias))
				message.fail(404, "The requested actor was not found!");
			*/
			// IllegalArgumentException
			
			JsonObject body = message.body();
			
			User user = null;
			if (body.containsKey("principal")) {
				JsonObject principal = body.getJsonObject("principal");
				user = new User(principal);
				body.remove("principal");
			}
			RemotePodMessageDTO remotePodMessageDTO = evaluateMessage(body);
			if (!Actor4jService.getService().sendViaAliasAsServer(ActorMessage.create(
					new RemotePodMessage(remotePodMessageDTO, message.replyAddress(), user), 0, Actor4jService.getService().SYSTEM_ID(), null), remotePodMessageDTO.alias()))
				message.fail(404, "The requested actor was not found!");
		});
	}
	
	public RemotePodMessageDTO evaluateMessage(JsonObject body) {
		Object payload = null;
		if (body.containsKey("payload"))
			payload = body.getValue("payload");
		Object params = null;
		if (body.containsKey("params"))
			params = body.getValue("params");
		
		RemotePodMessageDTO buf = body.mapTo(RemotePodMessageDTO.class);
		return new RemotePodMessageDTO(payload, buf.tag(), buf.alias(), params, buf.reply());
	}
}
