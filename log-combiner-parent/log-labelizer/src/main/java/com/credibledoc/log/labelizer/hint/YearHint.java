package com.credibledoc.log.labelizer.hint;

public class YearHint {
	public static final int OLDEST_YEAR = 1980;
	public static final int ACTUAL_YEAR = 2019;
	public static final int SHORT_OLD_YEAR = 80;
	public static final int SHORT_ACTUAL_YEAR = 19;
	public static final int SHORT_ZERO_YEAR = 00; //year 2000 in short version
	public static final int SHORT_HELPFULL_YEAR = 100; //year 2000 in helpfull version for nineteens years

	public static void main(String[] args) {
		String input =  "80adjhiiuhef1990fd99sdfafgrev1911";
		String output = "ddwwwwwwwwwwddddwwddwwwwwwwwwdddw";
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
						for (int d = 0; d < context.length(); d++) {
							result.append("d"); //píše "d" pouze pro případ, který odpovídá metodě isDate
						}
					}
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
		if ((contextResult >= OLDEST_YEAR && contextResult <= ACTUAL_YEAR) 
				|| (contextResult >= SHORT_ZERO_YEAR && contextResult <= SHORT_ACTUAL_YEAR)
				|| (contextResult >= SHORT_OLD_YEAR && contextResult < SHORT_HELPFULL_YEAR)) {
			return true;
		}
		return false;
	}

}
