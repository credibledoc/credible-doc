package com.credibledoc.log.labelizer.hint;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class HintTest {

	@Test
	public void test() {
		String example = "28.2.2019 11:45:00.123 1234567654 abcde 28.2.2018 11:46:00.12465";
		String result = Hint.yearLabels(example);
		String expectedResult = "nnnnnyyyynyynnnnyynnnnnnnnnnnnnnnnnnnnnnnnnnnyyyynyynnnnyynnnnnn";
		assertEquals(expectedResult, result);
	}
	
	@Test
    @Ignore("The Hint.yearLabels(example) method should be fixed.")
	public void testLineWithSpaces() {
		String example = "2028.02.08 22:56:18 -1200 2028.02.08 22:56:18 -1200                                                 ";
		String result = Hint.yearLabels(example);
		String expectedResult = "nnnnnyynyynnnnnnnyynnnnnnnnnnnnyynyynnnnnnnyynnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn";
		assertEquals(expectedResult, result);
	}

}
