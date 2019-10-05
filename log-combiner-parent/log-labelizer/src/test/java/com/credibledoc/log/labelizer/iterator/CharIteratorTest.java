package com.credibledoc.log.labelizer.iterator;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharIteratorTest {

    @Test
    public void yearHintSoft() {
        String exampleS = "79k1980ss80ssabc 2019.05.10 def1979d1234567890a000119";
        String expected = "nnnyyyynnyynnnnnnyyyynyynyynnnnyyynnyynnnnnyyynyyyyyy";

        String result = CharIterator.yearHintLenient(exampleS);
        assertEquals(expected, result);
    }
}
