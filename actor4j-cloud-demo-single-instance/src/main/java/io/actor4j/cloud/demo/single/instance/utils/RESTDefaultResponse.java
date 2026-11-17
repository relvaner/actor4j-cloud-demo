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

// answer of the REST API: type, (HTTP) status, pod answer (null if no pod answered), data and message
public record RESTDefaultResponse(String type, int status, Pod pod, Object data, String message) {
	public static final String SUCCESS = "success";
	public static final String NO_SUCCESS = "no_success";
	public static final String ERROR = "error";
}
