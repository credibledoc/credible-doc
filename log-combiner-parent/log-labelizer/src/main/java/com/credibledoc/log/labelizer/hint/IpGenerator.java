package com.credibledoc.log.labelizer.hint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.collections.ListUtils;

/**
 * IP Generator create random IP address. It create both forms. IPv4 and IPv6
 * In this class is 3 outputs for different purposes.
 * @author Olga Semenko
 */

public class IpGenerator {
	private static final int MAX_IP = 256;
	private static final int MAX_PORT = 65536;
	private static final List<String> SEPARATORS = new ArrayList<>(Arrays.asList(":", ".", " P ", " p ", " port ", " PORT ", " "));
	private static final List<String> NUMBER_CHARACTERS = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
	private static final List<String> CAPITAL_CHARACTERS = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F"));
	private static final List<String> LOWERCASE_CHARACTERS = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f"));
	
	@SuppressWarnings("unchecked")
    private static final List<String> DIGITS_AND_CAPITAL_CHARS = ListUtils.union(NUMBER_CHARACTERS, CAPITAL_CHARACTERS);
	
	@SuppressWarnings("unchecked")
	private static final List<String> DIGITS_AND_LOWER_CHARS = ListUtils.union(NUMBER_CHARACTERS, LOWERCASE_CHARACTERS);
	
	private static final List<List<String>> LIST_OF_FORMATS = new ArrayList<>(Arrays.asList(DIGITS_AND_CAPITAL_CHARS, DIGITS_AND_LOWER_CHARS));
	private static Random random = ThreadLocalRandom.current();
	
	private IpGenerator() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String randomIp() {
	    String result;
	    if (!random.nextBoolean()) {
	        result = randomIp4();
	    } else {
	        result = randomIp6();
	    }
        return result;
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

	/**
     * @return This method generate random port with random designation from string list. 
     * Port is random number from 0 to 65535. Designation of port is string list with
     * this types of designations:
     * <pre>
     * ":", ".", " P ", " p ", " port ", " PORT ", " "
     * </pre>
     */
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
		List<String> charList = randomFormat();
		suffix = octetMaker(charList) + "::";
		for (int i = 0; i < random.nextInt(6 + 1); i++) {
			ninethCase = octetMaker(charList) + ":";
			ninethCaseBuilder.append(ninethCase);
		}
		ninethCaseBuilder.append(suffix);

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 5; i++) {
			eighthCase = octetMaker(charList) + ":";
			eighthCaseBuilder.append(eighthCase);
		}
		prefix = octetMaker(charList) + "::";
		eighthCaseBuilder.append(prefix);
		eighthCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 4; i++) {
			seventhCase = octetMaker(charList) + ":";
			seventhCaseBuilder.append(seventhCase);
		}
		prefix = octetMaker(charList) + "::";
		seventhCaseBuilder.append(prefix);
		for (int i = 0; i < random.nextInt(1 + 1); i++) {
			seventhCase = octetMaker(charList) + ":";
			seventhCaseBuilder.append(seventhCase);
		}
		seventhCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 3; i++) {
			sixthCase = octetMaker(charList) + ":";
			sixthCaseBuilder.append(sixthCase);
		}
		prefix = octetMaker(charList) + "::";
		sixthCaseBuilder.append(prefix);
		for (int i = 0; i < random.nextInt(2 + 1); i++) {
			sixthCase = octetMaker(charList) + ":";
			sixthCaseBuilder.append(sixthCase);
		}
		sixthCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 2; i++) {
			fifthCase = octetMaker(charList) + ":";
			fifthCaseBuilder.append(fifthCase);
		}
		prefix = octetMaker(charList) + "::";
		fifthCaseBuilder.append(prefix);
		for (int i = 0; i < random.nextInt(3 + 1); i++) {
			fifthCase = octetMaker(charList) + ":";
			fifthCaseBuilder.append(fifthCase);
		}
		fifthCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 1; i++) {
			fourthCase = octetMaker(charList) + ":";
			fourthCaseBuilder.append(fourthCase);
		}
		prefix = octetMaker(charList) + "::";
		fourthCaseBuilder.append(prefix);
		for (int i = 0; i < random.nextInt(4 + 1); i++) {
			fourthCase = octetMaker(charList) + ":";
			fourthCaseBuilder.append(fourthCase);
		}
		fourthCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		prefix = octetMaker(charList) + "::";
		thirdCaseBuilder.append(prefix);
		for (int i = 0; i < random.nextInt(5 + 1); i++) {
			thirdCase = octetMaker(charList) + ":";
			thirdCaseBuilder.append(thirdCase);
		}
		thirdCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < random.nextInt(6 + 1); i++) {
			secondCase = octetMaker(charList) + ":";
			secondCaseBuilder.append(secondCase);
		}
		secondCaseBuilder.append(octetMaker(charList));

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
		List<String> charList = randomFormat();
		for (int i = 0; i < 7; i++) {
			firstCase = octetMaker(charList) + ":";
			firstCaseBuilder.append(firstCase);
		}
		firstCaseBuilder.append(octetMaker(charList));

		return firstCaseBuilder.toString();
	}
	
	/**
     * @param charList its chars will be used randomly
	 * @return This method create (from 1 to 4) random hexadecimal characters for IPv6 address. 
	 * Numbers of octet can be from range of (1-4).
	 * For example:
	 * <pre>
	 * 2, F095, 5260, 8ca
	 * </pre>
	 */
	private static String octetMaker(List<String> charList) {
		StringBuilder resultOfOctet = new StringBuilder();
		String randomCharacters;
		int range = ThreadLocalRandom.current().nextInt(1, 5);
		for (int i = 0; i < range; i++) {
			randomCharacters = charList.get(random.nextInt(charList.size()));
			resultOfOctet.append(randomCharacters);
		}

		return resultOfOctet.toString();
	}

	/**
     * @return This method choose one of formats of IPv6 address. 
     * One is format with capital characters, second is with lowercase characters.
     */
	private static List<String> randomFormat() {
		int randomIndex = random.nextInt(LIST_OF_FORMATS.size());
		return LIST_OF_FORMATS.get(randomIndex);
	}
	

}
