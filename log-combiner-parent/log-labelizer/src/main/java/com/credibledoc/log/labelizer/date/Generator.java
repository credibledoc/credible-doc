package com.credibledoc.log.labelizer.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates lines with date - time and thread in multiple different patterns for learning and test purposes.
 * 
 * @author Kyrylo Semenko
 */
public class Generator {

    private Date increaseRandom(Date date) {
        int addition = randomBetween(0, 100);
        return new Date(date.getTime() + addition);
    }
    
    private Date getRandomDate() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        int year = randomBetween(1970, 2050);
        gregorianCalendar.set(Calendar.YEAR, year);
        int dayOfYear = randomBetween(1, gregorianCalendar.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gregorianCalendar.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        return gregorianCalendar.getTime();
    }

    private int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
}
