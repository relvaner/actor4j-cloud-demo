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
package io.actor4j.cloud.demo.backend.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Utils {
	private static Set<Class<?>> PRIMITIVE_WRAPPERS;
	
	static {
		PRIMITIVE_WRAPPERS = new HashSet<Class<?>>();
		PRIMITIVE_WRAPPERS.add(Byte.class);
		PRIMITIVE_WRAPPERS.add(Short.class);
		PRIMITIVE_WRAPPERS.add(Integer.class);
		PRIMITIVE_WRAPPERS.add(Long.class);
		PRIMITIVE_WRAPPERS.add(Float.class);
		PRIMITIVE_WRAPPERS.add(Double.class);
		PRIMITIVE_WRAPPERS.add(Character.class);
		PRIMITIVE_WRAPPERS.add(String.class);
		PRIMITIVE_WRAPPERS.add(Boolean.class);
		PRIMITIVE_WRAPPERS.add(Void.class);
	}
	
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}
	
	public static boolean isWrapperType(Class<?> type) {
		return PRIMITIVE_WRAPPERS.contains(type);
	}

	public static <T> JsonArray mapFrom(List<T> list) {
		JsonArray result = new JsonArray();
		for (T obj : list) {
			if (isWrapperType(obj.getClass()))
				result.add(obj);
			else
				result.add(JsonObject.mapFrom(obj));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> mapTo(JsonArray jsonArray, Class<T> valueType) {
		List<T> result = new ArrayList<>(jsonArray.size());
		
		Iterator<Object> iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			if (isWrapperType(obj.getClass()))
				result.add((T)obj);
			else
				result.add(((JsonObject)obj).mapTo(valueType));
		}
		
		return result;
	}
	
	public static JsonObject parse(String json) {
		JsonObject result = null;
		try {
			result = new JsonObject(json);
		}
		catch (Exception e) {
			// empty
		}
		
		return result;
	}
}
