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
package io.actor4j.cloud.demo.module.backend.cartservice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import io.actor4j.cloud.demo.module.backend.shared.CartDTO;
import io.actor4j.cloud.demo.module.backend.shared.CartItemDTO;
import io.actor4j.cloud.demo.module.shared.utils.User;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.data.access.utils.PodVolatileActorCacheManager;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.ActorCacheHandler;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class CartServiceActor extends PodChildActor {
	protected PodVolatileActorCacheManager<String, CartEntity> manager;
	protected ActorCacheHandler<String, CartEntity> handler;
	
	public CartServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
		
		manager = new PodVolatileActorCacheManager<>(this, "volatile-cache-"+context.domain(), groupId);
		manager.init(addChild(manager.createReplica(500, context)));
		
		handler = new ActorCacheHandler<>((msg) -> manager.get(msg));
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (!handler.match(message))
			handleRequest(message);
	}
	
	public void handleRequest(ActorMessage<?> message) {
		@SuppressWarnings("unchecked")
		User user = ((PodActorMessage<JsonObject, User, Void>)message).user();
		if (user!=null && user.principal()!=null) {
			String user_id = user.principal().getString("sub");
			
			if (message.tag()==PodRequestMethod.GET) {
				handler.define(message.interaction(), (pair) -> {
					JsonObject result = new JsonObject();
					if (pair.b()!=null)
						result = JsonObject.mapFrom(new CartDTO(pair.b().user_id(), new LinkedList<>(pair.b().items().values())));
					tell(result, PodStatus.OK, message.source(), message.interaction());
				});
				manager.get(user_id, message.interaction());	
			}
			else if (message.tag()==PodRequestMethod.POST && message.value()!=null && message.value() instanceof JsonObject) {
				try {
					CartItemDTO cartItem = ((JsonObject)message.value()).mapTo(CartItemDTO.class);
					handler.define(message.interaction(), (pair) -> {
						CartEntity cart = pair.b();
						if (cart==null)
							cart = new CartEntity(user_id, new HashMap<>());
						CartItemDTO found = cart.items().get(cartItem.product_id());
						if (found!=null)
							cart.items().put(cartItem.product_id(), 
								new CartItemDTO(cartItem.product_id(), cartItem.quantity()+found.quantity()));
						else
							cart.items().put(cartItem.product_id(), cartItem);
						manager.set(user_id, cart);
					});
					manager.get(user_id, message.interaction());
					tell(null, PodStatus.ACCEPTED, message.source(), message.interaction());
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
					tell(e.getCause().getMessage(), PodStatus.BAD_REQUEST, message.source(), message.interaction());
				}
			}
			else if (message.tag()==PodRequestMethod.DELETE) {
				manager.del(user_id);
				tell(null, PodStatus.ACCEPTED, message.source(), message.interaction());
			}
			else
				tell(null, PodStatus.METHOD_NOT_ALLOWED, message.source(), message.interaction());
		}
		else
			tell(null, PodStatus.AUTHENTICATION_REQUIRED, message.source(), message.interaction());
	}
}
