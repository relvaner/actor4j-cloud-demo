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
package io.actor4j.cloud.demo.module.backend.productcatalogservice;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.actor4j.cloud.demo.module.backend.shared.ProductDTO;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.messages.ActorMessage;

public class ProductCatalogResourceActor extends ResourceActor {
	@Override
	public void receive(ActorMessage<?> message) {
		tell(loadProductCatalog(), 0, message.source());
	}
	
	public ProductCatalogDTO loadProductCatalog() {
		ProductCatalogDTO result = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			result = objectMapper.readValue(new File("data/products.json"), ProductCatalogDTO.class);

			if (result!=null) {
				String contextPath = getContextPath();
				if (!contextPath.isEmpty()) {
					ProductCatalogDTO buffer = new ProductCatalogDTO(new LinkedList<>());
					for (ProductDTO product : result.products())
						buffer.products().add(product.shallowCopy(contextPath + product.picture()));
					result = buffer;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public String getContextPath() {
		String env = System.getenv("ACTOR4J_CLOUD_CONTEXT_PATH_FRONTEND");
		
		return env!=null ? env : "";
	}
}
