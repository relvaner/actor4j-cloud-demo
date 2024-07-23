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
import io.actor4j.cloud.demo.module.shared.utils.User;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.runtime.embedded.InternalEmbeddedActorCell;
import io.vertx.core.json.JsonObject;

public class CheckoutEmbeddedActor extends EmbeddedActor {
	protected PodContext context;
	
	protected int state;
	
	protected OrderResult orderResult;
	
	protected CartDTO cart;
	
	public CheckoutEmbeddedActor(PodContext context) {
		super();
		
		this.context = context;
	}

	@Override
	public boolean receive(ActorMessage<?> message) {
		boolean done = true;
		/*
		if (!(message instanceof PodActorMessage)) {
			logger().log(ERROR, String.format("[%s] Call not allowed", "CheckoutService"));
			return false;
		}
		*/
		
		@SuppressWarnings("unchecked")
		User user = ((PodActorMessage<JsonObject, User, Void>)message).user();
		if (user!=null && user.principal()!=null) {
			if (message.tag()==PodRequestMethod.POST && message.value()!=null && message.value() instanceof JsonObject) {
				try {
					OrderDTO order = ((JsonObject)message.value()).mapTo(OrderDTO.class);
					if (order!=null) {
						done = false;
						placeOrder(order, user, message.source(), message.interaction());
					}
					else
						host().tell(context.domain()+"_001", PodStatus.INTERNAL_SERVER_ERROR, message.source(), message.interaction());
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					host().tell(context.domain()+"_002:"+e.getCause().getMessage(), PodStatus.BAD_REQUEST, message.source(), message.interaction());
				}
			}
			else
				host().tell(context.domain()+"_003", PodStatus.METHOD_NOT_ALLOWED, message.source(), message.interaction());
		}
		else
			host().tell(context.domain()+"_004", PodStatus.AUTHENTICATION_REQUIRED, message.source(), message.interaction());
		
		return done;
	}
	
	/**
	 *  (1) -> Fetch cart (CartService)
	 *  (2) -> Fetch products (ProductCatalogService)
	 *  (3) -> Converts product prices (CurrencyService)
	 *  (4) -> Quote shipping (ShippingService)
	 *  (5) -> Convert shipping cost (CurrencyService)
	 *  (6) -> Total calculation
	 *  (7) -> Charge credit card (PaymentService)
	 *  (8) -> Empty user cart (CartService)
	 *  (9) -> Ship order (ShippingService)
	 * (10) -> Send confirmation email (EmailService)
	 */
	protected void placeOrder(OrderDTO order, User user, UUID podHandler, UUID interaction) {
		logger().log(DEBUG, String.format("[%s] placeOrder", PodAlias.CheckoutService));
		
		orderResult = new OrderResult();
		orderResult.order_id = UUID.randomUUID().toString();
		
		become((msg) -> {
			boolean done = false;
			
			logger().log(DEBUG, String.format("[%s] state: %s", PodAlias.CheckoutService, state));
			
			state++;
			switch (state) {
				case 1: 
					fetchProducts(podHandler, interaction);
					break;
				case 2: 
					convertProductPrices(order, podHandler, interaction);
					break;
				case 3: 
					quoteShipping(order, podHandler, interaction);
					break;
				case 4:
					convertShippingCost(order, podHandler, interaction);
					break;
				case 5:
					totalCalculation();
					chargeCreditCard(order, podHandler, interaction);
					break;
				case 6:
					emptyCart(user, podHandler, interaction);
					break;
				case 7:
					shipOrder(order, podHandler, interaction);
					break;
				case 8:
					sendConfirmationEmail(order, podHandler, interaction);
					break;
				default: {
					done = true;
					host().tell(JsonObject.mapFrom(OrderResultDTO.of(orderResult)), PodStatus.OK, podHandler, interaction);
				}
			}
			
			return done;
		});
		fetchCart(user, podHandler, interaction);
	}
	
	protected void next() {
		unbecome();
		((InternalEmbeddedActorCell)cell).fireActiveBehaviour(null, (expectedSize) -> expectedSize==1);
	}
	
	protected void fetchCart(User user, UUID podHandler, UUID interaction) {
		host().sendViaAlias(PodActorMessage.create(null, PodRequestMethod.GET, host().self(), null, interaction, user, "", context.domain()), PodAlias.CartService);
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.CartService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof JsonObject) {
				if (((JsonObject)msg.value()).isEmpty()) {
					host().tell("Cart is empty", PodStatus.NOT_ACCEPTABALE, podHandler, interaction);
					return true;
				}
				
				try {
					cart = ((JsonObject)msg.value()).mapTo(CartDTO.class);
					if (cart!=null)
						done = false;
					else
						host().tell(context.domain()+"_005", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					host().tell(context.domain()+"_006:"+e.getCause().getMessage(), PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
			}
			else 
				host().tell(context.domain()+"_007", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void fetchProducts(UUID podHandler, UUID interaction) {
		if (cart.items().size()>0)
			host().tell(cart.items().get(0).product_id(), PodRequestMethod.GET, PodAlias.ProductCatalogService, interaction, null, context.domain());
		
		for (int i=cart.items().size()-1; i>=0; i--) {
			CartItemDTO item = cart.items().get(i);
			
			int index = i;
			await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.ProductCatalogService), (msg) -> {
				boolean done = true;
				
				if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof JsonObject) {
					try {
						ProductDTO product = ((JsonObject)msg.value()).mapTo(ProductDTO.class);
						if (product!=null) {
							done = false;
							orderResult.items.add(new OrderItem(item, product.price_usd()));
						}
						else
							host().tell(context.domain()+"e_008", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
						host().tell(context.domain()+"_009:"+e.getCause().getMessage(), PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
					}
				}
				else {
					host().tell(context.domain()+"_010", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
					/*
					// TODO fail() -> removeEmbeddedChild()
					host.removeEmbeddedChild(this);
					*/
				}
				
				if (index<cart.items().size()-1)
					host().tell(cart.items().get(index+1).product_id(), PodRequestMethod.GET, PodAlias.ProductCatalogService, interaction, null, context.domain());
				next();
				
				return done;
			}, false);
		}
	}
	
	protected void convertProductPrices(OrderDTO order, UUID podHandler, UUID interaction) {
		if (orderResult.items.size()>0)
			host().tell(JsonObject.mapFrom(new ConvertDTO(orderResult.items.get(0).cost(), order.user_currency())), PodRequestMethod.ACTION_1, PodAlias.CurrencyService, interaction, null, context.domain());
		
		for (int i=orderResult.items.size()-1; i>=0; i--) {
			OrderItem item = orderResult.items.get(i);
			
			int index = i;
			await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.CurrencyService), (msg) -> {
				boolean done = true;
				
				if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof JsonObject) {
					try {
						MoneyDTO money = ((JsonObject)msg.value()).mapTo(MoneyDTO.class);
						if (money!=null) {
							done = false;
							orderResult.items.set(index, new OrderItem(item.item(), money));
						}
						else
							host().tell(context.domain()+"_011", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
						host().tell(context.domain()+"_012"+e.getCause().getMessage(), PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
					}
				}
				else 
					host().tell(context.domain()+"_013", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				
				if (index<orderResult.items.size()-1)
					host().tell(JsonObject.mapFrom(new ConvertDTO( orderResult.items.get(index+1).cost(), order.user_currency())), PodRequestMethod.ACTION_1, PodAlias.CurrencyService, interaction, null, context.domain());
				next();
				
				return done;
			}, false);
		}
	}
	
	protected void quoteShipping(OrderDTO order, UUID podHandler, UUID interaction) {
		host().tell(JsonObject.mapFrom(new ShipOrderDTO(order.address(), cart.items())), PodRequestMethod.ACTION_1, PodAlias.ShippingService, interaction, null, context.domain());
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.ShippingService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof JsonObject) {
				try {
					MoneyDTO money = ((JsonObject)msg.value()).mapTo(MoneyDTO.class);
					if (money!=null) {
						done = false;
						orderResult.shipping_cost = money;
					}
					else
						host().tell(context.domain()+"_014", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					host().tell(context.domain()+"_015:"+e.getCause().getMessage(), PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
			}
			else 
				host().tell(context.domain()+"_016", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void convertShippingCost(OrderDTO order, UUID podHandler, UUID interaction) {
		host().tell(JsonObject.mapFrom(new ConvertDTO(orderResult.shipping_cost, order.user_currency())), PodRequestMethod.ACTION_1, PodAlias.CurrencyService, interaction, null, context.domain());
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.CurrencyService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof JsonObject) {
				try {
					MoneyDTO money = ((JsonObject)msg.value()).mapTo(MoneyDTO.class);
					if (money!=null) {
						done = false;
						orderResult.shipping_cost = money;
					}
					else
						host().tell(context.domain()+"_017", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					host().tell(context.domain()+"_018:"+e.getCause().getMessage(), PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
				}
			}
			else 
				host().tell(context.domain()+"_019", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void totalCalculation() {
		orderResult.total_cost = orderResult.shipping_cost.deepCopy();
		for (OrderItem item : orderResult.items)
			orderResult.total_cost = MoneyDTO.sum(orderResult.total_cost, MoneyDTO.mul(item.cost(), item.item().quantity()));
	}
	
	protected void chargeCreditCard(OrderDTO order, UUID podHandler, UUID interaction) {
		host().tell(JsonObject.mapFrom(new ChargeDTO(orderResult.total_cost, order.credit_card())), PodRequestMethod.POST, PodAlias.PaymentService, interaction, null, context.domain());
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.PaymentService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof String) {
				done = false;
				// String transaction_id = msg.valueAsString();
			}
			else if (msg.tag()==PodStatus.NOT_ACCEPTABALE)
				host().tell(msg.value(), msg.tag(), podHandler, interaction);
			else 
				host().tell(context.domain()+"_020", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void emptyCart(User user, UUID podHandler, UUID interaction) {
		host().sendViaAlias(PodActorMessage.create(null, PodRequestMethod.DELETE, host().self(), null, interaction, user, "", context.domain()), PodAlias.CartService);
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.CartService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.ACCEPTED)
				done = false;
			else 
				host().tell(context.domain()+"_021", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void shipOrder(OrderDTO order, UUID podHandler, UUID interaction) {
		host().tell(JsonObject.mapFrom(new ShipOrderDTO(order.address(), cart.items())), PodRequestMethod.POST, PodAlias.ShippingService, interaction, null, context.domain());
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.ShippingService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK && msg.value()!=null && msg.value() instanceof String) {
				done = false;
				orderResult.shipping_tracking_id = msg.valueAsString();
			}
			else 
				host().tell(context.domain()+"_022", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
	
	protected void sendConfirmationEmail(OrderDTO order, UUID podHandler, UUID interaction) {
		host().tell(JsonObject.mapFrom(new OrderConfirmationDTO(order.email(), OrderResultDTO.of(orderResult))), PodRequestMethod.GET, PodAlias.EmailService, interaction, null, context.domain());
		
		await((msg) -> msg.domain()!=null && msg.domain().equalsIgnoreCase(PodAlias.EmailService), (msg) -> {
			boolean done = true;
			
			if (msg.tag()==PodStatus.OK)
				done = false;
			else
				host().tell(context.domain()+"_023", PodStatus.INTERNAL_SERVER_ERROR, podHandler, interaction);
			
			next();
			
			return done;
		}, false);
	}
}
