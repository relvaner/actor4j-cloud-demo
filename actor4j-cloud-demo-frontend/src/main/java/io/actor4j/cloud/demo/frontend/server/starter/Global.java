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
package io.actor4j.cloud.demo.frontend.server.starter;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class Global {
	public static final MutableBoolean ACTOR4J_CLOUD_DOCKER = new MutableBoolean(false);
	public static final MutableObject<String> ACTOR4J_CLOUD_CONTEXT_PATH = new MutableObject<String>("");
	
	public static final MutableObject<String> ACTOR4J_CLOUD_VENDOR = new MutableObject<String>("actor4j-cloud");
	public static final MutableInt ACTOR4J_CLOUD_PORT = new MutableInt(8080);
	
	public static void config() {
		checkBoolean(ACTOR4J_CLOUD_DOCKER, "ACTOR4J_CLOUD_DOCKER");
		checkString(ACTOR4J_CLOUD_CONTEXT_PATH, "ACTOR4J_CLOUD_CONTEXT_PATH");
		
		checkString(ACTOR4J_CLOUD_VENDOR, "ACTOR4J_CLOUD_VENDOR");
		checkInteger(ACTOR4J_CLOUD_PORT, "ACTOR4J_CLOUD_PORT");
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
}
