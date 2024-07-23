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

public class RESTDefaultResponse {
	protected String type;
	protected int status;
	protected Pod pod;
	protected Object data;
	protected String message;
		
	public static final String SUCCESS = "success";
	public static final String NO_SUCCESS = "no_success";
	public static final String ERROR = "error";
	
	public RESTDefaultResponse() {
		this("", 0, null, null, "");
	}

	public RESTDefaultResponse(String type, int status, Pod pod, Object data, String message) {
		super();
		this.type = type;
		this.status = status;
		this.pod = pod;
		this.data = data;
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Pod getPod() {
		return pod;
	}

	public void setPod(Pod pod) {
		this.pod = pod;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "RESTDefaultResponse [type=" + type + ", status=" + status + ", pod=" + pod + ", data=" + data
				+ ", message=" + message + "]";
	}
}
