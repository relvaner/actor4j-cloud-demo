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
import io.actor4j.core.runtime.ActorGlobalSettings;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.ActorMessageUtils;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.api.Database;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
			ActorMessageUtils.SUPPORTED_TYPES.add(JsonObject.class);
			ActorMessageUtils.SUPPORTED_TYPES.add(JsonArray.class);
			ActorMessageUtils.SUPPORTED_TYPES.add(SecretKeySpec.class);
			
			ActorGlobalSettings.internal_server_callback = (replyAddress, result, tag) -> 
				vertx.eventBus().publish((String)replyAddress, new JsonObject().put("payload", result).put("status", tag));
			
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
			systemLogger().log(INFO, String.format("%s - Service started...", service.getConfig().name()));
			register();
			service.start();
		}
	}
	
	public static void register() {
		if (service!=null) {
			((InternalActorSystem)service).addSystemActor(() -> new HealthActor());
		
			service.deployPods(
				() -> new PingActorPod(), 
				new PodConfiguration("ping", PingActorPod.class.getName(), 1, 1));
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
