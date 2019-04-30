package com.credibledoc.combiner.application.identifier;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;

/**
 * Implementations of this interface belongs to a particular
 * {@link Application}.
 * It recognizes a log file and tell if the file belongs to some
 * {@link Application}.
 *
 * @author Kyrylo Semenko
 */
public interface ApplicationIdentifier {

    /**
     * Recognize a line and return 'true' if the line belongs
     * to the {@link Application} returned from the {@link #getApplication()} method.
     * @param line a line from a log file
     * @param logBufferedReader can be used for reading other lines from the current file
     * @return 'false', if the line cannot be recognized
     */
    boolean identifyApplication(String line, LogBufferedReader logBufferedReader);

    /**
     * @return an {@link Application}, this {@link ApplicationIdentifier}
     * belongs to.
     */
    Application getApplication();
}
