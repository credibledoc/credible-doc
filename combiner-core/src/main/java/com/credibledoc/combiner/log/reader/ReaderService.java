package com.credibledoc.combiner.log.reader;

import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.line.LineState;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.state.FilesMergerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Reads lines from log files.
 * Contains methods for working with file readers, for example {@link LogBufferedReader}s.
 *
 * @author Kyrylo Semenko
 */
public class ReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ReaderService.class);

    /**
     * How many characters can be marked for reset a stream,
     * see the {@link Reader#mark(int)} and {@link Reader#reset()} methods.
     */
    private static final int MAX_CHARACTERS_IN_ONE_LINE = 99999;

    /**
     * This module name
     */
    public static final String COMBINER_CORE_MODULE_NAME = "combiner-core";

    /**
     * Singleton.
     */
    private static ReaderService instance;

    /**
     * @return The {@link ReaderService} singleton.
     */
    public static ReaderService getInstance() {
        if (instance == null) {
            instance = new ReaderService();
        }
        return instance;
    }

    /**
     * Read a single log record. It can be multi-line.
     * @param line the first line of multi-lines record or a single line. It can be followed by additional lines but not necessarily.
     * @param logBufferedReader the data source
     * @return for example the one line
     * <pre>3.2-SNAPSHOT INFO  2018-04-13 13:19:40 : RAMJob...</pre>
     * or the multi lines 
     * <pre>
     * 3.2-SNAPSHOT INFO  2018-04-13 13:19:40 : Schedule...
     *     Scheduler class: 'org.quartz.core.QuartzSchedule...
     *     NOT STARTED.
     * </pre>
     */
    public List<String> readMultiline(String line, LogBufferedReader logBufferedReader) {
        List<String> result = new ArrayList<>();
        try {
            result.add(line);
            logBufferedReader.mark(MAX_CHARACTERS_IN_ONE_LINE);
            line = logBufferedReader.readLine();
            while (line != null) {
                if (containsStartPattern(line, logBufferedReader)) {
                    logBufferedReader.reset();
                    return result;
                } else {
                    result.add(line);
                }
                logBufferedReader.mark(MAX_CHARACTERS_IN_ONE_LINE);
                line = logBufferedReader.readLine();
            }
            return result;
        } catch (IOException e) {
            String message = "ReadMultiline failed. Line: '" + line + "', Result: " + result.toString();
            throw new CombinerRuntimeException(message, e);
        }
    }

    /**
     * Is this line the first line in a multi-line record?
     * Return <b>true</b> if the line contains the date and/or time pattern, for example
     * <pre>3.2-SNAPSHOT INFO  2018-04-13 13:19:40</pre>
     * or
     * <pre>3.0.27.1-SNAPSHOT DEBUG 2018-05-21 00:00:00:011 [quartzScheduler_QuartzSchedulerThread] : ...</pre>
     *
     * return <b>false</b> if the line is addition, for example this line
     * <pre>    Scheduler class: 'org.quartz.core.QuartzSchedule...</pre>
     * is addition, because doesn't contain a date pattern.
     *
     * @param line              log line, for example
     *                          <pre>3.2-SNAPSHOT INFO  2019-01-12 13:29:40 [main            ] : Add ... addLast(MutableSources.java:105)</pre>
     * @param logBufferedReader the current reader for identification of {@link Tactic}
     * @return 'true' if the line contains specific pattern
     */
    private boolean containsStartPattern(String line, LogBufferedReader logBufferedReader) {
        Tactic tactic = ApplicationService.getInstance().findSpecificTactic(logBufferedReader);
        return tactic.containsDate(line);
    }

    /**
     * <p>
     * Decide, which {@link NodeLog} will be used for reading.
     * Get a {@link NodeLog#getLogBufferedReader()} from the node log and read a line from it.
     *
     * <p>
     * Change LastUsedNodeLogIndex.
     *
     * @param filesMergerState contains information about last used index and {@link NodeFile}s
     * @return a preferred line from one of {@link LogBufferedReader}s or 'null' if all buffers are empty.
     */
    public String readLineFromReaders(FilesMergerState filesMergerState) {
        try {
            List<LogBufferedReader> logBufferedReaders = collectOpenedBufferedReaders(filesMergerState.getNodeFiles());
            if (logBufferedReaders.isEmpty()) {
                return null;
            }
            List<LineState> lineStates = getLineStates(logBufferedReaders);
            int lastUsedNodeLogIndex = filesMergerState.getLastUsedNodeLogIndex();
            LogBufferedReader logBufferedReader = logBufferedReaders.get(lastUsedNodeLogIndex);
            if (LineState.WITHOUT_DATE == lineStates.get(lastUsedNodeLogIndex)) {
                return logBufferedReader.readLine();
            }
            if (LineState.IS_NULL == lineStates.get(lastUsedNodeLogIndex)) {
                logBufferedReader.close();
                logBufferedReaders = collectOpenedBufferedReaders(filesMergerState.getNodeFiles());
                if (logBufferedReaders.isEmpty()) {
                    return null;
                }
            }
            int logBufferedReaderIndexWithOldestLine = findTheOldest(logBufferedReaders);
            filesMergerState.setLastUsedNodeLogIndex(logBufferedReaderIndexWithOldestLine);
            return logBufferedReaders.get(logBufferedReaderIndexWithOldestLine).readLine();
        } catch (IOException e) {
            throw new CombinerRuntimeException(e);
        }
    }

    private List<LogBufferedReader> collectOpenedBufferedReaders(List<NodeFile> nodeFiles) {
        List<LogBufferedReader> logBufferedReaders = new ArrayList<>();
        for (NodeFile nodeFile : nodeFiles) {
            NodeLog nodeLog = nodeFile.getNodeLog();
            if (nodeLog.getLogBufferedReader().isNotClosed()) {
                logBufferedReaders.add(nodeLog.getLogBufferedReader());
            }
        }
        return logBufferedReaders;
    }

    /**
     * Find out an opened {@link NodeLog#getLogBufferedReader()} of a {@link NodeFile#getNodeLog()},
     * which has the same order number as the
     * {@link FilesMergerState#getLastUsedNodeLogIndex()} index.
     *
     * If all {@link LogBufferedReader}s are closed, then return 'null'.
     *
     * @param filesMergerState contains {@link NodeFile}s with link to {@link LogBufferedReader}s
     * @return required {@link NodeLog#getLogBufferedReader()}
     */
    public LogBufferedReader getCurrentReader(FilesMergerState filesMergerState) {
        List<LogBufferedReader> logBufferedReaders =
            collectOpenedBufferedReaders(filesMergerState.getNodeFiles());

        if (logBufferedReaders.isEmpty()) {
            return null;
        }

        return logBufferedReaders.get(filesMergerState.getLastUsedNodeLogIndex());
    }

    /**
     * Find out the index of {@link LogBufferedReader} with the oldest date
     * @param logBufferedReaders all {@link LogBufferedReader}s
     * @return Index of the oldest line
     * @throws IOException if reading od resetting of a {@link LogBufferedReader} fails.
     */
    private int findTheOldest(List<LogBufferedReader> logBufferedReaders) throws IOException {
        int result = 0;
        Date previousDate = null;
        for (int i = 0; i < logBufferedReaders.size(); i++) {
            LogBufferedReader logBufferedReader = logBufferedReaders.get(i);
            logBufferedReader.mark(ReaderService.MAX_CHARACTERS_IN_ONE_LINE);
            String line = logBufferedReader.readLine();
            logBufferedReader.reset();
            Tactic tactic
                    = ApplicationService.getInstance().findSpecificTactic(logBufferedReader);
            NodeFile nodeFile = NodeFileService.getInstance().findNodeFile(logBufferedReader);
            Date date = tactic.findDate(line, nodeFile);

            if (date != null && (previousDate == null || date.before(previousDate))) {
                previousDate = date;
                result = i;
            }
        }
        return result;
    }

    /**
     * Call the {@link #getLineState(LogBufferedReader)} method for each {@link LogBufferedReader}
     * @param logBufferedReaders the source
     * @return {@link LineState}s of the source {@link LogBufferedReader}s
     */
    private List<LineState> getLineStates(List<LogBufferedReader> logBufferedReaders) {
        List<LineState> result = new ArrayList<>();
        for (LogBufferedReader logBufferedReader : logBufferedReaders) {
            result.add(getLineState(logBufferedReader));
        }
        return result;
    }

    /**
     * Read the first line from the argument and find out the {@link LineState} of the line.
     * @param logBufferedReader the source of the line
     * @return the {@link LineState} of the line
     */
    private LineState getLineState(LogBufferedReader logBufferedReader) {
        try {
            logBufferedReader.mark(ReaderService.MAX_CHARACTERS_IN_ONE_LINE);
            
            String line = logBufferedReader.readLine();
            if (line == null) {
                return LineState.IS_NULL;
            }
            logBufferedReader.reset();

            Tactic tactic
                    = ApplicationService.getInstance().findSpecificTactic(logBufferedReader);
            boolean containsDate = tactic.containsDate(line);
            if (!containsDate) {
                return LineState.WITHOUT_DATE;
            }
            
            return LineState.WITH_DATE;
            
        } catch (IOException e) {
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Get log file of this reader
     * @param logBufferedReader contains {@link LogInputStreamReader} that contains
     *                          {@link LogConcatenatedInputStream} that contains
     *                          {@link LogFileInputStream#getFile()}
     * @return a log file, this {@link LogBufferedReader} reads from
     */
    public File getFile(LogBufferedReader logBufferedReader) {
        LogInputStreamReader logInputStreamReader = (LogInputStreamReader) logBufferedReader.getReader();
        LogConcatenatedInputStream logConcatenatedInputStream = (LogConcatenatedInputStream) logInputStreamReader.getInputStream();
        LogFileInputStream logFileInputStream = logConcatenatedInputStream.getCurrentStream();
        return logFileInputStream.getFile();
    }

    /**
     * Create {@link FileInputStream}s from all log files and collect them
     * to {@link LogBufferedReader}s, where multiple files from the same
     * {@link NodeLog} will be concatenated
     * to a single {@link SequenceInputStream} and wrapped to
     * a {@link LogBufferedReader}. Each {@link NodeLog} will have its own
     * {@link NodeLog#getLogBufferedReader()}.
     *
     * @param applicationLogs holders of {@link NodeLog}s with files.
     */
    public void prepareBufferedReaders(List<ApplicationLog> applicationLogs) {
        long startNanos = System.nanoTime();
        NodeFileService nodeFileService = NodeFileService.getInstance();
        for (ApplicationLog applicationLog : applicationLogs) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(applicationLog)) {
                List<LogFileInputStream> inputStreams = new ArrayList<>();
                for (NodeFile nodeFile : nodeFileService.findNodeFiles(nodeLog)) {
                    try {
                        inputStreams.add(new LogFileInputStream(nodeFile.getFile()));
                    } catch (FileNotFoundException e) {
                        throw new CombinerRuntimeException(e);
                    }
                }
                Enumeration<LogFileInputStream> enumeration = Collections.enumeration(inputStreams);
                LogConcatenatedInputStream logConcatenatedInputStream = new LogConcatenatedInputStream(enumeration);
                LogInputStreamReader logInputStreamReader
                        = new LogInputStreamReader(logConcatenatedInputStream, StandardCharsets.UTF_8);
                LogBufferedReader logBufferedReader = new LogBufferedReader(logInputStreamReader);
                nodeLog.setLogBufferedReader(logBufferedReader);
                for (NodeFile nodeFile : nodeFileService.findNodeFiles(nodeLog)) {
                    nodeFile.setLogBufferedReader(logBufferedReader);
                }
            }
        }
        long durationInNanoseconds = System.nanoTime() - startNanos;
        String durationInMs = durationInNanoseconds / 1000000 + "," + durationInNanoseconds % 1000000;
        logger.info("Duration of prepareBufferedReaders is {} milliseconds", durationInMs);
    }
}
