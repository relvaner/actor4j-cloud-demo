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

package io.actor4j.cloud.demo.module.single.instance.starter;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.logging.ActorLogger.logger;
import static io.actor4j.core.logging.ActorLogger.systemLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class DefaultGlobal {
	public static final MutableBoolean ACTOR4J_CLOUD_DOCKER = new MutableBoolean(false);
	public static final MutableObject<String> ACTOR4J_CLOUD_CONTEXT_PATH_FRONTEND = new MutableObject<String>("");
	
	public static final MutableObject<String> ACTOR4J_CLOUD_VENDOR = new MutableObject<String>("actor4j-cloud");
	public static final MutableObject<String> ACTOR4J_CLOUD_COPYRIGHT = new MutableObject<String>("");
	public static final MutableInt ACTOR4J_CLOUD_PORT = new MutableInt(8080);
	public static final MutableInt ACTOR4J_CLOUD_WEBSOCKET_PORT = new MutableInt(3000);
	
	public static final MutableObject<String> ACTOR4J_APPLICATION_DOMAIN = new MutableObject<String>("default");
	
	public static final MutableObject<String> ACTOR4J_SYSTEM_LOGGER = new MutableObject<String>("info");
	public static final MutableObject<String> ACTOR4J_LOGGER = new MutableObject<String>("debug");
	
	public static final Map<String, Level> LOGGER_LEVEL_MAP;
	
	static {
		LOGGER_LEVEL_MAP = new HashMap<>();
		LOGGER_LEVEL_MAP.put("error", ERROR);
		LOGGER_LEVEL_MAP.put("warn", WARN);
		LOGGER_LEVEL_MAP.put("info", INFO);
		LOGGER_LEVEL_MAP.put("debug", DEBUG);
		LOGGER_LEVEL_MAP.put("trace", TRACE);
	}
	
	public static void config() {
		checkBoolean(ACTOR4J_CLOUD_DOCKER, "ACTOR4J_CLOUD_DOCKER");
		checkString(ACTOR4J_CLOUD_CONTEXT_PATH_FRONTEND, "ACTOR4J_CLOUD_CONTEXT_PATH_FRONTEND");
		
		checkString(ACTOR4J_CLOUD_VENDOR, "ACTOR4J_CLOUD_VENDOR");
		checkString(ACTOR4J_CLOUD_COPYRIGHT, "ACTOR4J_CLOUD_COPYRIGHT");
		checkInteger(ACTOR4J_CLOUD_PORT, "ACTOR4J_CLOUD_PORT");	
		checkInteger(ACTOR4J_CLOUD_WEBSOCKET_PORT, "ACTOR4J_CLOUD_WEBSOCKET_PORT");
		
		checkString(ACTOR4J_APPLICATION_DOMAIN, "ACTOR4J_APPLICATION_DOMAIN");
		
		checkLogging(ACTOR4J_SYSTEM_LOGGER, "ACTOR4J_SYSTEM_LOGGER");
		Level level = LOGGER_LEVEL_MAP.get(ACTOR4J_SYSTEM_LOGGER.getValue());
		if (level!=null)
			systemLogger().setLevel(level);
		
		checkLogging(ACTOR4J_LOGGER, "ACTOR4J_LOGGER");
		level = LOGGER_LEVEL_MAP.get(ACTOR4J_LOGGER.getValue());
		if (level!=null)
			logger().setLevel(level);
	}
	
	public static void checkBoolean(MutableBoolean value, String name) {
		String env = null;
		if ((env=System.getenv(name))!=null)
			value.setValue(env.equalsIgnoreCase("true") || env.equals("1"));
	}
	
	public static void checkInteger(MutableInt value, String name) {
		String env = null;
		if ((env=System.getenv(name))!=null)
			try {
				value.setValue(Integer.valueOf(env));
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
	}
	
	public static void checkString(MutableObject<String> value, String name) {
		String env = null;
		if ((env=System.getenv(name))!=null  && !env.isEmpty())
			value.setValue(env);
	}
	
	public static void checkLogging(MutableObject<String> value, String name) {
		String env = null;
		if ((env=System.getenv(name))!=null  && !env.isEmpty())
			value.setValue(LOGGER_LEVEL_MAP.containsKey(env.toLowerCase()) ?  env : "error");
	}
}
