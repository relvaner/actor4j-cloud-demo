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
package io.actor4j.cloud.demo.backend.services.recomendationservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.actor4j.cloud.demo.backend.services.helper.PodAlias;
import io.actor4j.cloud.demo.backend.services.shared.ProductDTO;
import io.actor4j.cloud.demo.backend.utils.Utils;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.ActorMessageHandler;
import io.vertx.core.json.JsonArray;

import static io.actor4j.core.logging.ActorLogger.*;

public class RecomendationServiceActor extends PodChildActor {
	protected static final int MAX_RECOMENDATIONS = 4;
	
	protected ActorMessageHandler<JsonArray> handler;
	protected final Random random;
	
	public RecomendationServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
		
		random = new Random();
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
		
		handler = new ActorMessageHandler<>(JsonArray.class);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (!handler.match(message)) {
			if (message.tag()==PodRequestMethod.GET_ALL && message.value()!=null && message.value() instanceof JsonArray) {
				handler.define(message.interaction(),  (array, msg) -> {
					tell(generateRecomendations((JsonArray)message.value(), array), PodStatus.OK, message.source(), message.interaction());
				});
				tell(null, PodRequestMethod.GET_ALL, PodAlias.ProductCatalogService, message.interaction(), null, context.domain());
			}
			else
				tell(null, PodStatus.METHOD_NOT_ALLOWED, message.source(), message.interaction());
		}
	}
	
	protected JsonArray generateRecomendations(JsonArray product_id_arr, JsonArray products_arr) {
		Set<String> cat_productIds = new HashSet<>(Utils.mapTo(product_id_arr, String.class));
		List<ProductDTO> products = Utils.mapTo(products_arr, ProductDTO.class);
		Set<String> productIds = new HashSet<>();
		for (ProductDTO product : products)
			productIds.add(product.id());
		productIds.removeAll(cat_productIds);
		List<String> productIdsList = new ArrayList<>(productIds);
		
		int limit = MAX_RECOMENDATIONS;
		if (productIdsList.size()<MAX_RECOMENDATIONS)
			limit = productIdsList.size();
		
		if (limit>0)
			return Utils.mapFrom(random.ints(0, productIdsList.size()).mapToObj(i -> productIdsList.get(i)).distinct().limit(limit).collect(Collectors.toList()));
		else
			return new JsonArray();
	}
}
