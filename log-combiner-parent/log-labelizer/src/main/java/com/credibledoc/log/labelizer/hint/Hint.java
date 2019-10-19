package com.credibledoc.log.labelizer.hint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;

/**
 * 
 *
 * @author Olga Semenko
 */

public class Hint {
	private static final String YEAR = ProbabilityLabel.Y_YEAR.getString();
	private static final String WITHOUT = ProbabilityLabel.N_WITHOUT_DATE.getString();
	public static final int OLDEST_YEAR = 1980;
	public static final int ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	public static final int SHORT_OLD_YEAR = 80;
	DateFormat shortActualYear = new SimpleDateFormat("yy");
	public static final int SHORT_ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR) % 100;
	public static final int SHORT_ZERO_YEAR = 00; // year 2000 in short version
	public static final int SHORT_HELPFULL_YEAR = 100; // year 2000 in helpfull version for nineteens years

	public static String yearLabels(String input) {
		Integer lastInputValue = (input.length() - 1);
		StringBuilder result = new StringBuilder();
		StringBuilder context = new StringBuilder();
		for (int index = 0; index < input.length(); index++) {
			Integer valueOfInput = index;
			Character character = input.charAt(index);
			if (!Character.isDigit(character)) {
				result.append(WITHOUT);
				context.delete(0, context.length());
				controlLastValue(result, lastInputValue, valueOfInput, context);
			} else {
				context.append(character);

				if (context.length() <= 4) {
					nextCharControl(valueOfInput, lastInputValue, input, context, result);
				} else {
					index = labelizingOfFifeAndMoreNumbers(input, lastInputValue, result, context, index, valueOfInput, // NOSONAR
							character);
				}

			}
		}
		return result.toString();
	}

	private static int labelizingOfFifeAndMoreNumbers(String input, Integer lastInputValue, StringBuilder result,
			StringBuilder context, int index, Integer valueOfInput, Character character) {
		while (index < lastInputValue && Character.isDigit(character)) {
			controlLastValue(result, lastInputValue, valueOfInput, context);
			index++;
			valueOfInput = index;
			controlLastValue(result, lastInputValue, valueOfInput - 1, context);
			character = input.charAt(index);
			context.append(character);
		}

		for (int w = 0; w < context.length(); w++) {
			result.append(WITHOUT);
		}
		context.delete(0, context.length());
		controlLastValue(result, lastInputValue, valueOfInput, context);
		return index;
	}

	/**
	 * This is method, which controls characters of context. Also it controls next
	 * characters if there is number or not.
	 */
	private static void nextCharControl(int valueOfInput, int lastInputValue, String input, StringBuilder context,
			StringBuilder result) {
		int c = valueOfInput;
		if (valueOfInput == lastInputValue) {
			controlOfSingleNumber(context, result);
			context.delete(0, context.length());
			controlLastValue(result, lastInputValue, valueOfInput, context);
		} else {
			c++;
		}
		Character nextChar = input.charAt(c);
		if (!Character.isDigit(nextChar)) {
			controlOfSingleNumber(context, result);
		}
	}

	/**
	 * When in context is just one number, this method controls, if next character
	 * is number or not. If number does not have number in the next step, it mean,
	 * that number certainly is not an year.
	 */
	private static void controlOfSingleNumber(StringBuilder context, StringBuilder result) {
		if (context.length() != 1) {
			dateControl(context, result);
		} else {
			result.append(WITHOUT);
		}
	}

	private static void dateControl(StringBuilder context, StringBuilder result) {
		Integer contextResult = Integer.valueOf(context.toString());
		if (isDate(contextResult)) {
			for (int d = 0; d < context.length(); d++) {
				result.append(YEAR);
			}
		} else {
			for (int w = 0; w < context.length(); w++) {
				result.append(WITHOUT);
			}
		}
	}

	private static boolean isDate(Integer contextResult) {
		return (contextResult >= OLDEST_YEAR && contextResult <= ACTUAL_YEAR + 1)
				|| (contextResult >= SHORT_ZERO_YEAR && contextResult <= SHORT_ACTUAL_YEAR + 1)
				|| (contextResult >= SHORT_OLD_YEAR && contextResult < SHORT_HELPFULL_YEAR);
	}

	private static void controlLastValue(StringBuilder result, int lastInputValue, int valueOfInput,
			StringBuilder context) {
		if (valueOfInput == lastInputValue) {
			for (int w = 0; w < context.length(); w++) {
				result.append(WITHOUT);
			}
		}
	}

}
