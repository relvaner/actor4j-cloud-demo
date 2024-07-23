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

package io.actor4j.cloud.demo.single.instance.utils.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.actor4j.cloud.demo.single.instance.service.Actor4jService;
import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.messages.ActorMessage;
import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;

public class RestServerConfig extends DefaultRestServerConfig {
	public static void config(Vertx vertx, Router router) {
		config(vertx, router, "actor4j", future -> {
			PseudoActor pseudo = new PseudoActor(Actor4jService.getService(), true) {
				@Override
				public void receive(ActorMessage<?> message) {
					// empty
				}
			};
			pseudo.sendViaAlias(ActorMessage.create(null, 0, pseudo.self(), null), "health");
			ActorMessage<?> message = null;
			try {
				message = pseudo.await(2000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | TimeoutException e) {
				e.printStackTrace();
			}
			
			if (message!=null && message.value() instanceof Integer && message.valueAsInt()==200)
				future.complete(Status.OK());
			else
				future.complete(Status.KO());
		});
	}
}
