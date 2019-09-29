package com.credibledoc.log.labelizer.hint;

public class YearHint {
	public static final int OLDEST_YEAR = 1980;
	public static final int ACTUAL_YEAR = 2019;

	public static void main(String[] args) {
		String input = "adjhiiuhef54fd1990sdfafgrev";
		String output = "wwwwwwwwwwwwwwddddwwwwwwwww";
		StringBuffer result = new StringBuffer();
		StringBuffer context = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			Character character = input.charAt(i);
			System.out.println(character + ":" + Character.isDigit(character));
			if (!Character.isDigit(character)) {
				result.append("w");
			} else {
				context.append(character);
				if (context.length() < 2) {
					continue;
				} else {
					Integer contextResult = Integer.valueOf(context.toString());
					boolean isDate = isDate(contextResult);
				}
			}
		}
		
		System.out.println(output);
		System.out.println(result.toString());
		System.out.println(result.toString().equals(output));
	}

	private static boolean isDate(Integer contextResult) {
		if (contextResult <= ACTUAL_YEAR && contextResult >= OLDEST_YEAR) {
			return true;
		}
		return false;
	}

}
