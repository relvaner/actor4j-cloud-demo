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

package io.actor4j.cloud.demo.single.instance.service;

import static io.actor4j.core.logging.ActorLogger.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

import io.actor4j.cloud.demo.shared.utils.User;
import io.actor4j.cloud.demo.single.instance.actors.HealthActor;
import io.actor4j.cloud.demo.single.instance.pods.PingActorPod;
import io.actor4j.cloud.demo.single.instance.utils.PodPolicy;
import io.actor4j.core.ActorRuntime;
import io.actor4j.core.ActorService;
import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.config.ActorRuntimeConfig;
import io.actor4j.core.runtime.config.InternalServerCallback;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.ActorMessageUtils;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodDeployment;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.api.Database;
import io.actor4j.cloud.demo.single.instance.utils.VertxJson;
import io.actor4j.core.json.JsonArray;
import io.actor4j.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

public class Actor4jService {
	protected static ActorService service;
	
	protected static final Map<String, PodPolicy> podPolicies;

	static {
		podPolicies = new ConcurrentHashMap<>();
	}
	
	private Actor4jService() {
		super();
	}

	public static void start(Vertx vertx) {
		start(vertx, null);
	}
	
	public static void start(Vertx vertx, Database<?> database) {
		if (service==null) {
			// JSON of actor4j as message value (the classes of the implementation on the classpath)
			ActorMessageUtils.registerSupportedType(
				JsonObject.create().getClass(),
				JsonArray.create().getClass(),
				SecretKeySpec.class
			);
			
			// replyTo is the Vert.x message of the request (Actor4jVerticle), the answer goes back with reply
			InternalServerCallback internalServerCallback = (replyTo, result, tag) -> {
				io.vertx.core.json.JsonObject answer = new io.vertx.core.json.JsonObject().put("payload", VertxJson.toVertx(result)).put("status", tag);
				if (replyTo instanceof Message<?> message)
					message.reply(answer);
				else if (replyTo instanceof String replyAddress)
					vertx.eventBus().send(replyAddress, answer);
			};
			ActorRuntimeConfig runtimeConfig = ActorRuntimeConfig.builder()
				.internalServerCallback(internalServerCallback)
				.build();
			
			/*
			ActorAnalyzerConfig config = ActorAnalyzerConfig.builder()
				.parallelism(4)
				.serverMode()
				.build();
			service = (ActorService)ActorAnalyzer.create(new DefaultActorAnalyzerThread(2000, true, true, true), config);
			*/
				
			ActorServiceConfig config = ActorServiceConfig.builder()
				.podHost(() -> vertx)
				.podDatabase(database)
				.debugUnhandled(true)
				.debugUndelivered(true)
				.build();
			service = ActorService.create(ActorRuntime.factory(), config);
			((InternalActorSystem)service).setRuntimeConfig(runtimeConfig);
			systemLogger().log(INFO, String.format("%s - Service started...", service.getConfig().name()));
			register();
			service.start();
		}
	}
	
	public static void register() {
		if (service!=null) {
			((InternalActorSystem)service).addSystemActor(() -> new HealthActor());
		
			service.deployPods(() -> new PingActorPod(), new PodDeployment());
		}
	}
	
	// the domain comes from the pod itself
	public static void deployPods(PodFactory factory, PodDeployment podDeployment, PodPolicy podPolicy) {
		if (service!=null) {
			podPolicies.put(factory.create().domain(), podPolicy);
			service.deployPods(factory, podDeployment);
		}
	}

	public static void deployPods(PodFactory factory, PodConfiguration podConfiguration, PodPolicy podPolicy) {
		if (service!=null) {
			podPolicies.put(podConfiguration.domain(), podPolicy);
			service.deployPods(factory, podConfiguration);
		}
	}
	
	public static boolean send(Object payload, int tag, String domain, User user, Object params) {
		boolean result = false;
		
		if (service!=null)
			result = service.sendViaAliasAsServer(ActorMessage.create(
				new RemotePodMessage(new RemotePodMessageDTO(payload, tag, domain, params, false), null, user), 
				0, Actor4jService.getService().SYSTEM_ID(), null), domain);
		
		return result;
	}

	public static void stop() {
		if (service!=null) {
			service.shutdownWithActors(true);
			systemLogger().log(INFO, String.format("%s - Service stopped...", service.getConfig().name()));
		}
	}

	public static ActorService getService() {
		return service;
	}
	
	public static Map<String, PodPolicy> getPodPolicies() {
		return podPolicies;
	}
}
