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
	 * @return This method generate random IPv4 addresses and random ports.
	 * For example:
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

	/**
	 * @return This method create random IPv6 address and random port. There is a lot of options of structure of IPv6 address.
	 * This method create all of this options. It works on 9 methods, which are modified on every single possibility of IPv6.
	 * Every methods are described in java.doc.
	 * For example:
	 * <pre>
	 * ::424:b6:30404
	 * ::1DE:DD:4A6D:76:E11:E2
	 * ::7:9:aa52 p 23509
	 * 5:760:ce:8:6::8.6792
	 * 41:1AA:55:AF53:4C0::E25:35C
	 * 8:5e1c:2876:3:aa14::
	 * 3f:04:4b::4fb p 4914
	 * 9:5:5:CC1:4F:9:90:B9 port 4981
	 * 258:8d86:f3:47d::711 P 63278
	 * 2D1:8:B28:7E:DA93::2A8
	 * </pre>
	 */
	public static String randomIp6() {
		final List<String> randomCaseMethod = new ArrayList<>(Arrays.asList(firstCaseOfAddress(), secondCaseOfAddress(),
				thirdCaseOfAddress(), fourthCaseOfAddress(), fifthCaseOfAddress(), 
				sixthCaseOfAddress(), seventhCaseOfAddress(), eighthCaseOfAddress(), ninethCaseOfAddress()));
		
		String randomMethod = randomCaseMethod.get(random.nextInt(randomCaseMethod.size()));
		
		return randomMethod + generatePort();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form (1-7)xxxx::
	 * For example:
	 * <pre>
	 * b::
	 * c:a7c1:ece9:6:e7e:7c::
	 * C732:F:7590::
	 * 0143:215e:4e05::
	 * 74:c9:d::
	 * 7:21::
	 * </pre>
	 */
	private static String ninethCaseOfAddress() {
		StringBuilder ninethCaseBuilder = new StringBuilder();
		String ninethCase;
		String suffix;
		if (!random.nextBoolean()) {
			suffix = octonMakerCapital() + "::";
			for(int i = 0; i < random.nextInt(6+1); i++) {
				ninethCase = octonMakerCapital() + ":";
				ninethCaseBuilder.append(ninethCase);
			}
			ninethCaseBuilder.append(suffix);
			
		} else {
			suffix = octonMakerLowercase() + "::";
			for(int i = 0; i < random.nextInt(6+1); i++) {
				ninethCase = octonMakerLowercase() + ":";
				ninethCaseBuilder.append(ninethCase);
			}
			ninethCaseBuilder.append(suffix);
		}
		return ninethCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:xxxx:xxxx:xxxx:xxxx::xxxx
	 * For example:
	 * <pre>
	 * F49:1369:016B:15E:4EA6:0A::3D17
	 * 951E:467:50DF:4AB:A7F:E47::DE3
	 * 12b:fa30:66a9:59d:e:5::0
	 * 6:90:d2a9:5:b78:d92::28
	 * 2252:9f3f:25:a68:4:95::27
	 * </pre>
	 */
	private static String eighthCaseOfAddress() {
		StringBuilder eighthCaseBuilder = new StringBuilder();
		String eighthCase;
		String prefix;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 5; i++) {
				eighthCase = octonMakerCapital() + ":";
				eighthCaseBuilder.append(eighthCase);
			}
			prefix = octonMakerCapital() + "::";
			eighthCaseBuilder.append(prefix);
			eighthCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 5; i++) {
				eighthCase = octonMakerLowercase() + ":";
				eighthCaseBuilder.append(eighthCase);
			}
			prefix = octonMakerLowercase() + "::";
			eighthCaseBuilder.append(prefix);
			eighthCaseBuilder.append(octonMakerLowercase());
		}
		return eighthCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:xxxx:xxxx:xxxx:: + (1-2)xxxx
	 * For example:
	 * <pre>
	 * 0e9e:e8a:84:82e3:047f::e:84
	 * f:a8a:93a:27f:56::77d:2
	 * 2:30E:9:EE:79C4::E46
	 * 7c7:80c:6:9b5:62::85
	 * 78:0:576:44:D::4:954
	 * </pre>
	 */
	private static String seventhCaseOfAddress() {
		StringBuilder seventhCaseBuilder = new StringBuilder();
		String seventhCase;
		String prefix;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 4; i++) {
				seventhCase = octonMakerCapital() + ":";
				seventhCaseBuilder.append(seventhCase);
			}
			prefix = octonMakerCapital() + "::";
			seventhCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(1+1); i++) {
				seventhCase = octonMakerCapital() + ":";
				seventhCaseBuilder.append(seventhCase);
			}
			seventhCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 4; i++) {
				seventhCase = octonMakerLowercase() + ":";
				seventhCaseBuilder.append(seventhCase);
			}
			prefix = octonMakerLowercase() + "::";
			seventhCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(1+1); i++) {
				seventhCase = octonMakerLowercase() + ":";
				seventhCaseBuilder.append(seventhCase);
			}
			seventhCaseBuilder.append(octonMakerLowercase());
		}
		return seventhCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:xxxx:xxxx:: + (1-3)xxxx
	 * For example:
	 * <pre>
	 * 4ab:3:0b:70::c6fb:f5:043
	 * 98:b4a:f:7c::eb6:56
	 * 584:EABC:A1:61D::37
	 * F:A49:4:9064::7:6
	 * 4b18:5:8b:5::127:6c
	 * </pre>
	 */
	private static String sixthCaseOfAddress() {
		StringBuilder sixthCaseBuilder = new StringBuilder();
		String sixthCase;
		String prefix;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 3; i++) {
				sixthCase = octonMakerCapital() + ":";
				sixthCaseBuilder.append(sixthCase);
			}
			prefix = octonMakerCapital() + "::";
			sixthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(2+1); i++) {
				sixthCase = octonMakerCapital() + ":";
				sixthCaseBuilder.append(sixthCase);
			}
			sixthCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 3; i++) {
				sixthCase = octonMakerLowercase() + ":";
				sixthCaseBuilder.append(sixthCase);
			}
			prefix = octonMakerLowercase() + "::";
			sixthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(2+1); i++) {
				sixthCase = octonMakerLowercase() + ":";
				sixthCaseBuilder.append(sixthCase);
			}
			sixthCaseBuilder.append(octonMakerLowercase());
		}
		return sixthCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:xxxx:: + (1-4)xxxx
	 * For example:
	 * <pre>
	 * 11D6:0FF:E::EFDD:C687
	 * 9B:7:9E2::3552:02D8
	 * 43cc:96:ac::a:1a:1e
	 * 8aa:d0:7f3::370:2:9a6b
	 * 18a:c2ab:e::4:2
	 * </pre>
	 */
	private static String fifthCaseOfAddress() {
		StringBuilder fifthCaseBuilder = new StringBuilder();
		String fifthCase;
		String prefix;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 2; i++) {
				fifthCase = octonMakerCapital() + ":";
				fifthCaseBuilder.append(fifthCase);
			}
			prefix = octonMakerCapital() + "::";
			fifthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(3+1); i++) {
				fifthCase = octonMakerCapital() + ":";
				fifthCaseBuilder.append(fifthCase);
			}
			fifthCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 2; i++) {
				fifthCase = octonMakerLowercase() + ":";
				fifthCaseBuilder.append(fifthCase);
			}
			prefix = octonMakerLowercase() + "::";
			fifthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(3+1); i++) {
				fifthCase = octonMakerLowercase() + ":";
				fifthCaseBuilder.append(fifthCase);
			}
			fifthCaseBuilder.append(octonMakerLowercase());
		}
		return fifthCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:: + (1-5)xxxx
	 * For example:
	 * <pre>
	 * 3a:2::87c9:b2
	 * 8:02CE::4:D:3
	 * 80c:3834::e6:0:57:6898:19da
	 * 0:fbba::568:03
	 * 4F:03::A
	 * </pre>
	 */
	private static String fourthCaseOfAddress() {
		StringBuilder fourthCaseBuilder = new StringBuilder();
		String fourthCase;
		String prefix;
		if (!random.nextBoolean()) {
			for(int i = 0; i < 1; i++) {
				fourthCase = octonMakerCapital() + ":";
				fourthCaseBuilder.append(fourthCase);
			}
			prefix = octonMakerCapital() + "::";
			fourthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(4+1); i++) {
				fourthCase = octonMakerCapital() + ":";
				fourthCaseBuilder.append(fourthCase);
			}
			fourthCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < 1; i++) {
				fourthCase = octonMakerLowercase() + ":";
				fourthCaseBuilder.append(fourthCase);
			}
			prefix = octonMakerLowercase() + "::";
			fourthCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(4+1); i++) {
				fourthCase = octonMakerLowercase() + ":";
				fourthCaseBuilder.append(fourthCase);
			}
			fourthCaseBuilder.append(octonMakerLowercase());
		}
		return fourthCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form xxxx:: + (1-6)xxxx
	 * For example:
	 * <pre>
	 * b698::0d:e3:7d
	 * 6::FA1:7D2
	 * e82::3c:0f
	 * 94a6::672:6d:76:9e1
	 * ae::f17f:913c:00:854f:a5d9
	 * C947::D:876:E:BD35
	 * </pre>
	 */
	private static String thirdCaseOfAddress() {
		StringBuilder thirdCaseBuilder = new StringBuilder();
		String thirdCase;
		String prefix;
		if (!random.nextBoolean()) {
			prefix = octonMakerCapital() + "::";
			thirdCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(5+1); i++) {
				thirdCase = octonMakerCapital() + ":";
				thirdCaseBuilder.append(thirdCase);
			}
			thirdCaseBuilder.append(octonMakerCapital());
			
		} else {
			prefix = octonMakerLowercase() + "::";
			thirdCaseBuilder.append(prefix);
			for(int i = 0; i < random.nextInt(5+1); i++) {
				thirdCase = octonMakerLowercase() + ":";
				thirdCaseBuilder.append(thirdCase);
			}
			thirdCaseBuilder.append(octonMakerLowercase());
		}
		return thirdCaseBuilder.toString();
	}
	
	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses with shortening of octots of address where is zeros with using two colons (::).
	 * It is make random IPv6 address in form :: + (1-7)xxxx
	 * For example:
	 * <pre>
	 * ::2:ADF:FF
	 * ::35:4:C8A:F8F5:D0
	 * ::D56:09:B
	 * ::9AA:E:86:746
	 * ::405:F32A:6:A165
	 * ::FB0E
	 * </pre>
	 */
	private static String secondCaseOfAddress() {
		StringBuilder secondCaseBuilder = new StringBuilder();
		String secondCase;
		String prefix = "::";
		secondCaseBuilder.append(prefix);
		if (!random.nextBoolean()) {
			for(int i = 0; i < random.nextInt(6+1); i++) {
				secondCase = octonMakerCapital() + ":";
				secondCaseBuilder.append(secondCase);
			}
			secondCaseBuilder.append(octonMakerCapital());
			
		} else {
			for(int i = 0; i < random.nextInt(6+1); i++) {
				secondCase = octonMakerLowercase() + ":";
				secondCaseBuilder.append(secondCase);
			}
			secondCaseBuilder.append(octonMakerLowercase());
		}
		return secondCaseBuilder.toString();
	}

	/**
	 * @return This method is one of cases of IPv6 report.
	 * This is random addresses without shortening of address with using two colons (::).
	 * It is make random IPv6 address in form xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx
	 * For example:
	 * <pre>
	 * F:A54B:6D81:7670:B0:F2:3:5EC
	 * 750:f5:90f0:5cfc:1:ab:bd1b:da
	 * 7403:CD:08:A3:6D0:5C77:9A:6B9
	 * 8db:0c:30:b747:fc9f:04a:bec:9d
	 * c91:e4:3:84b:9:a9:9:a
	 * </pre>
	 */
	private static String firstCaseOfAddress() {
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
		return firstCaseBuilder.toString();
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
