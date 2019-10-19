package com.credibledoc.log.labelizer.train;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class TrainDataGeneratorTest {

    @Test
    public void findLabels() {
        String dateString = "1985.09.08 13:43:35 -0600";
        String pattern =    "yyyy.MM.dd HH:mm:ss Z";
        Locale locale = new Locale("");
        long millis = 686263258344L;
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date(millis));
        String expectedResult = "yyyycMMcddcHHcmmcsscZZZZZ";
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta");
        String result = TrainDataGenerator.findLabels(dateString, pattern, locale, gregorianCalendar, timeZone);
        assertEquals(expectedResult, result);
    }
}
