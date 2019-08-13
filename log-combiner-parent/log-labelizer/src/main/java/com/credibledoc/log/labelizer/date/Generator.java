package com.credibledoc.log.labelizer.date;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates lines with date - time and thread in multiple different patterns for learning and test purposes.
 * 
 * @author Kyrylo Semenko
 */
public class Generator {
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);
    private static List<String> list = new ArrayList<>();
    
    public static void main(String[] args) {
        try {
            list.add("yyyy.MM.dd HH:mm:ss Z");
            list.add("yyyy.MM.dd HH:mm:ssZ");
            list.add("yyyy.MM.dd HH:mm:ss(Z)");
            list.add("MM d, yy HH:mm:ss");
            list.add("h:mm:ss:SSS");
            list.add("HH:mm:ss, Z");
            list.add("HH:mm:ss, K:mm, Z");
            list.add("yy.MM.dd hh:mm");
            list.add("d MM yyyy HH:mm:ss Z");
            list.add("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            list.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            list.add("YYYY-'W'ww-u-HH:mm:ss");
            list.add("dd.MM.yy HH:mm:ss");
            list.add("dd.MM.yy-HH:mm:ss.SSSZ");
            list.add("dd.MM.yyyy-HH:mm:ss.SSSZ");
            
            Date date = new Date();
            for (String format : list) {
                String dateString = new SimpleDateFormat(format).format(date);
                logger.info("List: {}, {}", format, dateString);
            }
            
            File file = new File("src\\main\\resources\\vectors\\labeled\\date\\dates.txt");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                for (String format : list) {
                    Date startDate = new Date(0);
                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
                    gregorianCalendar.setTime(startDate);
                    for (int year = 1990; year <= 2025; year++) {
                        gregorianCalendar.set(GregorianCalendar.YEAR, year);
                        int day = randomBetween(0, 365);
                        gregorianCalendar.set(GregorianCalendar.DAY_OF_YEAR, day);
                        for (int hour = 1; hour <= 24; hour++) {
                            gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            int minute = randomBetween(0, 59);
                            gregorianCalendar.set(GregorianCalendar.MINUTE, minute);
                            int second = randomBetween(0, 59);
                            gregorianCalendar.set(GregorianCalendar.SECOND, second);
                            int millis = randomBetween(0, 999);
                            gregorianCalendar.set(GregorianCalendar.MILLISECOND, millis);
                            String dateString =
                                new SimpleDateFormat(format).format(gregorianCalendar.getTime());
                            writer.write(dateString + System.lineSeparator());
                        }
                    }
                }
            }
            
            File noDatesFile = new File("src\\main\\resources\\vectors\\labeled\\without\\noDates.txt");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(noDatesFile)))) {
                for (int i = 0; i < 10000; i++) {
                    int length = randomBetween(1, 9);
                    String nines = StringUtils.repeat("9", length);
                    int number = randomBetween(0, Integer.parseInt(nines) + 1);
                    String stringNumber = Integer.toString(number);
                    String padded = StringUtils.leftPad(stringNumber, length, "0");
                    writer.write(padded + System.lineSeparator());
                }
            }
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private static String generate() {
        Date date = getRandomDate();
        String format = generateRandomFormat();
        return new SimpleDateFormat(format).format(date);
    }

    private static String generateRandomFormat() {
        int randomIndex = randomBetween(0, list.size() - 1);
        return list.get(randomIndex);
    }

    private static Date increaseRandom(Date date) {
        int addition = randomBetween(0, 100);
        return new Date(date.getTime() + addition);
    }
    
    private static Date getRandomDate() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        
        int year = randomBetween(1970, 2050);
        gregorianCalendar.set(Calendar.YEAR, year);
        
        int dayOfYear = randomBetween(1, gregorianCalendar.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gregorianCalendar.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        
        int hour = randomBetween(0, 24);
        gregorianCalendar.set(GregorianCalendar.HOUR_OF_DAY, hour);

        int minute = randomBetween(0, 59);
        gregorianCalendar.set(GregorianCalendar.MINUTE, minute);

        int second = randomBetween(0, 59);
        gregorianCalendar.set(GregorianCalendar.SECOND, second);
        
        int millis = randomBetween(0, 100);
        gregorianCalendar.set(GregorianCalendar.MILLISECOND, millis);
        
        return gregorianCalendar.getTime();
    }

    private static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
}
