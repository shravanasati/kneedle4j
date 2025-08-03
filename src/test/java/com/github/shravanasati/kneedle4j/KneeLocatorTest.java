package com.github.shravanasati.kneedle4j;

import org.junit.Test;
import static org.junit.Assert.*;

public class KneeLocatorTest {
	@Test
	public void testGreet() {
		String expected = "Hello, KneeLocator!";
		String actual = KneeLocator.greet();
		assertEquals(expected, actual);
	}
}
