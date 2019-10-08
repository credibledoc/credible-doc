package com.credibledoc.log.labelizer.hint;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class HintTest {

	@Test
	public void test() {
		String example = "6:00.k1134";
		String result = Hint.yearLabels(example);
		String expectedResult = "nnnnnyyyynyynnnnyynnnnnnnnnnnnnnnnnnnnnnnnnnnyyyynyynnnnyynnnn";
		assertEquals(expectedResult, result);
	}

}
