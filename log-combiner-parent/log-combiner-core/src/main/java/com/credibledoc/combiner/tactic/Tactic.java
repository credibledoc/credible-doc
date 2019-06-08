package com.credibledoc.combiner.tactic;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.file.NodeFile;

import java.io.File;
import java.util.Date;

/**
 * This interface contains methods specific for particular application log,
 * for example {@link #findDate(String)}. In case of merging log files with different date formats,
 * each format should have its own implementation of this {@link Tactic} interface.
 *
 * @author Kyrylo Semenko
 */
public interface Tactic {

    /**
     * Find out the oldest date and time in a file content.
     *
     * @param file the log file
     * @return a date and time of the first line with a date stamp.
     */
    Date findDate(File file);

    /**
     * Find out a date and time in a line. In some cases a line contains a
     * date and time stamp, for example <b>29.09.2018 22:28:40.029</b>.
     * In other cases a line contains a time stamp only, for example
     * <b>16:59:01.931</b>. In this cases a date can be found in the
     * {@link NodeFile#getDate()} field.
     *
     * @param line     a line from a log file
     * @param nodeFile in case when a line contains a time stamp only, the
     *                 {@link NodeFile#getDate()} will be used
     * @return 'null' if the line does not contains a time stamp
     */
    Date findDate(String line, NodeFile nodeFile);

    /**
     * Search for a date in a line.
     *
     * @param line from a log file
     * @return 'true' if the line contains a date pattern. Else return 'false'.
     * If the line is null, return 'false'.
     */
    boolean containsDate(String line);

    /**
     * Parse a date string from a log line.
     *
     * @param line for example
     *             <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1...</pre>
     * @return a parsed string, for example 13.04.2018 07:27:41.462 or 'null' if the line is null or the date cannot be found.
     */
    String parseDateStingFromLine(String line);

    /**
     * Find out thread name.
     *
     * <p>
     * Example of implementation:
     * <pre>{@code
     *     private static final String THREAD_SEPARATOR_PREFIX = " [";
     *     private static final int MAX_INDEX_OF_THREAD_SEPARATOR_PREFIX = 35;
     *     private static final String TREAD_SEPARATOR_SUFFIX = "] ";
     *
     *     @Override
     *     public String findThreadName(String line) {
     *         int beginIndex = line.indexOf(THREAD_SEPARATOR_PREFIX);
     *         if (beginIndex == -1 || beginIndex > MAX_INDEX_OF_THREAD_SEPARATOR_PREFIX) {
     *             return null;
     *         }
     *         int endIndex = line.indexOf(TREAD_SEPARATOR_SUFFIX, beginIndex);
     *         if (endIndex == -1) {
     *             return null;
     *         }
     *         return line.substring(beginIndex + THREAD_SEPARATOR_PREFIX.length(), endIndex);
     *     }
     * }</pre>
     * 
     * @param line for example
     *             <pre>29.09.2018 22:53:42.494 [https-jsse-nio-15443-exec-1] Execution ...</pre>
     * @return Thread name, for example <b>https-jsse-nio-15443-exec-1</b>
     */
    String findThreadName(String line);

    /**
     * Parse {@link Date} from a line
     * @param line the line with a date
     * @return 'null' if a date cannot be found
     */
    Date findDate(String line);

    /**
     * This name is used for identification of log lines in created
     * reports. It should be short and unique for each application.
     * implementations.
     *
     * @return for example 'my-app'
     */
    String getShortName();

    /**
     * Recognize a line and return 'true' if the line belongs
     * to this {@link Tactic}.
     *
     * @param line              a line from a log file
     * @param logBufferedReader can be used for reading other lines from the current file
     * @return 'false', if the line cannot be recognized
     */
    boolean identifyApplication(String line, LogBufferedReader logBufferedReader);
}
