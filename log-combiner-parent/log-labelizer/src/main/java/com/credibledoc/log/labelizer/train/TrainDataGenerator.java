package com.credibledoc.log.labelizer.train;

import com.credibledoc.log.labelizer.classifier.LinesWithDateClassification;
import com.credibledoc.log.labelizer.date.DateExample;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.hint.Hint;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates lines with dates in multiple different formats for learning and test purposes.
 * // TODO Kyrylo Semenko - zbavit se statickych vlastnosti
 * 
 * @author Kyrylo Semenko
 */
public class TrainDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TrainDataGenerator.class);
    private static final Locale[] AVAILABLE_LOCALES = DateFormat.getAvailableLocales();
    private static List<String> testPatterns = new ArrayList<>();
    private static final String[] TIME_ZONE_IDS = TimeZone.getAvailableIDs();
    private static int nextTimeZoneId = 0;
    private static int weekDay = 1;
    private static int month = 0;
    private static int nextLocaleIndex = 0;

        // TODO Kyrylo Semenko - smazat
//    public static void main(String[] args) {
//        try {
//            testPatterns.add("yyyy.MM.dd HH:mm:ss Z"); // 2019.09.15 18:10:34 +0200
//            testPatterns.add("yyyy.MM.dd HH:mm:ssZ"); // 2019.09.15 18:10:34+0200
//            testPatterns.add("yyyy.MM.dd HH:mm:ss(Z)"); // 2019.09.15 18:10:34(+0200)
//            testPatterns.add("MM d, yy HH:mm:ss"); // 09 15, 19 18:10:34
//            testPatterns.add("h:mm:ss:SSS"); // 6:10:34:773
//            testPatterns.add("HH:mm:ss, Z"); // 18:10:34, +0200
//            testPatterns.add("HH:mm:ss, K:mm, Z"); // 18:10:34, 6:10, +0200
//            testPatterns.add("yy.MM.dd hh:mm"); // 19.09.15 06:10
//            testPatterns.add("d MM yyyy HH:mm:ss Z"); // 15 09 2019 18:10:34 +0200
//            testPatterns.add("YYYY-'W'ww-u-HH:mm:ss"); // 2019-W37-7-18:10:34
//            testPatterns.add("yyyy/MM/dd'T'HH:mm:ss.SSSZ"); // 2019/09/15T18:10:34.773+0200
//            testPatterns.add("yyyy/MM/dd'T'HH:mm:ss.SSSXXX"); // 2019/09/15T18:10:34.773+02:00
//            testPatterns.add("YYYY/'W'ww/u/HH:mm:ss"); // 2019/W37/7/18:10:34
//            testPatterns.add("dd.MM.yy-HH:mm:ss.SSSZ"); // 15.09.19-18:10:34.773+0200
//            testPatterns.add("dd.MM.yyyy-HH:mm:ss.SSSZ"); // 15.09.2019-18:10:34.773+0200
//            testPatterns.add("EEEE MMMM dd HH:mm:ss yyyy"); // Saturday August 15 19:05:56 2019
//            
//            Date date = new Date();
//            List<String> existingPatterns = new ArrayList<>();
//            for (String pattern : testPatterns) {
//                String dateString = new SimpleDateFormat(pattern, Locale.US).format(date);
//                boolean databaseContainsThePattern = PagePatternRepository.getInstance().containsPattern(pattern);
//                logger.info("List: {}, {}, in DB: {}", pattern, dateString, databaseContainsThePattern);
//                if (databaseContainsThePattern) {
//                    existingPatterns.add(pattern);
//                }
//            }
//            if (!existingPatterns.isEmpty()) {
//                throw new LabelizerRuntimeException("These patterns already exist in the database. Please remove " +
//                    "these patterns from the testPatterns: " + existingPatterns);
//            }
//
//            int linesNumber = generateDates();
//
//            // TODO Kyrylo Semenko - relatively to the jar or workspace
//            File noDatesFile = new File("src\\main\\resources\\vectors\\labeled\\without\\02_noDates.txt");
//            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(noDatesFile)))) {
//                generateDaysAndMonths(writer);
//                for (int i = 0; i < linesNumber; i++) {
//                    int range = randomBetween(0, 9);
//                    String ip = "";
//                    String paddedNumber = "";
//                    if (range == 0) {
//                        ip = IpGenerator.randomIp();
//                    }
//                    if (range <= 4) {
//                        int length = randomBetween(1, 9);
//                        String nines = StringUtils.repeat("9", length);
//                        int number = randomBetween(0, Integer.parseInt(nines) + 1);
//                        String stringNumber = Integer.toString(number);
//                        paddedNumber = StringUtils.leftPad(stringNumber, length, "0");
//                    }
//                    String randomString = generateRandomString(LinesWithDateClassification.EXAMPLE_LENGTH_120 - paddedNumber.length() - ip.length());
//                    StringBuilder randomStringBuilder = new StringBuilder(randomString);
//                    if (randomString.length() != 100) {
//                        int randomIndex = randomBetween(0, randomString.length());
//                        randomStringBuilder.insert(randomIndex, ip);
//                        randomIndex = randomBetween(0, randomString.length());
//                        randomStringBuilder.insert(randomIndex, paddedNumber);
//                    }
//                    
//                    writer.write(randomStringBuilder.toString() + System.lineSeparator());
//                }
//            }
//        } catch (Exception e) {
//            throw new LabelizerRuntimeException(e);
//        } finally {
//            DatastoreService.getInstance().stop();
//        }
//    }

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
        for (String chunk : Splitter.fixedLength(LinesWithDateClassification.EXAMPLE_LENGTH_120).split(stringBuilder.toString())) {
            writer.write(chunk + System.lineSeparator());
        }
    }
// TODO Kyrylo Semenko - delete
//    private static int generateDates() throws IOException {
//        
//        int linesNumber = 0;
//        File file = new File("src/main/resources/vectors/labeled/date/dates.txt");
//        File dir = file.getParentFile();
//        if (!dir.exists()) {
//            Files.createDirectories(dir.toPath());
//        }
//        logger.info("File will be rewritten: '{}'", file.getAbsolutePath());
//        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
//            for (Locale locale : AVAILABLE_LOCALES) {
//                for (String pattern : testPatterns) {
//                    Date startDate = new Date(0);
//                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
//                    gregorianCalendar.setTime(startDate);
//                    for (int yearNum = 0; yearNum <= 1; yearNum++) {
//                        int year = randomBetween(1980, 2035);
//                        gregorianCalendar.set(Calendar.YEAR, year);
//                        int day = randomBetween(0, 365);
//                        for (int i = 0; i < 7; i++) {
//                            gregorianCalendar.set(Calendar.DAY_OF_YEAR, day + i);
//                            for (int hourNum = 0; hourNum <= 1; hourNum++) {
//                                int hour = randomBetween(1, 24);
//                                gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
//                                int minute = randomBetween(0, 59);
//                                gregorianCalendar.set(Calendar.MINUTE, minute);
//                                int second = randomBetween(0, 59);
//                                gregorianCalendar.set(Calendar.SECOND, second);
//                                int millis = randomBetween(0, 999);
//                                gregorianCalendar.set(Calendar.MILLISECOND, millis);
//                                
//                                String timeZoneId = TIME_ZONE_IDS[randomBetween(0, TIME_ZONE_IDS.length - 1)];
//                                TimeZone randomTimeZone = TimeZone.getTimeZone(timeZoneId);
//                                gregorianCalendar.setTimeZone(randomTimeZone);
//                                
//                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
//                                simpleDateFormat.setTimeZone(randomTimeZone);
//                                
//                                String dateString = simpleDateFormat.format(gregorianCalendar.getTime());
//                                DateExample dateExample = new DateExample();
//                                dateExample.setPattern(pattern);
//                                dateExample.setSource(dateString);
//                                String labels = findLabels(dateString, pattern, locale, gregorianCalendar,
//                                    randomTimeZone);
//                                dateExample.setLabels(labels);
//                                String json = new ObjectMapper().writeValueAsString(dateExample);
//                                writer.write(json + System.lineSeparator());
//                                linesNumber++;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return linesNumber;
//    }

    public static String findLabels(String dateString, String pattern, Locale locale, GregorianCalendar gregorianCalendar, TimeZone timeZone) {
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

    /**
     * Return random int.
     *
     * @param min inclusive
     * @param max inclusive
     * @return For example 0, 1 or 2 if the arguments are 0, 2.
     */
    public static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static Collection<? extends DateExample> generateDates(String pattern, int numExamples) {
        try {
            List<DateExample> result = new ArrayList<>();
            for (int i = 0; i < numExamples; i++) {
                Locale locale = getNextLocale();
                int weekDay = getNextWeekDay();
                int month = getNextMonth();
                TimeZone nextTimeZone = getNextTimeZone();

                Date startDate = new Date(0);
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTime(startDate);
                int year = randomBetween(1980, Hint.ACTUAL_YEAR + 5);
                gregorianCalendar.set(Calendar.YEAR, year);
                gregorianCalendar.set(Calendar.MONTH, month);
                gregorianCalendar.set(Calendar.DAY_OF_WEEK, weekDay);

                int hour = randomBetween(1, 24);
                gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
                int minute = randomBetween(0, 59);
                gregorianCalendar.set(Calendar.MINUTE, minute);
                int second = randomBetween(0, 59);
                gregorianCalendar.set(Calendar.SECOND, second);
                int millis = randomBetween(0, 999);
                gregorianCalendar.set(Calendar.MILLISECOND, millis);
                gregorianCalendar.setTimeZone(nextTimeZone);

                SimpleDateFormat simpleDateFormat;
                try {
                    simpleDateFormat = new SimpleDateFormat(pattern, locale);
                } catch (Exception e) {
                    String stackTrace = ExceptionUtils.getStackTrace(e);
                    String message = "Cannot create SimpleDateFormat. Pattern: " + pattern + ", locale: " + locale +
                        ",\n" + stackTrace;
                    logger.error(message);
                    String locationPath = TrainDataGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    File file = new File(locationPath, "failedPatterns.txt");
                    if (!file.exists()) {
                        logger.info("File will be created: {}", file.getAbsolutePath());
                        Files.createFile(file.toPath());
                    }
                    try(FileWriter fileWriter = new FileWriter(file, true)) {
                        fileWriter.write(message + System.lineSeparator());
                    }
                    continue;
                }
                simpleDateFormat.setTimeZone(nextTimeZone);

                String dateString = simpleDateFormat.format(gregorianCalendar.getTime());
                DateExample dateExample = new DateExample();
                dateExample.setDate(gregorianCalendar.getTime());
                dateExample.setLocale(locale);
                dateExample.setTimeZone(nextTimeZone);
                dateExample.setPattern(pattern);
                dateExample.setSource(dateString);
                String labels = findLabels(dateString, pattern, locale, gregorianCalendar,
                    nextTimeZone);
                dateExample.setLabels(labels);
                result.add(dateExample);
            }
            return result;
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private static int getNextMonth() {
        if (month > 11) {
            month = 0;
        }
        return month;
    }

    private static int getNextWeekDay() {
        if (weekDay == 8) {
            weekDay = 1;
        }
        return weekDay++;
    }

    private static TimeZone getNextTimeZone() {
        if (nextTimeZoneId == TIME_ZONE_IDS.length) {
            nextTimeZoneId = 0; 
        }
        String timeZoneId = TIME_ZONE_IDS[nextTimeZoneId++];
        return TimeZone.getTimeZone(timeZoneId);
    }

    private static Locale getNextLocale() {
        if (nextLocaleIndex == AVAILABLE_LOCALES.length) {
            nextLocaleIndex = 0;
        }
        return AVAILABLE_LOCALES[nextLocaleIndex++];
    }
}
