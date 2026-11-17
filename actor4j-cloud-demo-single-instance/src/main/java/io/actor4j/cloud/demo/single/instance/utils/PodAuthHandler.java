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

import org.apache.commons.lang3.StringUtils;

import io.actor4j.cloud.demo.shared.utils.User;
import io.actor4j.cloud.demo.single.instance.service.Actor4jService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

public abstract class PodAuthHandler implements AuthenticationHandler {
	protected JsonObject principal;
	
	@Override
	public void handle(RoutingContext rc) {
		principal = null;
		
		String jwt = rc.request().headers().get(PodHttpHeaders.X_POD_AUTHORIZATION);
		if (jwt!=null && StringUtils.isNotBlank(jwt))
			principal = authenticate(jwt.substring("Bearer ".length()));
		
		String authority = null;
		if (principal!=null && principal.containsKey("authority"))
			authority = principal.getString("authority");
		else
			authority = User.ROLE_UNAUTHENTICATED;
		
		String domain = rc.request().headers().get(PodHttpHeaders.X_POD_DOMAIN);
		String podRequestMethodStr = rc.request().headers().get(PodHttpHeaders.X_POD_REQUEST_METHOD);
		// name (GET, ACTION_1, ...) or own tag as number
		Integer method = PodApiMapper.method(podRequestMethodStr);
		if (method==null) {
			badRequest(rc.response(), "Invalid request method!");
			return;
		}
		int podRequestMethod = method;
		
		if (isUnknownMethod(domain, podRequestMethod))
			methodNotAllowed(rc.response());
		else if (isAuthorized(domain, authority, podRequestMethod)) {
			rc.put(PodHttpHeaders.X_POD_DOMAIN, domain);
			rc.put(PodHttpHeaders.X_POD_REQUEST_METHOD, podRequestMethod);
			rc.next();	
		}
		else
			notAuthorized(rc.response(), authority);
	}
	
	protected abstract JsonObject authenticate(Object auth);
	
	public static boolean isAuthorized(String domain, String authority, int podRequestMethod) {
		if (domain!=null && StringUtils.isNotBlank(domain)) {
			PodPolicy podPolicy = Actor4jService.getPodPolicies().get(domain);
		
			return podPolicy!=null && PodPolicy.allowedMethod(podPolicy, authority, podRequestMethod);
		}
		else
			return false;
	}
	
	// the pod exists, but no role may call the method
	public static boolean isUnknownMethod(String domain, int podRequestMethod) {
		if (domain!=null && StringUtils.isNotBlank(domain)) {
			PodPolicy podPolicy = Actor4jService.getPodPolicies().get(domain);

			return podPolicy!=null && !podPolicy.authorizedMethods.isEmpty() && !PodPolicy.knownMethod(podPolicy, podRequestMethod);
		}
		else
			return false;
	}

	public void badRequest(HttpServerResponse response, String message) {
		response.setStatusCode(400);
		response.end(JsonObject.mapFrom(new RESTDefaultResponse(
			RESTDefaultResponse.NO_SUCCESS, 400, new Pod(), "", message)).encodePrettily());
	}

	public void methodNotAllowed(HttpServerResponse response) {
		response.setStatusCode(405);
		response.end(JsonObject.mapFrom(new RESTDefaultResponse(
			RESTDefaultResponse.NO_SUCCESS, 405, new Pod(), "", "The method is not allowed for this pod!")).encodePrettily());
	}

	public void notAuthorized(HttpServerResponse response, String authority) {
		/* 
		 * @See: https://stackoverflow.com/questions/3297048/403-forbidden-vs-401-unauthorized-http-responses
		 * In summary, a 401 Unauthorized response should be used for missing or bad authentication, 
		 * and a 403 Forbidden response should be used afterwards, when the user is authenticated 
		 * but isn’t authorized to perform the requested operation on the given resource.
		 */
		if (authority.equalsIgnoreCase(User.ROLE_UNAUTHENTICATED)) {
			// Unauthenticated and unauthorized!!!
			response.setStatusCode(401);
			response.end(JsonObject.mapFrom(new RESTDefaultResponse(
				RESTDefaultResponse.NO_SUCCESS, 401, new Pod(), "", "The request was rejected!")).encodePrettily());
		}
		else {
			// Authenticated but unauthorized!!!
			response.setStatusCode(403);
			response.end(JsonObject.mapFrom(new RESTDefaultResponse(
				RESTDefaultResponse.NO_SUCCESS, 403, new Pod(), "", "The request was rejected!")).encodePrettily());
		}
	}

	public JsonObject getPrincipal() {
		return principal;
	}
}
