package com.credibledoc.log.labelizer.hint;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class IpGeneratorTest {

	@Test
	public void testIp4() {
		Set<String> controlOfSameness = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			String result = IpGenerator.randomIp4();
			controlOfSameness.add(result);
			
		}
		assertTrue(controlOfSameness.size() > 1);
	}
	
	@Test
	public void testIp6() {
		Set<String> controlOfSameness = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			String result = IpGenerator.randomIp6();
			controlOfSameness.add(result);
			
		}
		assertTrue(controlOfSameness.size() > 1);
	}
	
	@Test
	public void testIp() {
        Set<String> controlOfSameness = new HashSet<String>();
        for (int i = 0; i < 10; i++) {
            String result = IpGenerator.randomIp();
            controlOfSameness.add(result);
            
        }
        assertTrue(controlOfSameness.size() > 1);
    }

}
