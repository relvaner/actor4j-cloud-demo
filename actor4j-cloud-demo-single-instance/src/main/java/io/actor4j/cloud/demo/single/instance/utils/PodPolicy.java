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

import java.util.Map;
import java.util.Set;

import io.actor4j.cloud.demo.shared.utils.User;

public class PodPolicy {
	public final Map<String, Set<Integer>> authorizedMethods;
	
	private PodPolicy(Map<String, Set<Integer>> authorizedMethods) {
		super();
		this.authorizedMethods = authorizedMethods;
	}
	
	public static PodPolicy allow(Map<String, Set<Integer>> authorizedMethods) {
		return new PodPolicy(authorizedMethods);
	}
	
	public static PodPolicy allowAll() {
		return allow(Map.of(
			User.ROLE_PUBLIC, Set.of(Integer.MAX_VALUE)
		));
	}
	
	public static PodPolicy denyAll() {
		return new PodPolicy(Map.of());
	}
	
	public static boolean allowedMethod(PodPolicy podPolicy, String userAuthority, int podRequestMethod) {
		boolean result = false;
		
		if (podPolicy!=null) {
			Set<Integer> podRequestMethods = podPolicy.authorizedMethods.get(userAuthority);
			
			if (podRequestMethods!=null) {
				result = podRequestMethods.contains(podRequestMethod);
				if (!result) {	
					if (podRequestMethods.size()==1 && podRequestMethods.contains(Integer.MAX_VALUE))
						result = true;
				}
			};
			
			if (!result) {
				// Checking if existing User.ROLE_PUBLIC
				podRequestMethods = podPolicy.authorizedMethods.get(User.ROLE_PUBLIC);
				if (podRequestMethods!=null) {
					result = podRequestMethods.contains(podRequestMethod);
					if (!result && podRequestMethods.size()==1)
						if (podRequestMethods.contains(Integer.MAX_VALUE))
							result = true;
				}
			}
		}
		
		return result;
	}
}
