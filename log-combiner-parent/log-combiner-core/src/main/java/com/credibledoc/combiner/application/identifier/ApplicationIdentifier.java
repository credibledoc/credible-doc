package com.credibledoc.combiner.application.identifier;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.tactic.Tactic;

/**
 * Implementations of this interface belongs to a particular
 * {@link Tactic}.
 * It recognizes a log file and tell if the file belongs to some
 * {@link Tactic}.
 *
 * @author Kyrylo Semenko
 */
public interface ApplicationIdentifier {

    /**
     * Recognize a line and return 'true' if the line belongs
     * to the {@link Tactic} returned from the {@link #getTactic()} method.
     * @param line a line from a log file
     * @param logBufferedReader can be used for reading other lines from the current file
     * @return 'false', if the line cannot be recognized
     */
    boolean identifyApplication(String line, LogBufferedReader logBufferedReader);

    /**
     * @return an {@link Tactic}, this {@link ApplicationIdentifier}
     * belongs to.
     */
    Tactic getTactic();
}
