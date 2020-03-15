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
import java.util.Locale;

public class SyslogTactic implements Tactic {

    private static final String DATE_TIME_FORMAT_STRING = "yyyy MMM dd HH:mm:ss";

    private final DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING, Locale.ENGLISH);

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return findDate(line) != null;
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
        if (line == null || line.length() < 22) {
            return null;
        }
        // This approach is faster then regex, but regex can also be used
        int beginIndex = line.indexOf(":") + 1;
        if (beginIndex < 1 || beginIndex > 10) {
            return null;
        }
        int endIndex = beginIndex + DATE_TIME_FORMAT_STRING.length();
        return line.substring(beginIndex, endIndex);
    }

    @Override
    public String findThreadName(String line) {
        return null;
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
            throw new CombinerRuntimeException("Cannot parse a date from the line: '" + line + "'", e);
        }
    }
}
