package com.credibledoc.combiner.doc.module.combiner;

import com.credibledoc.combiner.date.DateService;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.doc.module.combiner.application.Substitution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the {@link Tactic}
 * for the {@link Substitution}.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SubstitutionTactic implements Tactic {

    /**
     * RegEx of a date in a {@link Substitution} log line,
     * for example <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1...</pre>
     */
    private static final String PATTERN_DATE_STRING =
        "\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d";

    /**
     * Compiled {@value #PATTERN_DATE_STRING} value
     */
    private static final Pattern PATTERN_DATE = Pattern.compile(PATTERN_DATE_STRING);

    /**
     * {@link Pattern} of date and time of {@link Substitution}, for example
     * for example <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1...</pre>
     */
    private static final String DATE_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss.SSS";
    private static final String ONE_SPACE = " ";
    private static final int THREAD_NAME_INDEX = 35;
    private static final String PIPE = "|";

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);

    @Override
    public Date findDate(File file) {
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Date date = parseDateFromLine(line);
                if (date != null) {
                    return date;
                }
                line = logBufferedReader.readLine();
            }
            return null;
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot find date. File: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public Date findDate(String line, NodeFile nodeFile) {
        return parseDateFromLine(line);
    }

    @Override
    public boolean containsDate(String line) {
        if (line == null) {
            return false;
        }
        return PATTERN_DATE.matcher(line).find();
    }

    @Override
    public String parseDateStingFromLine(String line) {
        if (line == null) {
            return null;
        }
        return findDateString(line);
    }

    /**
     * Parse a {@link Date} from a log line.
     *
     * @param line    for example
     *                <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1...</pre>
     *                for {@link Substitution}
     * @return a parsed {@link Date}, for example 13.04.2018 07:27:41.462
     * or 'null' if the line is null or the date cannot be found.
     */
    private Date parseDateFromLine(String line) {
        if (line == null) {
            return null;
        }
        try {
            String clientDateString = findDateString(line);
            if (clientDateString != null) {
                return simpleDateFormat.parse(clientDateString);
            }

            return null;
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot parse a date from the line: " + line, e);
        }
    }

    /**
     * Parse a {@link Date} from a log line.
     *
     * @param line for example
     * <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1...</pre>
     * for {@link Substitution}
     * @return a parsed {@link Date}, for example 13.04.2018 07:27:41.462 or 'null' if the line is 'null' or the date cannot be found.
     */
    private String findDateString(String line) {
        int maxLength = line.length() > 90 ? 90 : line.length();
        String dateString = null;
        Matcher matcher = PATTERN_DATE.matcher(line.substring(0, maxLength));
        if (matcher.find()) {
            dateString = matcher.group();
        }
        return dateString;
    }


    /**
     * Examples:
     * <pre>05.11.2018 08:37:08.100|https-jsse-nio-15443-exec-10|DEBUG|...</pre>
     * <pre>19.12.2018 06:17:56.761 http-nio-8080-exec-305 INFO  [1212121]...</pre>
     */
    @Override
    public String findThreadName(String line) {
        int beginIndex = line.indexOf(PIPE);
        int endIndex = line.indexOf(PIPE, beginIndex + 1);

        if (isPipeSeparator(line)) {
            if (beginIndex != -1 && endIndex != -1) {
                return line.substring(beginIndex + PIPE.length(), endIndex);
            }
        } else {
            int firstSpaceIndex = line.indexOf(ONE_SPACE);
            int secondSpaceIndex = line.indexOf(ONE_SPACE, firstSpaceIndex + 1);
            int thirdSpaceIndex = line.indexOf(ONE_SPACE, secondSpaceIndex + 1);
            return line.substring(secondSpaceIndex + 1, thirdSpaceIndex);
        }

        return null;
    }

    private boolean isPipeSeparator(String line) {
        return line.substring(0, THREAD_NAME_INDEX).contains(PIPE);
    }

    @Override
    public Date findDate(String line) {
        return DateService.getInstance().parseDateTimeFromLine(line, simpleDateFormat, PATTERN_DATE, 35);
    }
}
