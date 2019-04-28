package com.credibledoc.substitution.doc.module.substitution.logmessage;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This service helps to parse log messages.
 * 
 * @author Kyrylo Semenko
 */
@Service
public class LogMessageService {

    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String LOG_SEPARATOR = " - ";
    public static final String DOT = ".";
    public static final String FOUR_SPACES = "    ";
    private static final String WORDS_SEPARATOR = " ";
    private static final String BACKWARD_SLASH = "\\";
    private static final String NOT_ALLOWED_AT_THE_END = "~;";

    /**
     * Parse a message from a log line and split it to rows.
     * @param line for example
     *             <pre>04.03.2019 18:41:13.658|main|INFO |com.credibledoc.substitution.core.configuration.ConfigurationService - Properties loaded by ClassLoader from the resource: file..</pre>
     * @param maxRowLength maximal number of characters in a row
     * @return For example <pre>Properties loaded by ClassLoader from the resource: file..</pre>
     */
    public String parseMessage(String line, int maxRowLength) {
        int separatorIndex = line.indexOf(LOG_SEPARATOR);
        String[] tokens = line.substring(separatorIndex + LOG_SEPARATOR.length()).split("\\s");
        tokens = splitLongTokens(tokens, maxRowLength);
        StringBuilder result = new StringBuilder(line.length());
        StringBuilder row = new StringBuilder(line.length());
        for (int i = 0; i < tokens.length; i++) {
            if ("}".equals(tokens[i].trim())) {
                row.insert(row.length() - 1, tokens[i].trim());
                tokens[i] = "";
            }
            String escapedToken = escapeToken(tokens[i]);
            if (escapedToken.endsWith(NOT_ALLOWED_AT_THE_END)) {
                // PlantUML comment line cannot be ended by this sequence. Move this sequence to the next line.
                if (tokens.length > i + 2) {
                    tokens[i + 1] = NOT_ALLOWED_AT_THE_END + tokens[i + 1];
                }
                escapedToken = escapedToken.substring(0, escapedToken.length() - 2);
            }
            boolean hasMoreTokens = i + 1 < tokens.length;
            boolean isShortRow = row.length() + escapedToken.length() + WORDS_SEPARATOR.length() < maxRowLength;
            if (isShortRow) {
                appendEscapedToRow(maxRowLength, row, escapedToken, hasMoreTokens);
            } else {
                if (row.toString().endsWith(BACKWARD_SLASH)) {
                    row.replace(row.length() - 1, row.length(), "");
                    escapedToken = BACKWARD_SLASH + escapedToken;
                }
                result.append(row);
                result.append(LINE_SEPARATOR);
                row = new StringBuilder(line.length()).append(FOUR_SPACES);
                appendEscapedToRow(maxRowLength, row, escapedToken, hasMoreTokens);
            }
        }
        result.append(row);
        return result.toString();
    }

    private String[] splitLongTokens(String[] tokens, int maxRowLength) {
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (token.length() <= maxRowLength) {
                result.add(token);
            } else {
                while (token.length() > maxRowLength) {
                    result.add(token.substring(0, maxRowLength));
                    token = token.substring(maxRowLength);
                }
                if (token.length() > 0) {
                    result.add(token);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    private String escapeToken(String token) {
        return token
            .replaceAll(";", "~;")
            .replaceAll("'", "");
    }

    private void appendEscapedToRow(int maxRowLength, StringBuilder row, String replaced, boolean hasMoreTokens) {
        row.append(replaced);
        if (row.length() < maxRowLength && hasMoreTokens) {
            row.append(WORDS_SEPARATOR);
        }
    }
}
