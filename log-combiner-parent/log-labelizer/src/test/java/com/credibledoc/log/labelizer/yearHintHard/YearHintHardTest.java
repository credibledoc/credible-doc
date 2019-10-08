package com.credibledoc.log.labelizer.yearHintHard;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import com.credibledoc.log.labelizer.hint.yearHintHard;

public class YearHintHardTest {

	@Test
	public void test() {
		String example = "2019:1425...1998338";
		String result = yearHintHard.yearHintHard(example);
		String expectedResult = "yyyynyynnnnnyyyyynn";
		System.out.println(result);
		assertEquals(expectedResult, result);
	}

}
