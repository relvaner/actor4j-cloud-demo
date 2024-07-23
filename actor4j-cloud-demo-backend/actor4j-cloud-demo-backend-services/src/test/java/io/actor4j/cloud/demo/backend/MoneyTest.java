package io.actor4j.cloud.demo.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.actor4j.cloud.demo.module.backend.shared.MoneyDTO;

public class MoneyTest {
	@Test
	public void test_sum() {
		MoneyDTO a = new MoneyDTO("USD", 22, 335_000_000);
		MoneyDTO b = new MoneyDTO("USD", 22, 335_000_000);
		MoneyDTO sum = MoneyDTO.sum(a, b);
		assertEquals(44, sum.units());
		assertEquals(670_000_000, sum.nanos());
		
		a = new MoneyDTO("USD", 22, 335_000_000);
		b = new MoneyDTO("USD", 22, 935_000_000);
		sum = MoneyDTO.sum(a, b);
		assertEquals(45, sum.units());
		assertEquals(270_000_000, sum.nanos());
	}
	
	@Test
	public void test_mul() {
		MoneyDTO a = new MoneyDTO("USD", 22, 335_000_000);
		MoneyDTO result = MoneyDTO.mul(a, 2);
		assertEquals(44, result.units());
		assertEquals(670_000_000, result.nanos());
		
		a = new MoneyDTO("USD", 22, 335_000_000);
		result = MoneyDTO.mul(a, 3);
		assertEquals(67, result.units());
		assertEquals(5_000_000, result.nanos());
		
		a = new MoneyDTO("USD", 22, 335_000_000);
		result = MoneyDTO.mul(a, 0);
		assertEquals(0, result.units());
		assertEquals(0, result.nanos());
	}
	
	@Test
	public void test_covert() {
		MoneyDTO a = new MoneyDTO("USD", 22, 335_000_000);
		MoneyDTO result = MoneyDTO.convert(a, "EUR", 1.1823, 1.0);
		assertEquals(18, result.units());
		assertEquals(891_144_379, result.nanos());
		
		a = new MoneyDTO("USD", 22, 335_000_000);
		result = MoneyDTO.convert(a, "EUR", 1.1823, 1.1823);
		assertEquals(22, result.units());
		assertEquals(334_999_999, result.nanos());
	}
	
	@Test
	public void test_example() {
		MoneyDTO a = new MoneyDTO("USD", 36, 450_000_000);
		MoneyDTO b = new MoneyDTO("USD", 2245, 0);
		MoneyDTO c = new MoneyDTO("USD", 8, 800_000_000);
		MoneyDTO result = c;
		result = MoneyDTO.sum(result, MoneyDTO.mul(a, 2));
		result = MoneyDTO.sum(result, b);
		assertEquals(2326, result.units());
		assertEquals(700_000_000, result.nanos());
	}
}
