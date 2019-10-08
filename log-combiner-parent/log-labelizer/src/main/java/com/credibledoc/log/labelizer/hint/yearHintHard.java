package com.credibledoc.log.labelizer.hint;

import org.apache.commons.lang3.StringUtils;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;

public class yearHintHard {
	
	 public static String yearHintHard(String line) {
	        StringBuilder result = new StringBuilder(line.length());
	        StringBuilder context4 = new StringBuilder(4);
	        StringBuilder context2 = new StringBuilder(2);
	        for (char character : line.toCharArray()) {
	            result.append(ProbabilityLabel.N_WITHOUT_DATE.getCharacter());
	            if (Character.isDigit(character)) {
	                processDigit(result, context4, context2, character);
	            } else {
	                context4.setLength(0);
	                context2.setLength(0);
	            }
	        }
	        return result.toString();
	    }

	    private static void processDigit(StringBuilder result, StringBuilder context4, StringBuilder context2,
	                                     char character) {
	        context4.append(character);
	        context2.append(character);

	        if (context4.length() == 4) {
	            if (isDate(Integer.parseInt(context4.toString()))) {
	                writeToResult(result, 4);
	            }
	            context4.deleteCharAt(0);
	        }
	        if (context2.length() == 2) {
	            if (isDate(Integer.parseInt(context2.toString()))) {
	                writeToResult(result, 2);
	            }
	            context2.deleteCharAt(0);
	        }
	    }

	    private static void writeToResult(StringBuilder result, int numToAppend) {
	        String labels = StringUtils.rightPad("", numToAppend, ProbabilityLabel.Y_YEAR.getCharacter());
	        result.setLength(result.length() - numToAppend);
	        result.append(labels);
	    }

	    private static boolean isDate(Integer contextResult) {
	        return ((contextResult >= Hint.OLDEST_YEAR && contextResult <= Hint.ACTUAL_YEAR + 1)
	            || (contextResult >= Hint.SHORT_ZERO_YEAR && contextResult <= Hint.SHORT_ACTUAL_YEAR + 1)
	            || (contextResult >= Hint.SHORT_OLD_YEAR && contextResult < Hint.SHORT_HELPFULL_YEAR));
	    }


}
