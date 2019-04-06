package org.credibledoc.substitution.doc.searchcommand;

import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;

import java.util.List;

/**
 * Contains the {@link #isApplicable(ReportDocument, List, LogBufferedReader)} method.
 *
 * @author Kyrylo Semenko
 */
public interface SearchCommand {

    /**
     * The method returns true for lines which has to be processed
     * because it suits the conditions defined in the method.
     *
     * @param reportDocument the state object
     * @param multiLine last lines from the {@link LogBufferedReader} data source
     * @param logBufferedReader the data source created from log files
     * @return transformed string without a line separator at the end
     */
    boolean isApplicable(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader);

}
