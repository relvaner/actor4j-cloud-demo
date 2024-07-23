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

package io.actor4j.cloud.demo.backend.verticles;

import io.actor4j.cloud.demo.backend.utils.DefaultPodAuthHandler;
import io.actor4j.cloud.demo.module.single.instance.utils.JsonSanitizer;
import io.actor4j.cloud.demo.module.single.instance.utils.PodAuthHandler;
import io.actor4j.cloud.demo.module.single.instance.verticles.AbstractServerVerticle;
import io.vertx.core.json.JsonObject;

public class ServerVerticle extends AbstractServerVerticle {
	@Override
	public PodAuthHandler createPodAuthHandler() {
		return new DefaultPodAuthHandler();
	}
	
	@Override
	public JsonSanitizer createJsonSanitizer() {
		// OWASP JSON Sanitizer, https://github.com/OWASP/json-sanitizer
		return (json) -> new JsonObject(com.google.json.JsonSanitizer.sanitize(json));
	}
}
