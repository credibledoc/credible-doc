package com.credibledoc.log.labelizer.hint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class YearHint {
	public static final int OLDEST_YEAR = 1980;
	public static final int ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	public static final int SHORT_OLD_YEAR = 80;
	DateFormat shortActualYear = new SimpleDateFormat("yy");
	public static final int SHORT_ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR) % 100;
	public static final int SHORT_ZERO_YEAR = 00; //year 2000 in short version
	public static final int SHORT_HELPFULL_YEAR = 100; //year 2000 in helpfull version for nineteens years

	public static void main(String[] args) {
		String input =  "81adjhiiuhef1990fd70sdfafgrev1911";
		String output = "ddwwwwwwwwwwddddwwwwwwwwwwwwwwwww";
		StringBuffer result = new StringBuffer();
		StringBuffer context = new StringBuffer();
		begin:
		for (int i = 0; i < input.length(); i++) {
			Character character = input.charAt(i);
			System.out.println(character + ":" + Character.isDigit(character));
			if (!Character.isDigit(character)) {
				result.append("w");
				context.delete(0, context.length()); // zajišťuje, že context nebude počítat s hodnotami, které byli v předešlém zkoumaném čísle
			} else {
				context.append(character);
				if (context.length() == 2 || context.length() == 4) {
					if (context.length() == 2) {
							i++;
							character = input.charAt(i);
							System.out.println(character + ":" + Character.isDigit(character));
							if(!Character.isDigit(character)) {
								if (dateControl(context, result) == false) {
								}
								result.append("w");
							}
							context.append(character);
							continue begin;
					}
					dateControl(context, result);
				} else {
					continue;
				}
			}
		}

		System.out.println(output); // Pro ověření že výstup sedí
		System.out.println(result.toString());
		System.out.println(result.toString().equals(output));
	}

	private static boolean dateControl(StringBuffer context, StringBuffer result) {
		Integer contextResult = Integer.valueOf(context.toString());
		boolean isDate = isDate(contextResult);
		if (isDate == true) {
			for (int d = 0; d < context.length(); d++) {
				result.append("d"); //píše "d" pouze pro případ, který odpovídá metodě isDate
			}
			return true;
		}
		for (int w = 0; w < context.length(); w++) {
			result.append("w"); //píše "d" pouze pro případ, který odpovídá metodě isDate
		}
		return false;
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
