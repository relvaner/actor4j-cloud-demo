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
package io.actor4j.cloud.demo.backend.services.authorizationservice;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import io.actor4j.cloud.demo.shared.utils.User;
import io.actor4j.core.immutable.ImmutableObject;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.jsonwebtoken.Jwts;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class AuthorizationServiceActor extends PodChildActor {
	public static final int CMD_INIT_SECRET_KEY = PodRequestMethod.ACTION_1;
	
	public static final String JWT_ISSUER    = "https://yourdomain/demo";
	public static final String JWT_AUDIENCE  = "actor4j-cloud-demo";
	public static final String JWT_AUTHORITY = "authority";
	
	protected Key SECRET_KEY;
	
	public AuthorizationServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (message.tag()==PodRequestMethod.POST && message.value()!=null && message.value() instanceof JsonObject) {
			Object result = null;
			int tag = PodStatus.OK;
			
			try {
				CredentialsDTO credentials = ((JsonObject)message.value()).mapTo(CredentialsDTO.class);
				if (credentials!=null) {
					if (verified(credentials))
						// Simplified: Frontend ensures that "credentials.username" is a unique id
						result =  createJWT(credentials.username(), credentials.username(), User.ROLE_USER);
				}
				else
					tag = PodStatus.INTERNAL_SERVER_ERROR;
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
				result = e.getCause().getMessage();
				tag = PodStatus.BAD_REQUEST;
			}
			
			tell(result, tag, message.source(), message.interaction());
		}
		else if (message.tag()==CMD_INIT_SECRET_KEY && message.value()!=null && message.value() instanceof ImmutableObject) {
			@SuppressWarnings("unchecked")
			User user = ((PodActorMessage<JsonObject, User, Void>)message).user();
			if (user!=null && user.principal()!=null) {
				String authority = null;
				if (user.principal().containsKey(JWT_AUTHORITY))
					authority = user.principal().getString(JWT_AUTHORITY);
				
				if (authority!=null && authority.equalsIgnoreCase(User.ROLE_SYSTEM)) {
					@SuppressWarnings("unchecked")
					ImmutableObject<Key> immutableObject = (ImmutableObject<Key>)message.value();
					if (immutableObject.get() instanceof Key)
						SECRET_KEY = immutableObject.get();
					
					tell(null, PodStatus.OK, message.source(), message.interaction());
				}
				else
					tell(null, PodStatus.FORBIDDED, message.source(), message.interaction());
			}
			else
				tell(null, PodStatus.UNAUTHORIZED, message.source(), message.interaction());
		}
		else
			tell(null, PodStatus.METHOD_NOT_ALLOWED, message.source(), message.interaction());
	}
	
	protected boolean verified(CredentialsDTO credentials) {
		return true;
	}
	
	// https://docs.spring.io/spring-security/site/docs/4.0.x/apidocs/org/springframework/security/core/Authentication.html#getAuthorities--
	// https://github.com/jhipster/jhipster-sample-app/blob/main/src/main/java/io/github/jhipster/sample/security/AuthoritiesConstants.java
	protected String createJWT(String userid, String username, String userrole) {
		String result = "";
				
		if (SECRET_KEY!=null)		
			result = Jwts.builder()
				.subject(username)
				.id(userid)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 3600000*24))
				.issuer(JWT_ISSUER)
				.audience().add(JWT_AUDIENCE).and()
				.claim(JWT_AUTHORITY, userrole)
				.signWith(SECRET_KEY)
				.compact();
		
		return result;
	}
}
