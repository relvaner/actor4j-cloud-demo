/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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

import io.actor4j.core.json.JsonArray;
import io.actor4j.core.json.JsonObject;

// boundary between Vert.x (HTTP, event bus) and the actors: Vert.x JSON outside, JSON of actor4j inside, other values unchanged
public final class VertxJson {
	private VertxJson() {
	}

	public static Object toActor4j(Object value) {
		if (value instanceof io.vertx.core.json.JsonObject object)
			return JsonObject.create(object.encode());
		else if (value instanceof io.vertx.core.json.JsonArray array)
			return JsonArray.create(array.encode());
		else
			return value;
	}

	public static JsonObject toActor4j(io.vertx.core.json.JsonObject object) {
		return object!=null ? JsonObject.create(object.encode()) : null;
	}

	public static Object toVertx(Object value) {
		if (value instanceof JsonObject object)
			return new io.vertx.core.json.JsonObject(object.encode());
		else if (value instanceof JsonArray array)
			return new io.vertx.core.json.JsonArray(array.encode());
		else
			return value;
	}
}
