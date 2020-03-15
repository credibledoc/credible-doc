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

public class SpecialTactic implements Tactic {

    private static final String TIMESTAMP_H_M_S = "Timestamp H:M:S ";
    private final DateFormat dateFormat = new SimpleDateFormat("H:m:s 'D:M:Y' d:M:yyyy", Locale.ENGLISH);

    private Date base;

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return line.startsWith(TIMESTAMP_H_M_S);
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
        if (line.startsWith(TIMESTAMP_H_M_S)) {
            int beginIndex = TIMESTAMP_H_M_S.length();
            return line.substring(beginIndex);
        } else if (line.startsWith("[")) {
            if (line.charAt(6) == ']') {
                return line.substring(1, 6);
            }
            return null;
        }
        return null;
    }

    @Override
    public String findThreadName(String line) {
        return null;
    }

    @Override
    public Date findDate(String line) {
        try {
            String dateString = parseDateStingFromLine(line);
            if (dateString == null) {
                return null;
            }
            if (line.startsWith(TIMESTAMP_H_M_S)) {
                Date baseDate = dateFormat.parse(dateString);
                base = baseDate;
                return baseDate;
            } else {
                String[] parts = dateString.split(":");
                int seconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
                int millis = seconds * 1000;
                return new Date(base.getTime() + millis);
            }
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot parse a date from the line: '" + line + "'", e);
        }
    }
}
