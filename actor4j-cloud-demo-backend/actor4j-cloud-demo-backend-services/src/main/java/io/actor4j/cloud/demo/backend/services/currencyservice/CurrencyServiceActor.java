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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.actor4j.cloud.demo.backend.services.shared.ConvertDTO;
import io.actor4j.cloud.demo.backend.services.shared.MoneyDTO;
import io.actor4j.cloud.demo.backend.utils.Utils;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.immutable.ImmutableMap;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class CurrencyServiceActor extends PodChildActor {
	public static final int CMD_CONVERT_BY_CURRENCY = PodRequestMethod.ACTION_1;
	
	protected Map<String, String> currencyDataMap;
	protected ActorId currencyData;
	
	public CurrencyServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
		
		currencyDataMap = new HashMap<>();
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
		
		currencyData = addChild(() -> new CurrencyServiceResourceActor());
		// load currency resource file
		tell(null, 0, currencyData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (message.source()==currencyData && message.value()!=null && message.value() instanceof ImmutableMap)
			currencyDataMap = ((ImmutableMap<String, String>)message.value()).get();
		else {
			Object result = null;
			int tag = PodStatus.OK;
			
			if (message.tag()==PodRequestMethod.GET_ALL)
				result = Utils.mapFrom(getCurrencies());
			else if (message.tag()==CMD_CONVERT_BY_CURRENCY && message.value()!=null && message.value() instanceof JsonObject) {
				try {
					ConvertDTO convert = ((JsonObject)message.value()).mapTo(ConvertDTO.class);
					if (convert!=null) {
						String currencyFrom = currencyDataMap.get(convert.money().currency_code());
						String currencyTo   = currencyDataMap.get(convert.currency_code());
						if (currencyFrom!=null && currencyTo!=null  && !currencyFrom.equals(currencyTo))
							result = JsonObject.mapFrom(
								MoneyDTO.convert(convert.money(), convert.currency_code(), Double.valueOf(currencyFrom), Double.valueOf(currencyTo)));
						else
							result = JsonObject.mapFrom(convert.money());
					}
					else
						tag = PodStatus.INTERNAL_SERVER_ERROR;
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					result = e.getCause().getMessage();
					tag = PodStatus.BAD_REQUEST;
				}
			}
			else
				tag = PodStatus.METHOD_NOT_ALLOWED;
			
			tell(result, tag, message.source(), message.interaction());
		}
	}
	
	protected List<String> getCurrencies() {
		List<String> result = new ArrayList<>(currencyDataMap.keySet());
		Collections.sort(result);
		
		return result;
	}
}
