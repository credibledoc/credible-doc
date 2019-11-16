package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static service for this domain.
 * 
 * @author Kyrylo Semenko
 */
public class IteratorService {

    /**
     * Please do not instantiate this static helper.
     */
    private IteratorService() {
        // empty
    }

    /**
     * Count a number of {@link ProbabilityLabel}s not marked in recognizedOutput.
     *
     * @param recognizedOutput for example <b>yyyycMMcdmcHHcmmcssnSSnnZZZZnnyyycMMcddcHHcmmcssnSSnnZZZZ</b>
     * @param expectedOutput   for example <b>yyyycMMcddcHHcmmcsscSSSZZZZZnyyyycMMcddcHHcmmcsscSSSZZZZZ</b>
     * @return Count of mislabeled characters, for example 6 for above outputs.
     */
    public static int countOfNotMarkedCharsInDatePattern(String recognizedOutput, String expectedOutput) {
        int result = 0;
        for (int index = 0; index < recognizedOutput.length(); index++) {
            char expectedChar = expectedOutput.charAt(index);
            char recognizedChar = recognizedOutput.charAt(index);
            ProbabilityLabel probabilityLabel = ProbabilityLabel.find(expectedChar);
            if (probabilityLabel != null &&
                ProbabilityLabel.dates.contains(probabilityLabel) &&
                recognizedChar != expectedChar) {
                result++;
            }
        }
        return result;
    }
}
