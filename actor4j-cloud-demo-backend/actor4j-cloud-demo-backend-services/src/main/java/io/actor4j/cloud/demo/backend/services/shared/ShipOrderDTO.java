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

import java.util.List;

public record ShipOrderDTO(Address address, List<CartItemDTO> items) {
	// required fields, missing ones lead to BAD_REQUEST (IllegalArgumentException)
	public ShipOrderDTO {
		if (address==null)
			throw new IllegalArgumentException("address is required");
		if (items==null)
			throw new IllegalArgumentException("items is required");
		for (CartItemDTO item : items)
			if (item==null)
				throw new IllegalArgumentException("items must not contain null");
	}
}
