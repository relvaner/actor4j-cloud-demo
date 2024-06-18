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
package io.actor4j.cloud.demo.module.backend.checkoutservice;

import java.util.LinkedList;
import java.util.List;

import io.actor4j.cloud.demo.module.backend.shared.Address;
import io.actor4j.cloud.demo.module.backend.shared.MoneyDTO;
import io.actor4j.cloud.demo.module.backend.shared.OrderItem;

public class OrderResult {
	public String order_id;
    public String shipping_tracking_id;
    public MoneyDTO shipping_cost;
    public MoneyDTO total_cost;
    public Address shipping_address;
    public List<OrderItem> items;
    
    public OrderResult() {
		super();
		this.items = new LinkedList<>();
	}

	public OrderResult(String order_id, String shipping_tracking_id, MoneyDTO shipping_cost, MoneyDTO total_cost,
			Address shipping_address, List<OrderItem> items) {
		super();
		this.order_id = order_id;
		this.shipping_tracking_id = shipping_tracking_id;
		this.shipping_cost = shipping_cost;
		this.total_cost = total_cost;
		this.shipping_address = shipping_address;
		this.items = items;
	}

	@Override
	public String toString() {
		return "OrderResult [order_id=" + order_id + ", shipping_tracking_id=" + shipping_tracking_id
				+ ", shipping_cost=" + shipping_cost + ", total_cost=" + total_cost + ", shipping_address="
				+ shipping_address + ", items=" + items + "]";
	}
}
