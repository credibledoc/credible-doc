package com.credibledoc.log.labelizer.crawler;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RegexServiceTest {

    @Test
    public void parse() {
        String expected = "EEE MMM d HH:mm:ss zzz yyyy";
        String example = "aa \"&quot;" + expected + "&quot;\"";
        List<String> result = RegexService.parse(example + " bla\n, " + example + example);
        assertEquals(3, result.size());
        assertEquals(expected, result.get(0));
    }
}
