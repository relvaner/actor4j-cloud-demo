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

package io.actor4j.cloud.demo.frontend.starter;

import io.actor4j.cloud.demo.frontend.verticles.ServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends AbstractVerticle {
	@Override
	public void start() {
		Global.config();
		
		int instances = Runtime.getRuntime().availableProcessors();
		
		vertx.deployVerticle(ServerVerticle.class.getName(), new DeploymentOptions().setInstances(instances));
	}
	
	public void stop() {
		// empty
	}
}
