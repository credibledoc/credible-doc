package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.hint.SimilarityHint;
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

        int result = IteratorService.countOfNotMarkedCharsInDatePattern(recognizedOutput, expectedOutput);
        assertEquals(1, result);
    }

    @Test
    public void linesSimilarityMarker() {
        String aaa = "aaaa [thread] aaaa";//n - 1 row
        String bbb = "bbbb [nextOneThread] bbbb";//n - 2 row
        String ccc = "cccc [thread] cccc";//rn - 3 row
        String ddd = "dddd [nextOneThread] dddd";//rn - 4 row
        //            rn - 5 row
        //            n - 6 row
        //            n - 7 row
        //            rn - 8 row
        
        String sourceLines = aaa + "\n" + bbb + "\n" + ccc + "\r\n" + ddd + "\r\n\r\n\n\n\r\n";
        //                       aaaa [thread] aaaaNbbbb [nextOneThread] bbbbNcccc [thread] ccccNNdddd [nextOneThread] ddddNNNNNNNN
        String expectedResult = "nnnnwwwwwwwwwwnnnwnnnnnwwwwwwwwwwwwwwwwwnnnnnnnnnwwwwwwwwwwnnnnnnnnnnwwwwwwwwwwwwwwwwwnnnnnnwwwwww";
        //                       1 row              2 row                     3 row               4 row                      5 678
        String result = SimilarityHint.linesSimilarityMarker(sourceLines);
        if (!expectedResult.equals(result)) {
            System.out.println(sourceLines);
        }
        assertEquals(expectedResult, result);
    }
}
