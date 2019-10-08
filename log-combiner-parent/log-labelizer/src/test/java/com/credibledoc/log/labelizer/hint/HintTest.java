package com.credibledoc.log.labelizer.hint;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class HintTest {

	@Test
	public void test() {
<<<<<<< HEAD
		String example = "6:00.k1995676865kjohiz1995";
=======
		String example = "28.2.2019 11:45:00.123 1234567654 abcde 28.2.2018 11:46:00.193";
>>>>>>> 6557c90af50f4fbf6c6836bfc19e9a02c7cc1428
		String result = Hint.yearLabels(example);
		String expectedResult = "nnnnnyyyynyynnnnyynnnnnnnnnnnnnnnnnnnnnnnnnnnyyyynyynnnnyynnnn";
		assertEquals(expectedResult, result);
	}

}
