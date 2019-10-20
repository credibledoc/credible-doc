package com.credibledoc.log.labelizer.hint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class IpGenerator {
	public static final int MAX_IP = 256;
	public static final int MAX_PORT = 65536;
	private static final List<String> SEPARATORS = new ArrayList<>(Arrays.asList(":", ".", " P ", " p ", " port ", " PORT ", " "));
	private static Random random = ThreadLocalRandom.current();
	
	private IpGenerator() {
		throw new IllegalStateException("Utility class");
	}

	public static String randomIpNumbers() {
		return
				random.nextInt(MAX_IP) + "." +
				random.nextInt(MAX_IP) + "." +
				random.nextInt(MAX_IP) + "." +
				random.nextInt(MAX_IP) + generatePort();
	}

	private static String generatePort() {
		if (!random.nextBoolean()) {
			return "";
		}
		
		String randomSeparator = SEPARATORS.get(random.nextInt(SEPARATORS.size()));
		return randomSeparator + random.nextInt(MAX_PORT);
	}

}
