package com.credibledoc.combiner.log.reader;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileTreeSet;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

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
    public static final String COMBINER_CORE_MODULE_NAME = "log-combiner-core";

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
     * Read a single log record. It can be multi-line. Position in logBufferedReader will not be changed.
     * <p>
     * Example of a single line:
     * <pre>
     * 3.2-SNAPSHOT INFO  2018-04-13 13:19:40 : RAMJob...
     * </pre>
     * Example of multi lines:
     * <pre>
     * 3.2-SNAPSHOT INFO  2018-04-13 13:19:40 : Schedule...
     *     Scheduler class: 'org.quartz.core.QuartzSchedule...
     *     NOT STARTED.
     * </pre>
     *
     * @param line              the first line of the record.
     * @param logBufferedReader the data source
     * @param combinerContext   the current state
     * @return A single line, multi-line or 'null'.
     */
    public List<String> readMultiline(String line, LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        List<String> result = new ArrayList<>();
        try {
            Date lineDate = logBufferedReader.getLineDate(); // keep the date if exists
            result.add(line);
            logBufferedReader.mark(MAX_CHARACTERS_IN_ONE_LINE);
            line = logBufferedReader.readLine();
            while (line != null) {
                if (containsStartPattern(line, logBufferedReader, combinerContext)) {
                    logBufferedReader.reset();
                    logBufferedReader.setLineDate(lineDate);
                    return result;
                } else {
                    result.add(line);
                }
                logBufferedReader.mark(MAX_CHARACTERS_IN_ONE_LINE);
                line = logBufferedReader.readLine();
            }
            logBufferedReader.setLineDate(lineDate);
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
     * @param combinerContext the current state
     * @return 'true' if the line contains specific pattern
     */
    private boolean containsStartPattern(String line, LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        Tactic tactic = TacticService.getInstance().findTactic(logBufferedReader, combinerContext);
        return tactic.containsDate(line);
    }

    /**
     * Decide which {@link NodeFile} will be used for reading.
     * Get a {@link NodeFile#getLogBufferedReader()} from the node log and read a line from the reader.
     * <p>
     * Change the {@link FilesMergerState#setCurrentNodeFile(NodeFile)}
     * and current position in the {@link NodeFile#getLogBufferedReader()}.
     *
     * @param filesMergerState contains information of last used index and {@link NodeFile}s
     * @return a preferred line from one of {@link LogBufferedReader}s or 'null' if all buffers are empty.
     */
    public String readLineFromReaders(FilesMergerState filesMergerState) {
        try {
            NodeFile currentNodeFile = filesMergerState.getCurrentNodeFile();
            if (currentNodeFile != null) {
                currentNodeFile.getLogBufferedReader().setLineDate(null);
            }
            NodeFile actualNodeFile = findTheOldest(filesMergerState);
            if (actualNodeFile == null) {
                return null;
            }
            LogBufferedReader logBufferedReader = actualNodeFile.getLogBufferedReader();
            Date lineDate = logBufferedReader.getLineDate(); // keep the date if exists, because it was set in the findTheOldest method.
            String line = logBufferedReader.readLine();
            if (line != null) {
                if (lineDate == null) {
                    lineDate = actualNodeFile.getNodeLog().getTactic().findDate(line);
                }
                logBufferedReader.setLineDate(lineDate);
            }
            
            filesMergerState.setCurrentNodeFile(actualNodeFile);
            return line;
        } catch (IOException e) {
            throw new CombinerRuntimeException(e);
        }
    }

    public NodeFile findTheOldest(FilesMergerState filesMergerState) {
        try {
            NodeFile result = filesMergerState.getCurrentNodeFile();
            for (NodeFile nodeFile : filesMergerState.getNodeFiles()) {
                if (result == null) {
                    result = nodeFile;
                } else if (nodeFile != result && nodeFile.getLogBufferedReader() != null &&
                        nodeFile.getLogBufferedReader().isNotClosed()) {
                    result = getOlderNodeFile(result, nodeFile);
                }
            }
            return result;
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    private NodeFile getOlderNodeFile(NodeFile actual, NodeFile next) throws IOException {
        LogBufferedReader nextLogBufferedReader = next.getLogBufferedReader();
        LogBufferedReader actualLogBufferedReader = actual.getLogBufferedReader();
        Date actualLineDate = actualLogBufferedReader.getLineDate();
        if (actualLogBufferedReader.isNotClosed()) {
            actualLogBufferedReader.mark(1);
            if (actualLogBufferedReader.read() == -1) {
                actualLogBufferedReader.close();
                return next;
            }
            actualLogBufferedReader.reset();
            actualLogBufferedReader.setLineDate(actualLineDate);
        } else {
            return next;
        }
        Date nextLineDate = nextLogBufferedReader.getLineDate();
        if (nextLineDate == null) {
            readLineDate(next, nextLogBufferedReader);
            nextLineDate = nextLogBufferedReader.getLineDate();
        }
        if (actualLineDate == null) {
            readLineDate(actual, actualLogBufferedReader);
            actualLineDate = actualLogBufferedReader.getLineDate();
        }
        
        boolean isNextLast = false;
        nextLogBufferedReader.mark(1);
        if (nextLogBufferedReader.read() == -1) {
            isNextLast = true;
        }
        nextLogBufferedReader.reset();
        nextLogBufferedReader.setLineDate(nextLineDate);
        
        boolean isNextLineWithoutDate = nextLineDate == null && !isNextLast;
        boolean isNextNodeFileOlder = actualLineDate != null && isNextLineWithoutDate && next.getDate().before(actualLineDate);
        boolean isNextLineOlder = nextLineDate != null && actualLineDate != null && nextLineDate.before(actualLineDate);
        if (isNextNodeFileOlder || isNextLineOlder) {
            // older line wins
            return next;
        }
        return actual;
    }

    private void readLineDate(NodeFile nodeFile, LogBufferedReader nextLogBufferedReader) throws IOException {
        nextLogBufferedReader.mark(ReaderService.MAX_CHARACTERS_IN_ONE_LINE);
        String nextLine = nextLogBufferedReader.readLine();
        nextLogBufferedReader.reset();
        if (nextLine != null) {
            Tactic nextTactic = nodeFile.getNodeLog().getTactic();
            Date date = nextTactic.findDate(nextLine, nodeFile);
            nextLogBufferedReader.setLineDate(date);
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
     * Create {@link FileInputStream}s from all log files and set them to {@link NodeFile}s from the combinerContext.
     *
     * @param combinerContext the holder of {@link NodeFile}s with files for parsing.
     */
    public void prepareBufferedReaders(CombinerContext combinerContext) {
        NodeFileTreeSet<NodeFile> nodeFiles = combinerContext.getNodeFileRepository().getNodeFiles();
        prepareBufferedReaders(combinerContext, nodeFiles);
    }

    /**
     * Create {@link FileInputStream}s from {@link NodeFile}s and set them to {@link NodeFile}s from the combinerContext.
     *
     * @param combinerContext the holder of {@link NodeFile}s with files for parsing.
     * @param nodeFiles that should be used.
     */
    public void prepareBufferedReaders(CombinerContext combinerContext, NodeFileTreeSet<NodeFile> nodeFiles) {
        try {
            long startNanos = System.nanoTime();
            for (NodeFile nodeFile : nodeFiles) {
                List<LogFileInputStream> inputStreams = new ArrayList<>();
                inputStreams.add(new LogFileInputStream(nodeFile.getFileWithSources().getFile()));
                Enumeration<LogFileInputStream> enumeration = Collections.enumeration(inputStreams);
                LogConcatenatedInputStream logConcatenatedInputStream = new LogConcatenatedInputStream(enumeration);
                // TODO Kyrylo Semenko - charset from combinerContext
                LogInputStreamReader logInputStreamReader
                    = new LogInputStreamReader(logConcatenatedInputStream, StandardCharsets.UTF_8);
                LogBufferedReader logBufferedReader = new LogBufferedReader(logInputStreamReader);
                if (nodeFile.getLogBufferedReader() != null && nodeFile.getLogBufferedReader().isNotClosed()) {
                    throw new CombinerRuntimeException("LogBufferedReader is not closed yet. Expected 'null' or closed LogBufferedReader.");
                }
                nodeFile.setLogBufferedReader(logBufferedReader);
            }
            long durationInNanoseconds = System.nanoTime() - startNanos;
            String durationInMs = durationInNanoseconds / 1000000 + "," + durationInNanoseconds % 1000000;
            logger.trace("Duration of prepareBufferedReaders is {} milliseconds", durationInMs);
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

}
