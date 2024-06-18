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
package io.actor4j.cloud.demo.module.backend.functions.recomendationservice;

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

import io.actor4j.cloud.demo.module.backend.helper.PodAlias;
import io.actor4j.cloud.demo.module.backend.shared.ProductDTO;
import io.actor4j.cloud.demo.module.backend.utils.Utils;
import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.utils.Pair;
import io.vertx.core.json.JsonArray;

import static io.actor4j.core.logging.ActorLogger.*;

public class RecomendationServiceFunctionImpl {
	protected static final int MAX_RECOMENDATIONS = 5;
	
	protected ActorRef host;
	protected PodContext context;
	
	protected Map<UUID, Function<JsonArray, Pair<Object, Integer>>> handlerMap;
	protected final Random random;
	
	public RecomendationServiceFunctionImpl(ActorRef host, PodContext context) {
		super();
		this.host = host;
		this.context = context;
		
		random = new Random();
		handlerMap = new HashMap<>();
	}

	public Pair<Object, Integer> handle(ActorMessage<?> message) {
		Pair<Object, Integer> result = null;
		
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		Function<JsonArray, Pair<Object, Integer>> handler = handlerMap.get(message.interaction());
		if (handler!=null && message.value()!=null && message.value() instanceof JsonArray) {
			result = handler.apply((JsonArray)message.value());
			handlerMap.remove(message.interaction());
		}
		else if (message.tag()==PodRequestMethod.GET_ALL && message.value()!=null && message.value() instanceof JsonArray) {
			handlerMap.put(message.interaction(), (array) -> {
				return Pair.of(generateRecomendations((JsonArray)message.value(), array), PodStatus.OK);
			});
			host.tell(null, PodRequestMethod.GET_ALL, PodAlias.ProductCatalogService, message.interaction(), null, context.domain());
		}
		else
			result = Pair.of(null, PodStatus.METHOD_NOT_ALLOWED);
		
		return result;
	}
	
	protected JsonArray generateRecomendations(JsonArray product_id_arr, JsonArray products_arr) {
		Set<String> cat_productIds = new HashSet<>(Utils.mapTo(product_id_arr, String.class));
		List<ProductDTO> products = Utils.mapTo(products_arr, ProductDTO.class);
		Set<String> productIds = new HashSet<>();
		for (ProductDTO product : products)
			productIds.add(product.id());
		productIds.removeAll(cat_productIds);
		
		List<String> result = random.ints(MAX_RECOMENDATIONS, 0, productIds.size()).mapToObj(i -> new ArrayList<>(productIds).get(i)).collect(Collectors.toList());
		
		return Utils.mapFrom(result);
	}
}
