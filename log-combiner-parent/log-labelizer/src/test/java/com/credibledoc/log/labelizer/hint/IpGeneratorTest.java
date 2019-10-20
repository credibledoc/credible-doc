package com.credibledoc.log.labelizer.hint;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class IpGeneratorTest {

	@Test
	public void test() {
		Set<String> controlOfSameness = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			String result = IpGenerator.randomIpNumbers();
			controlOfSameness.add(result);
			
			System.out.println(result);
		}
		assertTrue(controlOfSameness.size() > 1);
	}

}
