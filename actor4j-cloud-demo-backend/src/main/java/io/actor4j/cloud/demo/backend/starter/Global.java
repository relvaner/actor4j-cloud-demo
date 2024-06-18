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

package io.actor4j.cloud.demo.backend.starter;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.mutable.MutableObject;

import io.actor4j.cloud.demo.module.single.instance.starter.DefaultGlobal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public final class Global extends DefaultGlobal {
	public static final MutableObject<String> ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR = new MutableObject<String>("");
	public static SecretKey ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY;
	
	public static void config() {
		ACTOR4J_CLOUD_COPYRIGHT.setValue("Copyright (c) 2019-2024, actor4j.io");
		DefaultGlobal.config();
	
		checkString(ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR, "ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR");
		if (ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR.getValue().isEmpty())
			ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY = Jwts.SIG.HS512.key().build();
		else
			ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(ACTOR4J_CLOUD_AUTHORIZATION_SECRET_KEY_STR.getValue()));

	}
}
