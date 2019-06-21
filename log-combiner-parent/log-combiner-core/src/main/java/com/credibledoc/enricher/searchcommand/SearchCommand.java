package com.credibledoc.enricher.searchcommand;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.deriving.Printable;

import java.util.List;

/**
 * Contains the {@link #isApplicable(Printable, List, LogBufferedReader)} method.
 *
 * @author Kyrylo Semenko
 */
public interface SearchCommand {

    /**
     * The method returns true for lines which has to be processed
     * because it suits the conditions defined in the method.
     *
     * @param printable the state object
     * @param multiLine last lines from the {@link LogBufferedReader} data source
     * @param logBufferedReader the data source created from log files
     * @return transformed string without a line separator at the end
     */
    boolean isApplicable(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader);

}
