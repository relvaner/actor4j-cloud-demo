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
package io.actor4j.cloud.demo.backend.services.shared;

import java.util.LinkedList;
import java.util.List;

import io.actor4j.cloud.demo.backend.services.checkoutservice.OrderResult;

public record OrderResultDTO(String order_id, String shipping_tracking_id, MoneyDTO shipping_cost, MoneyDTO total_cost,
		Address shipping_address, List<OrderItem> items) {
	
	public static OrderResultDTO of(OrderResult result) {
		return new OrderResultDTO(result.order_id, result.shipping_tracking_id, result.shipping_cost, result.total_cost,
			result.shipping_address, new LinkedList<>(result.items));
	}
}
