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

public class FirstApplicationTactic implements Tactic {

    private static final String DATE_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss.SSS";

    private final DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);

    @Override
    public String getShortName() {
        return "app0";
    }

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return line.contains("Application app0 started.");
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
        int endIndex = line.indexOf(" [");
        if (endIndex == -1) {
            return null;
        }
        if (endIndex > DATE_TIME_FORMAT_STRING.length()) {
            return null;
        }
        return line.substring(0, endIndex);
    }

    @Override
    public String findThreadName(String line) {
        int beginIndex = line.indexOf(" [");
        if (beginIndex == -1 || beginIndex > DATE_TIME_FORMAT_STRING.length()) {
            return null;
        }
        int endIndex = line.indexOf("] ", beginIndex);
        return line.substring(beginIndex, endIndex);
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
