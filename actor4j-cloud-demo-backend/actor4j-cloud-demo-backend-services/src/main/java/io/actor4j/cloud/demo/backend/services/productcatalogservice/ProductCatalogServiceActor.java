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
package io.actor4j.cloud.demo.backend.services.productcatalogservice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.actor4j.cloud.demo.backend.services.shared.ProductDTO;
import io.actor4j.cloud.demo.backend.utils.Utils;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodChildActor;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.pods.utils.PodStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static io.actor4j.core.logging.ActorLogger.*;

public class ProductCatalogServiceActor extends PodChildActor {
	public static final int QRY_SEARCH_BY_NAME_OR_DESCRIPTION = PodRequestMethod.ACTION_1;
	public static final int QRY_SEARCH_BY_CATEGORY = PodRequestMethod.ACTION_2;
	
	protected List<ProductDTO> products;
	protected final Map<String, List<ProductDTO>> productCatalogMap;
	protected ActorId productCatalog;
	
	public ProductCatalogServiceActor(UUID groupId, PodContext context) {
		super(groupId, context);
		
		products = new LinkedList<>();
		productCatalogMap = new HashMap<>();
	}

	@Override
	public void preStart() {
		setAlias(context.domain());
		
		productCatalog = addChild(() -> new ProductCatalogResourceActor());
		// load product catalog resource file
		tell(null, 0, productCatalog);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		logger().log(DEBUG, String.format("[%s] %s", context.domain(), message.value()));
		
		if (message.source()==productCatalog && message.value()!=null && message.value() instanceof ProductCatalogDTO)
			generateProductCatalogMap((ProductCatalogDTO)message.value());
		else {
			if (message.tag()==PodRequestMethod.GET && message.value()!=null && message.value() instanceof String) {
				JsonObject result = null;
				
				ProductDTO product = getProduct(message.valueAsString());
				if (product!=null)
					result = JsonObject.mapFrom(product);
				else
					result = new JsonObject();
				
				tell(result, PodStatus.OK, message.source(), message.interaction());
			}
			else {
				Object result = null;
				int tag = PodStatus.OK;
	
				if (message.tag()==PodRequestMethod.GET_ALL) {
					result = Utils.mapFrom(products);
				}
				else if (message.tag()==QRY_SEARCH_BY_NAME_OR_DESCRIPTION && message.value()!=null && message.value() instanceof String) {
					List<ProductDTO> list = searchByNameOrDescription(message.valueAsString());
					if (list!=null)
						result = Utils.mapFrom(list);
					else
						result = new JsonArray();
				}
				else if (message.tag()==QRY_SEARCH_BY_CATEGORY && message.value()!=null && message.value() instanceof String) {
					List<ProductDTO> list = productCatalogMap.get(message.valueAsString());
					if (list!=null)
						result = Utils.mapFrom(list);
					else
						result = new JsonArray();
				}
				else
					tag = PodStatus.METHOD_NOT_ALLOWED;
				
				tell(result, tag, message.source(), message.interaction());
			}
		}
	}
	
	protected ProductDTO getProduct(String id) {
		ProductDTO result = null;
		
		for (ProductDTO product : products)
			if (product.id().equals(id)) {
				result = product;
				break;
			}
		
		return result;
	}
	
	protected List<ProductDTO> searchByNameOrDescription(String query) {
		List<ProductDTO> result = new LinkedList<>();
		
		query = query.toLowerCase();
		for (ProductDTO product : products)
			if (product.name().toLowerCase().contains(query) || product.description().toLowerCase().contains(query))
				result.add(product);
		
		return result;
	}
	
	protected void generateProductCatalogMap(ProductCatalogDTO productCatalog) {
		products = productCatalog.products();
		
		for (ProductDTO product : productCatalog.products())
			for (String category :  product.categories()) {
				List<ProductDTO> list = productCatalogMap.get(category);
				if (list==null) {
					list = new LinkedList<>();
					list.add(product);
					productCatalogMap.put(category, list);
				}
				else
					list.add(product);
			}
	}
}
