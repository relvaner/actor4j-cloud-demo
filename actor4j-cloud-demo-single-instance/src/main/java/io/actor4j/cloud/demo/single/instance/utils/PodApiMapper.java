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
package io.actor4j.cloud.demo.single.instance.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static io.actor4j.core.messages.ActorReservedTag.*;

import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.json.JsonObject;

// translation between the external API (method names, status without offset) and the internal pod tags
public final class PodApiMapper {
	private static final Map<String, Integer> METHODS = new HashMap<>();

	static {
		for (Field field : PodRequestMethod.class.getFields())
			if (Modifier.isStatic(field.getModifiers()) && field.getType()==int.class && !field.getName().equals("OFFSET"))
				try {
					METHODS.put(field.getName(), field.getInt(null));
				}
				catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
	}

	private PodApiMapper() {
	}

	// a name (GET, ACTION_1, ...) becomes the internal pod request method, a number is passed through as own tag,
	// null if invalid (see allowed)
	public static Integer method(Object value) {
		Integer result = null;
		if (value instanceof Number number)
			result = number.intValue();
		else if (value instanceof String str && !str.isBlank()) {
			result = METHODS.get(str.trim().toUpperCase());
			if (result==null)
				try {
					result = Integer.valueOf(str.trim());
				}
				catch (NumberFormatException e) {
					// empty
				}
		}

		return result!=null && allowed(result) ? result : null;
	}

	// named pod request methods and own tags >= 0 outside the reserved range of actor4j,
	// negative tags are system directives (e.g. stop, kill) and must not come from outside
	public static boolean allowed(int tag) {
		return METHODS.containsValue(tag) || (tag>=0 && (tag<RESERVED_RANGE_START || tag>=RESERVED_RANGE_END));
	}

	// replaces the tag of the request by the internal one, false if the tag is invalid
	public static boolean normalize(JsonObject body) {
		Integer tag = method(body.getValue("tag"));
		if (tag!=null)
			body.put("tag", tag);

		return tag!=null;
	}

	// pod status without offset, other tags unchanged
	public static int status(int podStatus) {
		return podStatus>=RESERVED_POD_STATUS_RANGE_START && podStatus<RESERVED_POD_STATUS_RANGE_END ? podStatus-PodStatus.OFFSET : podStatus;
	}

	// response to a pod answer, a pod status 4xx or 5xx becomes the (HTTP) status
	public static RESTDefaultResponse response(int podStatus, Object payload) {
		int status = status(podStatus);
		Pod pod = new Pod(status, PodStatus.getStatus(podStatus));

		if (status>=400 && status<600)
			return new RESTDefaultResponse(RESTDefaultResponse.NO_SUCCESS, status, pod, payload, pod.message!=null ? pod.message : "");
		else
			return new RESTDefaultResponse(RESTDefaultResponse.SUCCESS, 200, pod, payload, "");
	}
}
