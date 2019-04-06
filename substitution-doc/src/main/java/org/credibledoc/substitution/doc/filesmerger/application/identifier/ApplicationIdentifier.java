package org.credibledoc.substitution.doc.filesmerger.application.identifier;

import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.module.tactic.TacticHolder;

/**
 * Implementations of this interface belongs to a particular
 * {@link TacticHolder}.
 * It recognizes a log file and tell if the file belongs to some
 * {@link TacticHolder}.
 *
 * @author Kyrylo Semenko
 */
public interface ApplicationIdentifier {

    /**
     * Recognize a line and return 'true' if the line belongs
     * to the {@link TacticHolder} returned from the {@link #getSpecificTacticHolder()} method.
     * @param line a line from a log file
     * @param logBufferedReader can be used for reading other lines from the current file
     * @return 'false', if the line cannot be recognized
     */
    boolean identifyApplication(String line, LogBufferedReader logBufferedReader);

    /**
     * @return an {@link TacticHolder}, this {@link ApplicationIdentifier}
     * belongs to.
     */
    TacticHolder getSpecificTacticHolder();
}
