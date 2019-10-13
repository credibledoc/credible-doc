package com.credibledoc.log.labelizer.train;

import com.credibledoc.log.labelizer.date.DateExample;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates lines with dates in multiple different formats for learning and test purposes.
 * 
 * @author Kyrylo Semenko
 */
public class TrainDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TrainDataGenerator.class);
    private static List<String> patternsList = new ArrayList<>();
    
    public static void main(String[] args) {
        try {
            patternsList.add("yyyy.MM.dd HH:mm:ss Z"); // 2019.09.15 18:10:34 +0200
            patternsList.add("yyyy.MM.dd HH:mm:ssZ"); // 2019.09.15 18:10:34+0200
            patternsList.add("yyyy.MM.dd HH:mm:ss(Z)"); // 2019.09.15 18:10:34(+0200)
            patternsList.add("yyyy.MM.dd HH:mm:ss"); // 2019.09.15 18:10:34
            patternsList.add("MM d, yy HH:mm:ss"); // 09 15, 19 18:10:34
            patternsList.add("h:mm:ss:SSS"); // 6:10:34:773
            patternsList.add("HH:mm:ss, Z"); // 18:10:34, +0200
            patternsList.add("HH:mm:ss, K:mm, Z"); // 18:10:34, 6:10, +0200
            patternsList.add("yy.MM.dd hh:mm"); // 19.09.15 06:10
            patternsList.add("d MM yyyy HH:mm:ss Z"); // 15 09 2019 18:10:34 +0200
            patternsList.add("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // 2019-09-15T18:10:34.773+0200
            patternsList.add("yyyy-MM-dd HH:mm:ss"); // 2019-09-15 18:10:34
            patternsList.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); // 2019-09-15T18:10:34.773+02:00
            patternsList.add("YYYY-'W'ww-u-HH:mm:ss"); // 2019-W37-7-18:10:34
            patternsList.add("yyyy/MM/dd'T'HH:mm:ss.SSSZ"); // 2019/09/15T18:10:34.773+0200
            patternsList.add("yyyy/MM/dd HH:mm:ss"); // 2019/09/15 18:10:34
            patternsList.add("yyyy/MM/dd'T'HH:mm:ss.SSSXXX"); // 2019/09/15T18:10:34.773+02:00
            patternsList.add("YYYY/'W'ww/u/HH:mm:ss"); // 2019/W37/7/18:10:34
            patternsList.add("dd.MM.yy HH:mm:ss"); // 15.09.19 18:10:34
            patternsList.add("dd.MM.yy-HH:mm:ss.SSSZ"); // 15.09.19-18:10:34.773+0200
            patternsList.add("dd.MM.yyyy-HH:mm:ss.SSSZ"); // 15.09.2019-18:10:34.773+0200
            patternsList.add("EEE MMM dd HH:mm:ss yyyy"); // Sat Aug 12 04:05:51 2006
            patternsList.add("EEEE MMMM dd HH:mm:ss yyyy"); // Saturday August 15 19:05:56 2019
            // TODO Kyrylo Semenko - [03/Jul/1996:06:56:12 -0800]
            // TODO Kyrylo Semenko - 03/22 08:51:06
            
            Date date = new Date();
            for (String format : patternsList) {
                String dateString = new SimpleDateFormat(format, Locale.US).format(date);
                logger.info("List: {}, {}", format, dateString);
            }

            int linesNumber = generateDates();

            File noDatesFile = new File("src\\main\\resources\\vectors\\labeled\\without\\02_noDates.txt");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(noDatesFile)))) {
                generateDaysAndMonths(writer);
                for (int i = 0; i < linesNumber; i++) {
                    int length = randomBetween(1, 9);
                    String nines = StringUtils.repeat("9", length);
                    int number = randomBetween(0, Integer.parseInt(nines) + 1);
                    String stringNumber = Integer.toString(number);
                    String paddedNumber = StringUtils.leftPad(stringNumber, length, "0");
                    String randomString = generateRandomString(CharIterator.EXAMPLE_LENGTH - paddedNumber.length());
                    int randomOrder = randomBetween(0, 1);
                    // TODO Kyrylo Semenko - generovat IP adresy napriklad 111.222.333.123
                    if (randomOrder == 0) {
                        writer.write(randomString + paddedNumber + System.lineSeparator());
                    } else {
                        writer.write(paddedNumber + randomString + System.lineSeparator());
                    }
                }
            }
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    /**
     * Add all possible days and months in all locales. Unique characters from these strings
     * will be used in {@link CharIterator}.
     */    
    private static void generateDaysAndMonths(BufferedWriter writer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Locale locale : DateFormat.getAvailableLocales()) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(new Date());
            for (int dayIndex = 1; dayIndex <= 7; dayIndex++) {
                gregorianCalendar.set(Calendar.DAY_OF_WEEK, dayIndex);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", locale);
                String dayString = simpleDateFormat.format(gregorianCalendar.getTime());
                stringBuilder.append(dayString);
            }
            for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
                gregorianCalendar.set(Calendar.MONTH, monthIndex);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", locale);
                String monthString = simpleDateFormat.format(gregorianCalendar.getTime());
                stringBuilder.append(monthString);
            }
        }
        for (String chunk : Splitter.fixedLength(CharIterator.EXAMPLE_LENGTH).split(stringBuilder.toString())) {
            writer.write(chunk + System.lineSeparator());
        }
    }

    private static int generateDates() throws IOException {
        String[] timeZoneIds = TimeZone.getAvailableIDs();
        int linesNumber = 0;
        File file = new File("src/main/resources/vectors/labeled/date/dates.txt");
        logger.info("File will be rewritten: '{}'", file.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            for (Locale locale : DateFormat.getAvailableLocales()) {
                for (String pattern : patternsList) {
                    Date startDate = new Date(0);
                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
                    gregorianCalendar.setTime(startDate);
                    for (int yearNum = 0; yearNum <= 1; yearNum++) {
                        int year = randomBetween(1980, 2035);
                        gregorianCalendar.set(Calendar.YEAR, year);
                        int day = randomBetween(0, 365);
                        for (int i = 0; i < 7; i++) {
                            gregorianCalendar.set(Calendar.DAY_OF_YEAR, day + i);
                            for (int hourNum = 0; hourNum <= 1; hourNum++) {
                                int hour = randomBetween(1, 24);
                                gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
                                int minute = randomBetween(0, 59);
                                gregorianCalendar.set(Calendar.MINUTE, minute);
                                int second = randomBetween(0, 59);
                                gregorianCalendar.set(Calendar.SECOND, second);
                                int millis = randomBetween(0, 999);
                                gregorianCalendar.set(Calendar.MILLISECOND, millis);
                                
                                String timeZoneId = timeZoneIds[randomBetween(0, timeZoneIds.length - 1)];
                                TimeZone randomTimeZone = TimeZone.getTimeZone(timeZoneId);
                                gregorianCalendar.setTimeZone(randomTimeZone);
                                
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
                                simpleDateFormat.setTimeZone(randomTimeZone);
                                
                                String dateString = simpleDateFormat.format(gregorianCalendar.getTime());
                                DateExample dateExample = new DateExample();
                                dateExample.setPattern(pattern);
                                dateExample.setSource(dateString);
                                String labels = findLabels(dateString, pattern, locale, gregorianCalendar,
                                    randomTimeZone);
                                dateExample.setLabels(labels);
                                String json = new ObjectMapper().writeValueAsString(dateExample);
                                writer.write(json + System.lineSeparator());
                                linesNumber++;
                            }
                        }
                    }
                }
            }
        }
        return linesNumber;
    }

    static String findLabels(String dateString, String pattern, Locale locale, GregorianCalendar gregorianCalendar, TimeZone timeZone) {
        gregorianCalendar.setTimeZone(timeZone);
        List<Pair<String, ProbabilityLabel>> patterns = splitDateFormatPattern(pattern);
        StringBuilder result = new StringBuilder();
        for (Pair<String, ProbabilityLabel> pair : patterns) {
            ProbabilityLabel probabilityLabel = pair.getRight();
            if (probabilityLabel != ProbabilityLabel.C_CALENDAR_DATE_FILLER) {
                String subpattern = pair.getLeft();
                SimpleDateFormat format = new SimpleDateFormat(subpattern, locale);
                format.setTimeZone(timeZone);
                String text = format.format(gregorianCalendar.getTime());
                if (subpattern.contains(ProbabilityLabel.M_MONTH_IN_YEAR.getString()) && !pattern.equals(subpattern)) {
                    // Get flexible form of month
                    SimpleDateFormat formatWithDays = new SimpleDateFormat(subpattern + "dd", locale);
                    formatWithDays.setTimeZone(timeZone);
                    String textWithDate = formatWithDays.format(gregorianCalendar.getTime());
                    text = textWithDate.substring(0, textWithDate.length() - 2);
                }
                String replacement = StringUtils.leftPad("", text.length(), probabilityLabel.getString());
                result.append(replacement);       
            } else {
                String replacement = StringUtils.leftPad("", pair.getLeft().length(), probabilityLabel.getString());
                result.append(replacement);
            }
        }
        if (dateString.length() != result.length()) {
            throw new LabelizerRuntimeException("DateString and result have different length. " +
                "DateString: '" + dateString +
                "', result: '" + result.toString() +
                "', pattern: '" + pattern +
                "', locale: '" + locale +
                "', date in milliseconds: '" + gregorianCalendar.getTimeInMillis() +
                "', timeZone: '" + timeZone.getID() +
                "'");
        }
        return result.toString();
    }

    /**
     * @param pattern for example <i>HH:mm:ss, K:mm, Z</i>
     * @return For example
     * <ul>
     *     <li>Pair{key=HH, value=HOUR_IN_DAY_0_23}</li>
     *     <li>Pair{key=c, value=C_CALENDAR_DATE_FILLER}</li>
     *     <li>Pair{key=mm, value=MINUTE_OF_HOUR}</li>
     *     <li>Pair{key=c, value=C_CALENDAR_DATE_FILLER}</li>
     *     <li>Pair{key=ss, value=SECOND_IN_MINUTE}</li>
     *     <li>Pair{key=cc, value=C_CALENDAR_DATE_FILLER}</li>
     *     <li>Pair{key=K, value=HOUR_IN_AM_PM_0_11}</li>
     *     <li>Pair{key=c, value=C_CALENDAR_DATE_FILLER}</li>
     *     <li>Pair{key=mm, value=MINUTE_OF_HOUR}</li>
     *     <li>Pair{key=cc, value=C_CALENDAR_DATE_FILLER}</li>
     *     <li>Pair{key=Z, value=TIME_ZONE_RFC_822}</li>
     * </ul>
     */
    private static List<Pair<String, ProbabilityLabel>> splitDateFormatPattern(String pattern) {
        List<Pair<String, ProbabilityLabel>> pairs = new ArrayList<>();
        String unescaped;
        if (pattern.contains("'")) {
            unescaped = unescapePattern(pattern);
        } else {
            unescaped = pattern;
        }
        for (Character character : unescaped.toCharArray()) {
            ProbabilityLabel probabilityLabel = ProbabilityLabel.find(character);
            if (probabilityLabel == null || !ProbabilityLabel.dates.contains(probabilityLabel)) {
                probabilityLabel = ProbabilityLabel.C_CALENDAR_DATE_FILLER;
            }
            if (pairs.isEmpty() || pairs.get(pairs.size() - 1).getRight() != probabilityLabel) {
                Pair<String, ProbabilityLabel> pair = new Pair<>(probabilityLabel.getString(), probabilityLabel);
                pairs.add(pair);
            } else {
                Pair<String, ProbabilityLabel> last = pairs.get(pairs.size() - 1);
                last.setFirst(last.getFirst() + probabilityLabel.getString());
            }
        }
        return pairs;
    }

    private static String unescapePattern(String pattern) {
        String replaced = pattern.replace("''", ProbabilityLabel.C_CALENDAR_DATE_FILLER.getString());
        boolean started = false;
        StringBuilder result = new StringBuilder(pattern.length());
        for (char character : replaced.toCharArray()) {
            if (character == '\'') {
                started = !started;
            } else {
                if (started) {
                    result.append(ProbabilityLabel.C_CALENDAR_DATE_FILLER.getString());
                } else {
                    result.append(character);
                }
            }
        }
        return result.toString();
    }

    private static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String character = CharIterator.randomWordChar();
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }

    private static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
}
