package com.credibledoc.log.labelizer.iterator;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharIteratorTest {

    @Test
    public void yearHintLenient() {
        String exampleS = "79k1980ss80ssabc 2019.05.10 def1979d1234567890a000119";
        String expected = "nnnyyyynnyynnnnnnyyyynyynyynnnnyyynnyynnnnnyyynyyyyyy";

        String result = CharIterator.yearHintLenient(exampleS);
        assertEquals(expected, result);
    }

    @Test
    public void countOfSuccessfullyMarkedChars() {
        String recognizedOutput = "nyyEE";
        String expectedOutput   = "nnnEE";

        int result = CharIterator.countOfSuccessfullyMarkedChars(recognizedOutput, expectedOutput);
        assertEquals(3, result);
    }

    @Test
    public void countOfNotMarkedCharsInDatePattern() {
        String recognizedOutput = "nyncEE";
        String expectedOutput   = "nyycEE";

        int result = CharIterator.countOfNotMarkedCharsInDatePattern(recognizedOutput, expectedOutput);
        assertEquals(1, result);
    }
}
