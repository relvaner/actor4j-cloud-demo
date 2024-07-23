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
package io.actor4j.cloud.demo.module.backend.shared;

import io.actor4j.core.utils.DeepCopyable;

public record MoneyDTO(String currency_code, int units, int nanos) implements DeepCopyable<MoneyDTO> {
	public static final long NANOS_MOD = 1_000_000_000;
	
	@Override
	public MoneyDTO deepCopy() {
		return new MoneyDTO(currency_code, units, nanos);
	}

	public static MoneyDTO sum(MoneyDTO l, MoneyDTO r) {
		if (l==null || r==null)
			return null;
		
		if (!l.currency_code.equalsIgnoreCase(r.currency_code))
			throw new RuntimeException("Mismatching currency");
		
		long units = l.units + r.units;
		long nanos = l.nanos + r.nanos;

		if ((units == 0 && nanos == 0) || (units > 0 && nanos >= 0) || (units < 0 && nanos <= 0)) {
			units += nanos / NANOS_MOD;
			nanos = nanos % NANOS_MOD;
		} 
		else {
			if (units > 0) {
				units--;
				nanos += NANOS_MOD;
			} else {
				units++;
				nanos -= NANOS_MOD;
			}
		}

		return new MoneyDTO(l.currency_code, (int)units, (int)nanos);
	}

	public static MoneyDTO mul(MoneyDTO money, int n) {
		MoneyDTO result = null;
		if (n==0)
			result = new MoneyDTO(money.currency_code, 0, 0);
		else if (n>0)
			result = money.deepCopy();
		
		if (money!=null)
			for (int i=0; i<n-1; i++) 
				result = sum(result, money);
			
		return result;
	}
	
	protected static MoneyDTO carry(String currency_code, double units, double nanos) {
		nanos += (units % 1) * NANOS_MOD;
		int result_units = (int) (Math.floor(units) + Math.floor(nanos / NANOS_MOD));
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
