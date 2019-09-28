package com.credibledoc.log.labelizer.train;

import com.credibledoc.log.labelizer.classifier.LinesWithDateClassification;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
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
public class Generator {
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);
    private static List<String> list = new ArrayList<>();
    
    public static void main(String[] args) {
        try {
            list.add("yyyy.MM.dd HH:mm:ss Z"); // 2019.09.15 18:10:34 +0200
            list.add("yyyy.MM.dd HH:mm:ssZ"); // 2019.09.15 18:10:34+0200
            list.add("yyyy.MM.dd HH:mm:ss(Z)"); // 2019.09.15 18:10:34(+0200)
            list.add("yyyy.MM.dd HH:mm:ss"); // 2019.09.15 18:10:34
            list.add("MM d, yy HH:mm:ss"); // 09 15, 19 18:10:34
            list.add("h:mm:ss:SSS"); // 6:10:34:773
            list.add("HH:mm:ss, Z"); // 18:10:34, +0200
            list.add("HH:mm:ss, K:mm, Z"); // 18:10:34, 6:10, +0200
            list.add("yy.MM.dd hh:mm"); // 19.09.15 06:10
            list.add("d MM yyyy HH:mm:ss Z"); // 15 09 2019 18:10:34 +0200
            list.add("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // 2019-09-15T18:10:34.773+0200
            list.add("yyyy-MM-dd HH:mm:ss"); // 2019-09-15 18:10:34
            list.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); // 2019-09-15T18:10:34.773+02:00
            list.add("YYYY-'W'ww-u-HH:mm:ss"); // 2019-W37-7-18:10:34
            list.add("yyyy/MM/dd'T'HH:mm:ss.SSSZ"); // 2019/09/15T18:10:34.773+0200
            list.add("yyyy/MM/dd HH:mm:ss"); // 2019/09/15 18:10:34
            list.add("yyyy/MM/dd'T'HH:mm:ss.SSSXXX"); // 2019/09/15T18:10:34.773+02:00
            list.add("YYYY/'W'ww/u/HH:mm:ss"); // 2019/W37/7/18:10:34
            list.add("dd.MM.yy HH:mm:ss"); // 15.09.19 18:10:34
            list.add("dd.MM.yy-HH:mm:ss.SSSZ"); // 15.09.19-18:10:34.773+0200
            list.add("dd.MM.yyyy-HH:mm:ss.SSSZ"); // 15.09.2019-18:10:34.773+0200
            list.add("EEE MMM dd HH:mm:ss yyyy"); // Sat Aug 12 04:05:51 2006
            list.add("EEEE MMMM dd HH:mm:ss yyyy"); // Saturday August 15 19:05:56 2019
            
            Date date = new Date();
            for (String format : list) {
                String dateString = new SimpleDateFormat(format).format(date);
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
                    String randomString = generateRandomString(LinesWithDateClassification.EXAMPLE_LENGTH - paddedNumber.length());
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
        for (String chunk : Splitter.fixedLength(LinesWithDateClassification.EXAMPLE_LENGTH).split(stringBuilder.toString())) {
            writer.write(chunk + System.lineSeparator());
        }
    }

    private static int generateDates() throws IOException {
        int linesNumber = 0;
        File file = new File("src\\main\\resources\\vectors\\labeled\\date\\dates.txt");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            for (Locale locale : DateFormat.getAvailableLocales()) {
                for (String format : list) {
                    Date startDate = new Date(0);
                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
                    gregorianCalendar.setTime(startDate);
                    for (int yearNum = 0; yearNum <= 1; yearNum++) {
                        int year = randomBetween(1980, 2035);
                        gregorianCalendar.set(Calendar.YEAR, year);
                        int day = randomBetween(0, 365);
                        gregorianCalendar.set(Calendar.DAY_OF_YEAR, day);
                        for (int hourNum = 0; hourNum <= 1; hourNum++) {
                            int hour = randomBetween(1, 24);
                            gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            int minute = randomBetween(0, 59);
                            gregorianCalendar.set(Calendar.MINUTE, minute);
                            int second = randomBetween(0, 59);
                            gregorianCalendar.set(Calendar.SECOND, second);
                            int millis = randomBetween(0, 999);
                            gregorianCalendar.set(Calendar.MILLISECOND, millis);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, locale);
                            String dateString = simpleDateFormat.format(gregorianCalendar.getTime());
                            writer.write(dateString + System.lineSeparator());
                            linesNumber++;
                        }
                    }
                }
            }
        }
        return linesNumber;
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
