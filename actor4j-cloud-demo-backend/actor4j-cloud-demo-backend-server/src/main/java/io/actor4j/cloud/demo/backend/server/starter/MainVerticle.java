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

package io.actor4j.cloud.demo.backend.server.starter;

import java.security.Key;
import java.util.Map;
import java.util.Set;

import io.actor4j.cloud.demo.backend.server.verticles.ServerVerticle;
import io.actor4j.cloud.demo.backend.services.adservice.AdServiceActorPod;
import io.actor4j.cloud.demo.backend.services.authorizationservice.AuthorizationServiceActorPod;
import io.actor4j.cloud.demo.backend.services.cartservice.CartServiceActorPod;
import io.actor4j.cloud.demo.backend.services.checkoutservice.CheckoutServiceActorPod;
import io.actor4j.cloud.demo.backend.services.currencyservice.CurrencyServiceActorPod;
import io.actor4j.cloud.demo.backend.services.emailservice.EmailServiceActorPod;
import io.actor4j.cloud.demo.backend.services.functions.recomendationservice.RecomendationServiceFunctionPod;
import io.actor4j.cloud.demo.backend.services.paymentservice.PaymentServiceActorPod;
import io.actor4j.cloud.demo.backend.services.productcatalogservice.ProductCatalogServiceActorPod;
import io.actor4j.cloud.demo.backend.services.recomendationservice.RecomendationServiceActorPod;
import io.actor4j.cloud.demo.backend.services.shippingservice.ShippingServiceActorPod;
import io.actor4j.cloud.demo.shared.utils.User;
import io.actor4j.cloud.demo.single.instance.service.Actor4jService;
import io.actor4j.cloud.demo.single.instance.utils.PodPolicy;
import io.actor4j.cloud.demo.single.instance.verticles.Actor4jVerticle;
import io.actor4j.core.immutable.ImmutableObject;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {
	public static final int MIN_REPLICA = 3;
	public static final int MAX_REPLICA = 3;
	
	@Override
	public void start() {
		Global.config();
		
		Actor4jService.start(vertx);
		deployPods();
		
		int instances = Runtime.getRuntime().availableProcessors();
		
		vertx.deployVerticle(Actor4jVerticle.class.getName(), new DeploymentOptions().setInstances(instances));
		vertx.deployVerticle(ServerVerticle.class.getName(), new DeploymentOptions().setInstances(instances));
	}
	
	public void deployPods() {
		Actor4jService.getPodPolicies().put("ping", 
			PodPolicy.allowAll());
		
		Actor4jService.deployPods(
			() -> new AuthorizationServiceActorPod(), 
			new PodConfiguration("AuthorizationService", AuthorizationServiceActorPod.class.getName(), 1, 1),
			PodPolicy.allow(Map.of(
				User.ROLE_PUBLIC, Set.of(PodRequestMethod.POST)
		)));
		User admin = new User(new JsonObject().put("authority", User.ROLE_SYSTEM));
		Actor4jService.send(new ImmutableObject<Key>(Global.ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY), PodRequestMethod.ACTION_1, "AuthorizationService", admin, null);
		
		Actor4jService.deployPods(
			() -> new CartServiceActorPod(), 
			new PodConfiguration("CartService", CartServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(
				User.ROLE_USER, Set.of(
					PodRequestMethod.GET, 
					PodRequestMethod.POST, 
					PodRequestMethod.DELETE)
		)));
		Actor4jService.deployPods(
			() -> new ProductCatalogServiceActorPod(), 
			new PodConfiguration("ProductCatalogService", ProductCatalogServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(
				User.ROLE_PUBLIC, Set.of(
					PodRequestMethod.GET, 
					PodRequestMethod.GET_ALL, 
					PodRequestMethod.ACTION_1,
					PodRequestMethod.ACTION_2)
		)));
		Actor4jService.deployPods(
			() -> new CurrencyServiceActorPod(), 
			new PodConfiguration("CurrencyService", CurrencyServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(
				User.ROLE_PUBLIC, Set.of( 
					PodRequestMethod.GET_ALL, 
					PodRequestMethod.ACTION_1)
			)));
		Actor4jService.deployPods(
			() -> new PaymentServiceActorPod(), 
			new PodConfiguration("PaymentService", PaymentServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.denyAll());
		Actor4jService.deployPods(
			() -> new ShippingServiceActorPod(), 
			new PodConfiguration("ShippingService", ShippingServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(
				User.ROLE_PUBLIC, Set.of(PodRequestMethod.ACTION_1)
		)));
		Actor4jService.deployPods(
			() -> new EmailServiceActorPod(), 
			new PodConfiguration("EmailService", EmailServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.denyAll());
		Actor4jService.deployPods(
			() -> new CheckoutServiceActorPod(), 
			new PodConfiguration("CheckoutService", CheckoutServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(  
				User.ROLE_PUBLIC, Set.of(PodRequestMethod.POST)
		)));
		Actor4jService.deployPods(
			() -> new RecomendationServiceActorPod(), 
			new PodConfiguration("RecomendationService", RecomendationServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(
				User.ROLE_PUBLIC, Set.of(PodRequestMethod.GET_ALL)
		)));
		Actor4jService.deployPods(
			() -> new AdServiceActorPod(), 
			new PodConfiguration("AdService", AdServiceActorPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(  
				User.ROLE_PUBLIC, Set.of(
					PodRequestMethod.GET_ALL,
					PodRequestMethod.ACTION_1,
					PodRequestMethod.ACTION_2)
		)));
		
		Actor4jService.deployPods(
			() -> new RecomendationServiceFunctionPod(), 
			new PodConfiguration("RecomendationServiceAsFunction", RecomendationServiceFunctionPod.class.getName(), MIN_REPLICA, MAX_REPLICA),
			PodPolicy.allow(Map.of(  
				User.ROLE_PUBLIC, Set.of(PodRequestMethod.GET_ALL)
		)));
	}
	
	public void stop() {
		Actor4jService.stop();
	}
}
