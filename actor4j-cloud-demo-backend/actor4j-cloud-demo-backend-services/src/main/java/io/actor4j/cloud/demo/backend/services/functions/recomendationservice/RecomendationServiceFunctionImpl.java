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
package io.actor4j.cloud.demo.backend.services.functions.recomendationservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.actor4j.cloud.demo.backend.services.helper.PodAlias;
import io.actor4j.cloud.demo.backend.services.shared.ProductDTO;
import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.Reply;
import io.actor4j.core.json.JsonArray;

import static io.actor4j.core.logging.ActorLogger.*;

public class RecomendationServiceFunctionImpl {
	protected static final int MAX_RECOMENDATIONS = 5;
	
	protected ActorRef host;
	protected PodContext context;
	
	protected Map<UUID, Function<JsonArray, Reply>> handlerMap;
	protected final Random random;
	
	public RecomendationServiceFunctionImpl(ActorRef host, PodContext context) {
		super();
		this.host = host;
		this.context = context;
		
		random = new Random();
		handlerMap = new HashMap<>();
	}

	public Reply handle(ActorMessage<?> message, UUID interaction) {
		Reply result = null;
		
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		// the replies carry the interaction, so the proxy answers the pending request and not the product catalog
		Function<JsonArray, Reply> handler = handlerMap.remove(interaction);
		if (handler!=null) {
			if (message.value() instanceof JsonArray array)
				result = handler.apply(array);
			else
				result = Reply.failure(message.value(), message.tag()>=PodStatus.BAD_REQUEST ? message.tag() : PodStatus.INTERNAL_SERVER_ERROR, interaction);
		}
		else if (message.tag()==PodRequestMethod.GET_ALL && message.value() instanceof JsonArray product_ids) {
			handlerMap.put(interaction, (array) -> {
				return Reply.of(generateRecomendations(product_ids, array), PodStatus.OK, interaction);
			});
			host.tell(null, PodRequestMethod.GET_ALL, PodAlias.ProductCatalogService, interaction, null, context.domain());
			result = Reply.pending();
		}
		else if (message.tag()==PodRequestMethod.GET_ALL)
			result = Reply.failure("Array of product ids expected", PodStatus.BAD_REQUEST);
		else
			result = Reply.failure(null, PodStatus.METHOD_NOT_ALLOWED);
		
		return result;
	}
	
	protected JsonArray generateRecomendations(JsonArray product_id_arr, JsonArray products_arr) {
		Set<String> cat_productIds = new HashSet<>(product_id_arr.mapTo(String.class));
		List<ProductDTO> products = products_arr.mapTo(ProductDTO.class);
		Set<String> productIds = new HashSet<>();
		for (ProductDTO product : products)
			productIds.add(product.id());
		productIds.removeAll(cat_productIds);
		
		List<String> result = random.ints(MAX_RECOMENDATIONS, 0, productIds.size()).mapToObj(i -> new ArrayList<>(productIds).get(i)).collect(Collectors.toList());
		
		return JsonArray.create(result);
	}
}
