package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexService {
    private static final String DATE_IN_DOUBLE_QUOTES_REGEX = ".*(HH.{1,3}mm).*";
    private static final Pattern DATE_PATTERN = Pattern.compile(DATE_IN_DOUBLE_QUOTES_REGEX, Pattern.MULTILINE);
    
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
                result.add(value);
            }
        }
        return result;
    }
}
