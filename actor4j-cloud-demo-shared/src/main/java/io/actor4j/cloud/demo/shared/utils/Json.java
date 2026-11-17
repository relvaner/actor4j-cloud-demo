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
package io.actor4j.cloud.demo.shared.utils;

import java.util.ArrayList;
import java.util.List;

import io.actor4j.core.json.JsonArray;
import io.actor4j.core.json.JsonObject;
import io.actor4j.core.json.ObjectMapper;

// mapping between DTOs and the JSON of actor4j (implementation: actor4j-dataformat-json-jackson)
public final class Json {
	private static final ObjectMapper objectMapper = ObjectMapper.create();

	private Json() {
	}

	// DTO or record -> JsonObject
	public static JsonObject from(Object obj) {
		return JsonObject.create(obj);
	}

	// list of DTOs, records or simple values -> JsonArray
	public static JsonArray fromList(List<?> list) {
		JsonArray result = JsonArray.create();
		for (Object obj : list)
			result.add(obj);

		return result;
	}

	public static <T> T mapTo(JsonObject json, Class<T> type) {
		return objectMapper.mapTo(json.encode(), type);
	}

	// JsonArray of objects or simple values -> list
	@SuppressWarnings("unchecked")
	public static <T> List<T> mapTo(JsonArray json, Class<T> type) {
		List<T> result = new ArrayList<>(json.size());
		for (Object obj : json)
			result.add(obj instanceof JsonObject object ? mapTo(object, type) : (T)obj);

		return result;
	}

	// null for invalid JSON
	public static JsonObject parse(String json) {
		try {
			return JsonObject.create(json);
		}
		catch (RuntimeException e) {
			return null;
		}
	}
}
