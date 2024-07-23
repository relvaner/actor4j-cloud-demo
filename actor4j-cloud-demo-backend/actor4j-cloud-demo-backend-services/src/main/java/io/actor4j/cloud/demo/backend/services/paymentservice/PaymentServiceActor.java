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
package io.actor4j.cloud.demo.backend.services.paymentservice;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.validator.routines.CreditCardValidator;

import io.actor4j.cloud.demo.backend.services.shared.ChargeDTO;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.Pair;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class PaymentServiceActor extends PodChildActor {
	public PaymentServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (message.tag()==PodRequestMethod.POST && message.value()!=null && message.value() instanceof JsonObject) {
			Object result = null;
			int tag = PodStatus.OK;
			
			try {
				ChargeDTO charge = ((JsonObject)message.value()).mapTo(ChargeDTO.class);
				if (charge!=null) {
					Pair<Object, Integer> pair = charge(charge);
					result = pair.a();
					tag = pair.b();
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
	
	protected Pair<Object, Integer> validate(ChargeDTO charge) {
		Object result = null;
		int tag = PodStatus.OK;
		
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		int currentMonth = calendar.get(Calendar.MONTH);
		int currentYear = calendar.get(Calendar.YEAR);
		
		CreditCardValidator ccv = new CreditCardValidator(CreditCardValidator.MASTERCARD + CreditCardValidator.VISA);
		if (!ccv.isValid(charge.credit_card().credit_card_number())) {
			result = "Credit card is not valid or accepted";
			tag = PodStatus.NOT_ACCEPTABALE;
		}
		else if ((currentYear * 12 + currentMonth) > (charge.credit_card().credit_card_expiration_year() * 12 + charge.credit_card().credit_card_expiration_month())) {
			result = "Credit card has expired";
			tag = PodStatus.NOT_ACCEPTABALE;
		}
		
		return Pair.of(result, tag);
	}
	
	protected Pair<Object, Integer> charge(ChargeDTO charge) {
		Pair<Object, Integer> result = validate(charge);
		
		if (result.b()==PodStatus.OK)
			result = Pair.of(UUID.randomUUID().toString(), PodStatus.OK);
		
		return result;
	}
}

/*
	4242424242424242 (VISA)
*/
