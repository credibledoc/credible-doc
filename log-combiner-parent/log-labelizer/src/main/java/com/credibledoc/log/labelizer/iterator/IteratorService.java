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

    private static final char N_NO_REPETITION = 'n';
    private static final char W_WITH_REPETITION = 'w';

    /**
     * Please do not instantiate this static helper.
     */
    private IteratorService() {
        // empty
    }

    /**
     * Mark columns with the same characters within column as 'w' ({@link #W_WITH_REPETITION})
     * and mark other columns as 'n' ({@link #N_NO_REPETITION}).
     * For example if we have three lines:
     * <pre>
     *     abd
     *     dcd
     *     aef
     * </pre>
     * then the result will be <b>wnw</b>, because first column <a>aba</a> contains repeated char 'a'
     * and third column <a>ddf</a> contains repeated char 'd'. Middle (second) column contains <b>dce</b> with
     * chars without repetitions hence it marked as 'n' ({@link #N_NO_REPETITION}).
     *
     * @param inputLines multiple lines separated with Unix \n or Windows \r\n.
     * @return String with the same length as inputLines and with 'n' ({@link #N_NO_REPETITION})
     * or 'w' ({@link #W_WITH_REPETITION}) markers only. It will help network to understand repeated patterns
     * in multiple lines (rows).
     */
    static String linesSimilarityMarker(String inputLines) {
        StringBuilder result = new StringBuilder(inputLines.length());
        Map<Integer, List<Character>> map = new HashMap<>();
        List<Character> currentRow = new ArrayList<>();
        int lineIndex = 0;
        map.put(lineIndex, currentRow);
        int maxLen = 0;
        int index = 0;
        for (char character : inputLines.toCharArray()) {
            currentRow.add(character);
            index++;
            if (character == '\n') {
                maxLen = Math.max(maxLen, currentRow.size());
                if (index < inputLines.length() - 1) {
                    lineIndex++;
                    currentRow = new ArrayList<>();
                    map.put(lineIndex, currentRow);
                }
            }
        }
        
        List<String> columns = new ArrayList<>(lineIndex + 1);
        for (int columnIndex = 0; columnIndex < maxLen; columnIndex++) {
            StringBuilder stringBuilder = new StringBuilder(lineIndex + 1);
            for (Map.Entry<Integer, List<Character>> entry : map.entrySet()) {
                List<Character> list = entry.getValue();
                if (list.size() > columnIndex) {
                    stringBuilder.append(list.get(columnIndex));
                }
            }
            columns.add(stringBuilder.toString());
        }
        
        for (Map.Entry<Integer, List<Character>> entry : map.entrySet()) {
            List<Character> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                Character character = list.get(i);
                if (columns.size() > i) {
                    String column = columns.get(i);
                    int count = StringUtils.countMatches(column, character);
                    if (count > 1) {
                        result.append(W_WITH_REPETITION);
                    } else {
                        result.append(N_NO_REPETITION);
                    }
                } else {
                    result.append(N_NO_REPETITION);
                }
            }
        }
        return result.toString();
    }

    /**
     * Count number of {@link ProbabilityLabel}s not marked in recognizedOutput.
     * @param recognizedOutput for example // TODO Kyrylo Semenko - complete an example
     * @param expectedOutput
     * @return
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
