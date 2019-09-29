package com.credibledoc.log.labelizer.hint;

public class YearHint {
	public static final int OLDEST_YEAR = 1980;
	public static final int ACTUAL_YEAR = 2019;

	public static void main(String[] args) {
		String input = "adjhiiuhef1990fd54sdfafgrev";
		String output = "wwwwwwwwwwwwwwddddwwwwwwwww";
		StringBuffer result = new StringBuffer();
		StringBuffer context = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			Character character = input.charAt(i);
			System.out.println(character + ":" + Character.isDigit(character));
			if (!Character.isDigit(character)) {
				result.append("w");
				context.delete(0, context.length()); // zajišťuje, že context nebude počítat s hodnotami, které byli v předešlém zkoumaném čísle
			} else {
				context.append(character);
				if (context.length() == 2 || context.length() == 4) {
					Integer contextResult = Integer.valueOf(context.toString());
					boolean isDate = isDate(contextResult);
					if (isDate == true) {
						result.append("d"); //zatím píše "d" pouze pro případ který odpovídá metodě isDate, ta je ještě k dodělání
					}
					// for cyklus pro zápis správného počtu písmen podle délky řetězce
					for (int j = 0; j < context.length(); j++) {
						result.append("x");
					}
					continue;
				} else {
					continue;
				}
			}
		}

		System.out.println(output); // Pro ověření že výstup sedí
		System.out.println(result.toString());
		System.out.println(result.toString().equals(output));
	}

	private static boolean isDate(Integer contextResult) {
		if (contextResult >= OLDEST_YEAR && contextResult <= ACTUAL_YEAR) {
			return true;
		}
		return false;
	}

}
