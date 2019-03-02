package org.credibledoc.substitution.doc.log.reader;

import com.credibledoc.substitution.exception.SubstitutionRuntimeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.credibledoc.substitution.doc.application.ApplicationService;
import org.credibledoc.substitution.doc.line.LineState;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.log.buffered.LogConcatenatedInputStream;
import org.credibledoc.substitution.doc.log.buffered.LogFileInputStream;
import org.credibledoc.substitution.doc.log.buffered.LogInputStreamReader;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLog;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLogService;
import org.credibledoc.substitution.doc.node.file.NodeFile;
import org.credibledoc.substitution.doc.node.file.NodeFileService;
import org.credibledoc.substitution.doc.node.log.NodeLog;
import org.credibledoc.substitution.doc.node.log.NodeLogService;
import org.credibledoc.substitution.doc.report.Report;
import org.credibledoc.substitution.doc.report.ReportService;
import org.credibledoc.substitution.doc.specific.SpecificTactic;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Reads lines from log files.
 * Contains methods for working with file readers, for example {@link LogBufferedReader}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ReaderService {
    
    @NonNull
    private final NodeFileService nodeFileService;

    @NonNull
    private final ApplicationService applicationService;

    @NonNull
    private final NodeLogService nodeLogService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private final ApplicationLogService applicationLogService;

    /** How many characters can be marked for reset a stream, see the {@link Reader#mark(int)} and {@link Reader#reset()} methods. */
    private static final int MAX_CHARACTERS_IN_ONE_LINE = 99999;

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
            throw new SubstitutionRuntimeException(message, e);
        }
    }

    /**
     * Is this line the first line in a multi-line record?
     *
     * @param line              log line, for example
     *                          <pre>3.2-SNAPSHOT INFO  2019-01-12 13:29:40 [main            ] : Add ... addLast(MutableSources.java:105)</pre>
     * @param logBufferedReader the current reader for identification of {@link SpecificTactic}
     * @return <b>true</b> if the line contains the date and/or time pattern, for example
     * <pre>3.2-SNAPSHOT INFO  2018-04-13 13:19:40<pre/>
     * or
     * <pre>3.0.27.1-SNAPSHOT DEBUG 2018-05-21 00:00:00:011 [quartzScheduler_QuartzSchedulerThread] : ...<pre/>
     *
     * return <b>false</b> if the line is addition, for example this line
     * <pre>    Scheduler class: 'org.quartz.core.QuartzSchedule...</pre>
     * is addition, because does not contains a date pattern.
     */
    private boolean containsStartPattern(String line, LogBufferedReader logBufferedReader) {
        SpecificTactic specificTactic = applicationService.findSpecificTactic(logBufferedReader);
        return specificTactic.containsDate(line);
    }

    /**
     * <p>
     * Decide, which {@link NodeLog} will be used for reading.
     * Get a {@link NodeLog#getLogBufferedReader()} from the node log and read a line from it.
     *
     * @return a preferred line from one of {@link LogBufferedReader}s or 'null' if all buffers are empty.
     */
    public String readLineFromReaders() {
        Report report = reportService.getReport();
        try {
            List<LogBufferedReader> logBufferedReaders = collectBufferedReaders();
            if (logBufferedReaders.isEmpty()) {
                return null;
            }
            List<LineState> lineStates = getLineStates(logBufferedReaders);
            int lastUsedNodeLogIndex = report.getLastUsedNodeLogIndex();
            LogBufferedReader logBufferedReader = logBufferedReaders.get(lastUsedNodeLogIndex);
            if (LineState.WITHOUT_DATE == lineStates.get(lastUsedNodeLogIndex)) {
                return logBufferedReader.readLine();
            }
            if (LineState.IS_NULL == lineStates.get(lastUsedNodeLogIndex)) {
                logBufferedReader.close();
                logBufferedReaders = collectBufferedReaders();
                if (logBufferedReaders.isEmpty()) {
                    return null;
                }
            }
            int logBufferedReaderIndexWithOldestLine = findTheOldest(logBufferedReaders);
            report.setLastUsedNodeLogIndex(logBufferedReaderIndexWithOldestLine);
            return logBufferedReaders.get(logBufferedReaderIndexWithOldestLine).readLine();
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private List<LogBufferedReader> collectBufferedReaders() {
        List<LogBufferedReader> logBufferedReaders = new ArrayList<>();
        for (ApplicationLog applicationLog : applicationLogService.getApplicationLogs()) {
            for (NodeLog nodeLog : nodeLogService.findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader().isNotClosed()) {
                    logBufferedReaders.add(nodeLog.getLogBufferedReader());
                }
            }
        }
        return logBufferedReaders;
    }

    /**
     * Find out a {@link NodeLog#getLogBufferedReader()} of the {@link NodeLog},
     * which has the same order number as the
     * {@link Report#getLastUsedNodeLogIndex()} index.
     *
     * @param report contains a {@link Report#getLastUsedNodeLogIndex()}
     * @return required {@link NodeLog#getLogBufferedReader()}
     */
    public LogBufferedReader getCurrentReader(Report report) {
        int nextNumber = 0;
        for (ApplicationLog applicationLog : applicationLogService.getApplicationLogs()) {
            for (NodeLog nodeLog : nodeLogService.findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader().isNotClosed()) {
                    if (report.getLastUsedNodeLogIndex() == nextNumber) {
                        return nodeLog.getLogBufferedReader();
                    }
                    nextNumber++;
                }
            }
        }
        throw new SubstitutionRuntimeException("No BufferedReaders found. Expected at least one.");
    }

    /**
     * Find out the index of {@link LogBufferedReader} with the oldest date
     */
    private int findTheOldest(List<LogBufferedReader> logBufferedReaders) throws IOException {
        int result = 0;
        Date previousDate = null;
        for (int i = 0; i < logBufferedReaders.size(); i++) {
            LogBufferedReader logBufferedReader = logBufferedReaders.get(i);
            logBufferedReader.mark(ReaderService.MAX_CHARACTERS_IN_ONE_LINE);
            String line = logBufferedReader.readLine();
            logBufferedReader.reset();
            SpecificTactic specificTactic
                    = applicationService.findSpecificTactic(logBufferedReader);
            NodeFile nodeFile = nodeFileService.findNodeFile(logBufferedReader);
            Date date = specificTactic.findDate(line, nodeFile);

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

            SpecificTactic specificTactic
                    = applicationService.findSpecificTactic(logBufferedReader);
            boolean containsDate = specificTactic.containsDate(line);
            if (!containsDate) {
                return LineState.WITHOUT_DATE;
            }
            
            return LineState.WITH_DATE;
            
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
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
        for (ApplicationLog applicationLog : applicationLogs) {
            for (NodeLog nodeLog : nodeLogService.findNodeLogs(applicationLog)) {
                List<LogFileInputStream> inputStreams = new ArrayList<>();
                for (NodeFile nodeFile : nodeFileService.findNodeFiles(nodeLog)) {
                    try {
                        inputStreams.add(new LogFileInputStream(nodeFile.getFile()));
                    } catch (FileNotFoundException e) {
                        throw new SubstitutionRuntimeException(e);
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
        log.info("Duration of prepareBufferedReaders is {} milliseconds", durationInMs);
    }
}
