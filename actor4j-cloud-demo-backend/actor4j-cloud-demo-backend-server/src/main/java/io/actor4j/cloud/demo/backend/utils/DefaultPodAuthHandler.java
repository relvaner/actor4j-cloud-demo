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

import io.actor4j.cloud.demo.backend.starter.Global;
import io.actor4j.cloud.demo.module.single.instance.utils.PodAuthHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.vertx.core.json.JsonObject;

public class DefaultPodAuthHandler extends PodAuthHandler {
	@Override
	protected JsonObject authenticate(Object auth) {
		JsonObject result = null;
		
		if (auth!=null && auth instanceof String)
			try {
				Jws<Claims> jws = Jwts.parser().verifyWith(Global.ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY).build().parseSignedClaims((String)auth);
				
				// @See: https://datatracker.ietf.org/doc/html/rfc7519
				// @See: https://datatracker.ietf.org/doc/html/rfc9068
				// @See: https://datatracker.ietf.org/doc/html/rfc6750
				// @See: https://www.oauth.com/oauth2-servers/access-tokens/access-token-response/
				if (jws!=null) {
					result = new JsonObject();
					result.put("sub", jws.getPayload().getSubject());
					result.put("jti", jws.getPayload().getId());
					
					result.put("exp", jws.getPayload().getExpiration().getTime());
					result.put("iat", jws.getPayload().getIssuedAt().getTime());
					
					// result.put("access_token", (String)auth);
					
					if (jws.getPayload().containsKey("authority"))
						result.put("authority", jws.getPayload().get("authority"));
				}
			} catch (JwtException e) {
				// empty
			}
		
		return result;
	}
}
