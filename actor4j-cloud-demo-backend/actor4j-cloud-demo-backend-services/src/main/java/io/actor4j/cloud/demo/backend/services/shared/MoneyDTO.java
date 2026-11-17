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

import io.actor4j.core.utils.DeepCopyable;

// units as long like the original data model (int64), calculations in nanos with overflow check (ArithmeticException)
public record MoneyDTO(String currency_code, long units, int nanos) implements DeepCopyable<MoneyDTO> {
	public static final long NANOS_MOD = 1_000_000_000;

	// required fields, missing ones lead to BAD_REQUEST (IllegalArgumentException)
	public MoneyDTO {
		if (currency_code==null)
			throw new IllegalArgumentException("currency_code is required");
		if (nanos<=-NANOS_MOD || nanos>=NANOS_MOD)
			throw new IllegalArgumentException("nanos must be between -999999999 and 999999999");
	}

	@Override
	public MoneyDTO deepCopy() {
		return new MoneyDTO(currency_code, units, nanos);
	}

	public long toNanos() {
		return Math.addExact(Math.multiplyExact(units, NANOS_MOD), nanos);
	}

	// units and nanos with the same sign
	public static MoneyDTO ofNanos(String currency_code, long nanos) {
		return new MoneyDTO(currency_code, nanos / NANOS_MOD, (int)(nanos % NANOS_MOD));
	}

	public static MoneyDTO sum(MoneyDTO l, MoneyDTO r) {
		if (l==null || r==null)
			return null;
		if (!l.currency_code.equalsIgnoreCase(r.currency_code))
			throw new IllegalArgumentException("Mismatching currency");

		return ofNanos(l.currency_code, Math.addExact(l.toNanos(), r.toNanos()));
	}

	public static MoneyDTO mul(MoneyDTO money, int n) {
		if (money==null)
			return null;

		return ofNanos(money.currency_code, Math.multiplyExact(money.toNanos(), n));
	}

	protected static MoneyDTO carry(String currency_code, double units, double nanos) {
		nanos += (units % 1) * NANOS_MOD;
		long result_units = (long) (Math.floor(units) + Math.floor(nanos / NANOS_MOD));
		int result_nanos = (int) (nanos % NANOS_MOD);
		 
		return new MoneyDTO(currency_code, result_units, result_nanos);
	}
	
	public static MoneyDTO convert(MoneyDTO money, String currency_code, double currencyFrom, double currencyTo) {
		MoneyDTO result = null;

		MoneyDTO euros = carry("EUR", 
			money.units / currencyFrom, 
			Math.round(money.nanos / currencyFrom)
		);
		result = carry(currency_code, 
			euros.units * currencyTo, 
			euros.nanos * currencyTo
		);
		
		return result;
	}
}
