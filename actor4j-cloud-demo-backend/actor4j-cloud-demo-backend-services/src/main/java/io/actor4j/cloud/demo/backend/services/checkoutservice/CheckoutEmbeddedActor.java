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
package io.actor4j.cloud.demo.backend.services.checkoutservice;

import static io.actor4j.core.logging.ActorLogger.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.actor4j.cloud.demo.backend.services.helper.PodAlias;
import io.actor4j.cloud.demo.backend.services.shared.CartDTO;
import io.actor4j.cloud.demo.backend.services.shared.CartItemDTO;
import io.actor4j.cloud.demo.backend.services.shared.ChargeDTO;
import io.actor4j.cloud.demo.backend.services.shared.ConvertDTO;
import io.actor4j.cloud.demo.backend.services.shared.MoneyDTO;
import io.actor4j.cloud.demo.backend.services.shared.OrderConfirmationDTO;
import io.actor4j.cloud.demo.backend.services.shared.OrderItem;
import io.actor4j.cloud.demo.backend.services.shared.OrderResultDTO;
import io.actor4j.cloud.demo.backend.services.shared.ProductDTO;
import io.actor4j.cloud.demo.backend.services.shared.ShipOrderDTO;
import io.actor4j.cloud.demo.shared.utils.User;
import io.actor4j.core.actors.behaviour.ActorBehaviour;
import io.actor4j.core.actors.behaviour.BehaviourEmbeddedActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.json.JsonObject;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;

/**
 * One order, one embedded actor. Every step sends a request and returns the behaviour that waits for the answer:
 *  (1) Fetch cart (CartService)
 *  (2) Fetch products (ProductCatalogService)
 *  (3) Convert product prices (CurrencyService)
 *  (4) Quote shipping (ShippingService)
 *  (5) Convert shipping cost (CurrencyService)
 *  (6) Total calculation
 *  (7) Charge credit card (PaymentService)
 *  (8) Empty user cart (CartService)
 *  (9) Ship order (ShippingService)
 * (10) Send confirmation email (EmailService)
 */
public class CheckoutEmbeddedActor extends BehaviourEmbeddedActor {
	protected static final long STEP_TIMEOUT = 5; // seconds per answer of a service

	protected final PodContext context;

	protected OrderDTO order;
	protected User user;
	protected ActorId podHandler;
	protected UUID interaction;

	protected CartDTO cart;
	protected OrderResult orderResult;

	public CheckoutEmbeddedActor(PodContext context) {
		super();

		this.context = context;
	}

	@Override
	protected ActorBehaviour initial() {
		return ActorBehaviour.named("NEW", this::placeOrder);
	}

	protected ActorBehaviour placeOrder(ActorMessage<?> message) {
		if (!(message instanceof PodActorMessage<?, ?, ?> podMessage))
			return ActorBehaviour.stopped(); // no request, e.g. a late answer

		podHandler = message.source();
		interaction = message.interaction();

		if (!(podMessage.user() instanceof User user) || user.principal()==null)
			return fail("_004", PodStatus.UNAUTHORIZED);
		if (message.tag()!=PodRequestMethod.POST || !(message.value() instanceof JsonObject value))
			return fail("_003", PodStatus.METHOD_NOT_ALLOWED);

		try {
			order = value.mapTo(OrderDTO.class);
		}
		catch (IllegalArgumentException e) {
			return fail("_002:"+(e.getCause()!=null ? e.getCause().getMessage() : e.getMessage()), PodStatus.BAD_REQUEST);
		}
		if (order==null)
			return fail("_001", PodStatus.INTERNAL_SERVER_ERROR);

		this.user = user;
		orderResult = new OrderResult();
		orderResult.order_id = UUID.randomUUID().toString();
		logger().log(DEBUG, String.format("[%s] placeOrder %s", PodAlias.CheckoutService, orderResult.order_id));

		return fetchCart();
	}

	// (1)
	protected ActorBehaviour fetchCart() {
		host().sendViaAlias(PodActorMessage.create(null, PodRequestMethod.GET, host().self(), null, interaction, user, "", context.domain()), PodAlias.CartService);

		return answer("FETCH_CART", PodAlias.CartService, (msg) -> {
			if (msg.tag()==PodStatus.OK && msg.value() instanceof JsonObject obj && obj.isEmpty())
				return fail("Cart is empty", PodStatus.NOT_ACCEPTABLE);

			cart = read(msg, CartDTO.class);
			return cart!=null ? fetchProduct(0) : fail("_005");
		});
	}

	// (2) one product after the other
	protected ActorBehaviour fetchProduct(int index) {
		if (index==cart.items().size())
			return convertProductPrice(0);

		CartItemDTO item = cart.items().get(index);
		request(item.product_id(), PodRequestMethod.GET, PodAlias.ProductCatalogService);

		return answer("FETCH_PRODUCTS", PodAlias.ProductCatalogService, (msg) -> {
			ProductDTO product = read(msg, ProductDTO.class);
			if (product==null)
				return fail("_008");

			orderResult.items.add(new OrderItem(item, product.price_usd()));
			return fetchProduct(index+1);
		});
	}

	// (3) one price after the other
	protected ActorBehaviour convertProductPrice(int index) {
		if (index==orderResult.items.size())
			return quoteShipping();

		OrderItem item = orderResult.items.get(index);
		request(JsonObject.create(new ConvertDTO(item.cost(), order.user_currency())), PodRequestMethod.ACTION_1, PodAlias.CurrencyService);

		return answer("CONVERT_PRODUCT_PRICES", PodAlias.CurrencyService, (msg) -> {
			MoneyDTO money = read(msg, MoneyDTO.class);
			if (money==null)
				return fail("_011");

			orderResult.items.set(index, new OrderItem(item.item(), money));
			return convertProductPrice(index+1);
		});
	}

	// (4)
	protected ActorBehaviour quoteShipping() {
		request(JsonObject.create(new ShipOrderDTO(order.address(), cart.items())), PodRequestMethod.ACTION_1, PodAlias.ShippingService);

		return answer("QUOTE_SHIPPING", PodAlias.ShippingService, (msg) -> {
			orderResult.shipping_cost = read(msg, MoneyDTO.class);
			return orderResult.shipping_cost!=null ? convertShippingCost() : fail("_014");
		});
	}

	// (5)
	protected ActorBehaviour convertShippingCost() {
		request(JsonObject.create(new ConvertDTO(orderResult.shipping_cost, order.user_currency())), PodRequestMethod.ACTION_1, PodAlias.CurrencyService);

		return answer("CONVERT_SHIPPING_COST", PodAlias.CurrencyService, (msg) -> {
			orderResult.shipping_cost = read(msg, MoneyDTO.class);
			if (orderResult.shipping_cost==null)
				return fail("_017");

			totalCalculation(); // (6)
			return chargeCreditCard();
		});
	}

	// (6)
	protected void totalCalculation() {
		orderResult.total_cost = orderResult.shipping_cost.deepCopy();
		for (OrderItem item : orderResult.items)
			orderResult.total_cost = MoneyDTO.sum(orderResult.total_cost, MoneyDTO.mul(item.cost(), item.item().quantity()));
	}

	// (7)
	protected ActorBehaviour chargeCreditCard() {
		request(JsonObject.create(new ChargeDTO(orderResult.total_cost, order.credit_card())), PodRequestMethod.POST, PodAlias.PaymentService);

		return answer("CHARGE_CREDIT_CARD", PodAlias.PaymentService, (msg) -> {
			if (msg.tag()==PodStatus.OK && msg.value() instanceof String)
				return emptyCart();
			else if (msg.tag()==PodStatus.NOT_ACCEPTABLE)
				return fail(msg.value(), msg.tag());
			else
				return fail("_020");
		});
	}

	// (8)
	protected ActorBehaviour emptyCart() {
		host().sendViaAlias(PodActorMessage.create(null, PodRequestMethod.DELETE, host().self(), null, interaction, user, "", context.domain()), PodAlias.CartService);

		return answer("EMPTY_CART", PodAlias.CartService, (msg) ->
			msg.tag()==PodStatus.ACCEPTED ? shipOrder() : fail("_021"));
	}

	// (9)
	protected ActorBehaviour shipOrder() {
		request(JsonObject.create(new ShipOrderDTO(order.address(), cart.items())), PodRequestMethod.POST, PodAlias.ShippingService);

		return answer("SHIP_ORDER", PodAlias.ShippingService, (msg) -> {
			if (msg.tag()!=PodStatus.OK || !(msg.value() instanceof String trackingId))
				return fail("_022");

			orderResult.shipping_tracking_id = trackingId;
			return sendConfirmationEmail();
		});
	}

	// (10)
	protected ActorBehaviour sendConfirmationEmail() {
		request(JsonObject.create(new OrderConfirmationDTO(order.email(), OrderResultDTO.of(orderResult))), PodRequestMethod.GET, PodAlias.EmailService);

		return answer("SEND_CONFIRMATION_EMAIL", PodAlias.EmailService, (msg) -> {
			if (msg.tag()!=PodStatus.OK)
				return fail("_023");

			host().tell(JsonObject.create(OrderResultDTO.of(orderResult)), PodStatus.OK, podHandler, interaction);
			return ActorBehaviour.stopped(); // done
		});
	}

	// ----------------------------------------------------------------------------------------------------------------

	protected void request(Object value, int tag, String service) {
		host().tell(value, tag, service, interaction, null, context.domain());
	}

	// waits for the answer of the service, other messages are ignored, without answer in time the order fails
	protected ActorBehaviour answer(String step, String service, Function<ActorMessage<?>, ActorBehaviour> onAnswer) {
		logger().log(DEBUG, String.format("[%s] %s", PodAlias.CheckoutService, step));

		return ActorBehaviour.named(step, behaviours.await(
			(msg) -> service.equalsIgnoreCase(msg.domain()),
			ActorBehaviour.ignore(),
			onAnswer,
			STEP_TIMEOUT, TimeUnit.SECONDS,
			() -> fail(String.format("_024: %s, no answer of %s", step, service), PodStatus.SERVICE_UNAVAILABLE)));
	}

	// the answer of a service as DTO, null if the service failed or the answer is invalid
	protected <T> T read(ActorMessage<?> msg, Class<T> clazz) {
		if (msg.tag()==PodStatus.OK && msg.value() instanceof JsonObject obj)
			try {
				return obj.mapTo(clazz);
			}
			catch (IllegalArgumentException e) {
				logger().log(ERROR, String.format("[%s] %s", PodAlias.CheckoutService, e.getMessage()));
			}
		return null;
	}

	protected ActorBehaviour fail(String code) {
		return fail(code, PodStatus.INTERNAL_SERVER_ERROR);
	}

	// answers the request with the error, the order (embedded actor) ends
	protected ActorBehaviour fail(Object value, int status) {
		if (value instanceof String code && code.startsWith("_"))
			value = context.domain()+code;
		host().tell(value, status, podHandler, interaction);

		return ActorBehaviour.stopped();
	}
}
