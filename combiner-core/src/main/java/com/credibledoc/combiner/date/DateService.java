package com.credibledoc.combiner.date;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateService {

    /**
     * Singleton.
     */
    private static DateService instance;

    /**
     * @return The {@link DateService} singleton.
     */
    public static DateService getInstance() {
        if (instance == null) {
            instance = new DateService();
        }
        return instance;
    }

    /**
     * Parse a {@link Date} from a log line.
     *
     * @param line    for example
     *                <pre>31.10.2019;07:00:00.231 [placeholder-substitution]  DEBUG [http-nio-8280-exec-29] - Dispatcher...</pre>
     * @return a parsed {@link Date}, for example 16.10.2019;00:44:23.973
     * or 'null' if the line is null or the date cannot be found.
     */
    public Date parseDateTimeFromLine(String line, SimpleDateFormat simpleDateFormat,
                                      Pattern dateTimePattern, int maxIndexEndOfTime) {
        if (line == null) {
            return null;
        }
        try {
            String dateString = findDateTime(line, dateTimePattern, maxIndexEndOfTime);
            if (dateString != null) {
                return simpleDateFormat.parse(dateString);
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
     * <pre>17:45:58.172|qtp826690115-39|DEBUG|...</pre>
     * @return a parsed {@link Date}, for example 17:45:58.172 or 'null' if the line is 'null' or the date cannot be found.
     */
    public String findDateTime(String line, Pattern dateTimePattern, int maxIndexEndOfTime) {
        int maxLength = line.length() > maxIndexEndOfTime ? maxIndexEndOfTime : line.length();
        String dateString = null;
        Matcher matcher = dateTimePattern.matcher(line.substring(0, maxLength));
        if (matcher.find()) {
            dateString = matcher.group();
        }
        return dateString;
    }

    public Date findDateInFile(File file, SimpleDateFormat simpleDateTimeFormat, Pattern dateTimePattern, int maxIndexEndOfDate) {
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Date dateTime = parseDateTimeFromLine(line,
                        simpleDateTimeFormat, dateTimePattern, maxIndexEndOfDate);
                if (dateTime != null) {
                    return dateTime;
                }
                line = logBufferedReader.readLine();
            }
            return null;
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot find date. File: " + file.getAbsolutePath(), e);
        }
    }
}
