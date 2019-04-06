package org.credibledoc.substitution.doc.module.substitution.activity;

import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a part of PlantUML activity diagram, for example
 * <pre>
 *     |Swimlane1|
 *         :foo4;
 * </pre>
 * from the <i>04.03.2019 18:41:13.658|main|INFO |com.credibledoc.substitution.core.configuration.ConfigurationService - Properties loaded by ClassLoader from the resource: file..</i> line.
 */
@Service
public class AnyLineTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String LOG_SEPARATOR = " - ";
    private static final String DOT = ".";
    private static final String FOUR_SPACES = "    ";
    private static final String WORDS_SEPARATOR = " ";
    private static final String BACKWARD_SLASH = "\\";

    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine,
                            LogBufferedReader logBufferedReader) {
        String currentSwimlane = parseClassName(multiLine.get(0));
        int maxRowLength = currentSwimlane.length() * 2 + currentSwimlane.length() / 2;
        String message = parseMessage(multiLine.get(0), maxRowLength);
        String result = "|" + currentSwimlane + "|" + LINE_SEPARATOR +
            FOUR_SPACES + ":" + message + ";" + LINE_SEPARATOR;

        reportDocument.getCacheLines().add(result);

        return null;
    }

    private String parseMessage(String line, int maxRowLength) {
        int separatorIndex = line.indexOf(LOG_SEPARATOR);
        String[] tokens = line.substring(separatorIndex + LOG_SEPARATOR.length()).split("\\s");
        tokens = splitLongTokens(tokens, maxRowLength);
        StringBuilder result = new StringBuilder(line.length());
        StringBuilder row = new StringBuilder(line.length());
        for (int i = 0; i < tokens.length; i++) {
            String escapedToken = escapeToken(tokens[i]);
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
        return token//.replaceAll(":", "~:")
                    .replaceAll(";", "~;")
                    .replaceAll("'", "");
    }

    private void appendEscapedToRow(int maxRowLength, StringBuilder row, String replaced, boolean hasMoreTokens) {
        row.append(replaced);
        if (row.length() < maxRowLength && hasMoreTokens) {
            row.append(WORDS_SEPARATOR);
        }
    }

    private String parseClassName(String line) {
        int separatorIndex = line.indexOf(LOG_SEPARATOR);
        String firstPart = line.substring(0, separatorIndex);
        int lastDotIndex = firstPart.lastIndexOf(DOT);
        return firstPart.substring(lastDotIndex + DOT.length());
    }
}
