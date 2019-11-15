package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexService {
    private static final String DATE_IN_DOUBLE_QUOTES_REGEX = ".*(HH.{1,3}mm).*";
    public static final Pattern DATE_PATTERN = Pattern.compile(DATE_IN_DOUBLE_QUOTES_REGEX, Pattern.MULTILINE);
    
    private RegexService() {
        throw new LabelizerRuntimeException("Do not instantiate this static helper please.");
    }

    static List<String> parse(String page) {
        List<String> result = new ArrayList<>();
        String[] array = page.split("&quot;|<|>|\"|\\{|}");
        for (String next : array) {
            Matcher matcher = DATE_PATTERN.matcher(next);
            while (matcher.find()) {
                String value = matcher.group(0);
                String substring = filterNonDateHeaderAndTail(value);
                result.add(substring);
            }
        }
        return result;
    }

    @NotNull
    private static String filterNonDateHeaderAndTail(String value) {
        int firstPatternIndex = findFirst(value);
        int lastPatternIndex = findLast(value);
        return value.substring(firstPatternIndex, lastPatternIndex + 1);
    }

    private static int findLast(String value) {
        int result = value.length() - 1;
        char[] chars = value.toCharArray();
        for (int i = value.length() - 1; i >= 0; i--) {
            char next = chars[i];
            if (next == '\'') {
                return result;
            }
            ProbabilityLabel probabilityLabel = ProbabilityLabel.find(next);
            if (probabilityLabel != null && ProbabilityLabel.dates.contains(probabilityLabel)) {
                return result;
            }
            result = i - 1;
        }
        return result;
    }

    private static int findFirst(String value) {
        int result = 0;
        for (char next : value.toCharArray()) {
            if (next == '\'') {
                return result;
            }
            ProbabilityLabel probabilityLabel = ProbabilityLabel.find(next);
            if (probabilityLabel != null && ProbabilityLabel.dates.contains(probabilityLabel)) {
                return result;
            }
            result++;
        }
        return result;
    }
}
