package com.credibledoc.tactic;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.tactic.Tactic;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecondApplicationTactic implements Tactic {

    private static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String DATE_AND_THREAD_SEPARATOR = " [";
    private static final int DATE_LENGTH = 28;
    private static final String ONE_SPACE = " ";

    private final DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);

    private final int MAX_LEVEL_NAME_LENGTH = 8;

    @Override
    public String getShortName() {
        return "app1";
    }

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return line.contains("Application app1 started.");
    }

    @Override
    public Date findDate(File file) {
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Date date = findDate(line);
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
        return findDate(line);
    }

    @Override
    public boolean containsDate(String line) {
        return findDate(line) != null;
    }

    @Override
    public String parseDateStingFromLine(String line) {
        if (line == null) {
            return null;
        }
        // This approach is faster then regex, for example Matcher matcher = PATTERN.matcher(line);
        int endIndex = line.indexOf(DATE_AND_THREAD_SEPARATOR);
        if (endIndex == -1) {
            return null;
        }
        if (endIndex > DATE_LENGTH + MAX_LEVEL_NAME_LENGTH) {
            return null;
        }
        int beginIndex = line.indexOf(ONE_SPACE);
        if (beginIndex == endIndex) {
            throw new CombinerRuntimeException("Cannot parse date from the line '" + line + "'");
        }
        String result = line.substring(beginIndex + ONE_SPACE.length(), endIndex);
        if (result.length() != DATE_LENGTH) {
            return null;
        }
        return result;
    }

    @Override
    public String findThreadName(String line) {
        int beginIndex = line.indexOf(DATE_AND_THREAD_SEPARATOR);
        if (beginIndex == -1 || beginIndex > MAX_LEVEL_NAME_LENGTH + DATE_TIME_FORMAT_STRING.length()) {
            throw new CombinerRuntimeException("Cannot parse thread name from the line '" + line + "'");
        }
        int endIndex = line.indexOf("] ", beginIndex);
        return line.substring(beginIndex + DATE_AND_THREAD_SEPARATOR.length(), endIndex);
    }

    @Override
    public Date findDate(String line) {
        try {
            String dateTime = parseDateStingFromLine(line);
            if (dateTime == null) {
                return null;
            }
            return dateFormat.parse(dateTime);
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot parse date from the line: '" + line + "'", e);
        }
    }
}
