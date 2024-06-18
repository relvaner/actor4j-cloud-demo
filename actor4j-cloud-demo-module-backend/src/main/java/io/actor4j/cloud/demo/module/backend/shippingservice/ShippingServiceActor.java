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
package io.actor4j.cloud.demo.module.backend.shippingservice;

import java.util.Random;
import java.util.UUID;

import io.actor4j.cloud.demo.module.backend.shared.CartItemDTO;
import io.actor4j.cloud.demo.module.backend.shared.MoneyDTO;
import io.actor4j.cloud.demo.module.backend.shared.ShipOrderDTO;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class ShippingServiceActor extends PodChildActor {
	public static final int QUOTE = PodRequestMethod.ACTION_1;
	
	protected final Random random;
	
	public ShippingServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
		
		random = new Random();
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if ((message.tag()==PodRequestMethod.POST ||message.tag()==QUOTE) && message.value()!=null && message.value() instanceof JsonObject) {
			Object result = null;
			int tag = PodStatus.OK;
			
			try {
				ShipOrderDTO shipOrder = ((JsonObject)message.value()).mapTo(ShipOrderDTO.class);
				if (shipOrder!=null) {
					if (message.tag()==PodRequestMethod.POST) {
						result = createTrackingId(String.format("%s %s %s", 
								shipOrder.address().street_address(),
								shipOrder.address().city(),
								shipOrder.address().state()
								));
					}
					else
						result = JsonObject.mapFrom(quote(shipOrder));
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
		else
			tell(null, PodStatus.METHOD_NOT_ALLOWED, message.source(), message.interaction());
	}
	
	protected MoneyDTO quote(ShipOrderDTO shipOrder) {
		int count = 0;
		for (CartItemDTO item : shipOrder.items())
			count += item.quantity();
		
		double quote = quoteByCount(count);
		
		double integral = Math.floor(quote);
		double fractional = quote - integral;
		
		return new MoneyDTO("USD", (int)integral, (int)Math.round(fractional*100)* 10_000_000);
	}
	
	protected double quoteByCount(int count) {
		if (count==0)
			return 0;
		else
			return count + Math.pow(3, 1 + count * 0.2);
	}
	
	protected String createTrackingId(String salt) {
		return String.format("%c%c-%d%s-%d%s",
			getRandomLetterCode(),
			getRandomLetterCode(),
			salt.length(),
			getRandomNumber(3),
			salt.length()/2,
			getRandomNumber(7)
		);
	}
	
	protected int getRandomLetterCode() {
		return 65 + random.nextInt(25);
	}
	
	protected String getRandomNumber(int digits) {
		StringBuffer result = new StringBuffer();
		
		for (int i=0; i<digits; i++)
			result.append(random.nextInt(10));
		
		return result.toString();
	}
}
