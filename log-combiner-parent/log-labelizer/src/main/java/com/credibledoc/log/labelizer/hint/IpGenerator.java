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
	private static final List<String> CAPITAL_CHARACTERS = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"));
	private static final List<String> LOWERCASE_CHARACTERS = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"));
	private static Random random = ThreadLocalRandom.current();
	
	private IpGenerator() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @return For example:
	 * <pre>
		24.176.247.224 P 11239
		21.75.22.118
		106.32.177.77 PORT 41373
		36.124.220.87:30405
		13.161.54.195
		132.66.113.153 port 2101
		46.159.122.154
		41.26.163.243.22901
		131.36.65.252
	 * </pre>
	 */
	public static String randomIp4() {
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

	public static String randomIp6() {
		StringBuilder firstCaseBuilder = firstCaseOfAddress();
		return firstCaseBuilder.toString();
	}

	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses without shortening of address with using two colons (::).
	 * For example:
	 * <pre>
	 * F:A54B:6D81:7670:B0:F2:3:5EC
	 * 750:f5:90f0:5cfc:1:ab:bd1b:da
	 * 7403:CD:08:A3:6D0:5C77:9A:6B9
	 * 8db:0c:30:b747:fc9f:04a:bec:9d
	 * c91:e4:3:84b:9:a9:9:a
	 * </pre>
	 */
	private static StringBuilder firstCaseOfAddress() {
		StringBuilder firstCaseBuilder = new StringBuilder();
		String firstCase;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 7; i++) {
				firstCase = octonMakerCapital() + ":";
				firstCaseBuilder.append(firstCase);
			}
			firstCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 7; i++) {
				firstCase = octonMakerLowercase() + ":";
				firstCaseBuilder.append(firstCase);
			}
			firstCaseBuilder.append(octonMakerLowercase());
		}
		return firstCaseBuilder;
	}
	
	/**
	 * @return This method create (from 1 to 4) random hexadecimal numbers with CAPITAL CHARACTERS. 
	 * This numbers are octets of IPv6 address.
	 * Numbers of octets can be from ranges (0-9; A-F). 
	 * Numbers of octet can be from range of (1-4). 
	 * For example:
	 * <pre>
	 * BA, 436, 3F79, 8CE0, FA, AAA6
	 * </pre>
	 */
	private static String octonMakerCapital() {
		StringBuilder resultOfOctetCapital = new StringBuilder();
		String randomCharacters = null;
		int range = ThreadLocalRandom.current().nextInt(1, 5);
			for (int i = 0; i < range; i++) {
				randomCharacters = CAPITAL_CHARACTERS.get(random.nextInt(CAPITAL_CHARACTERS.size()));
				resultOfOctetCapital.append(randomCharacters);
			}

		return resultOfOctetCapital.toString();
	}
	
	/**
	 * @return This method create (from 1 to 4) random hexadecimal numbers with LOWERCASE CHARACTERS. 
	 * This numbers are octets of IPv6 address.
	 * Numbers of octets can be from ranges (0-9; a-f). 
	 * Numbers of octet can be from range of (1-4). 
	 * For example:
	 * <pre>
	 * a1f0, c0f, 2b3, 321, b, 85
	 * </pre>
	 */
	private static String octonMakerLowercase() {
		StringBuilder resultOfOctetLowercase = new StringBuilder();
		String randomCharacters = null;
		int range = ThreadLocalRandom.current().nextInt(1, 5);
			for (int i = 0; i < range; i++) {
				randomCharacters = LOWERCASE_CHARACTERS.get(random.nextInt(LOWERCASE_CHARACTERS.size()));
				resultOfOctetLowercase.append(randomCharacters);
			}

		return resultOfOctetLowercase.toString();
	}
	

}
