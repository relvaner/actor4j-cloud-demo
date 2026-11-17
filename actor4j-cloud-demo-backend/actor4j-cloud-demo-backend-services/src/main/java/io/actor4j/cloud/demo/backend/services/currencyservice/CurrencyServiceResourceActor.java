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
package io.actor4j.cloud.demo.backend.services.currencyservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.immutable.ImmutableMap;
import io.actor4j.core.json.ObjectMapper;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.GenericType;

public class CurrencyServiceResourceActor extends ResourceActor {
	@Override
	public void receive(ActorMessage<?> message) {
		tell(new ImmutableMap<>(loadCurrencyData()), 0, message.source());
	}
	
	public Map<String, String> loadCurrencyData() {
		Map<String, String> result = null;
		
		ObjectMapper objectMapper = ObjectMapper.create();
		try {
			result = objectMapper.mapTo(Files.readString(Path.of("data/currency_conversion.json")), new GenericType<Map<String,String>>() {});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
