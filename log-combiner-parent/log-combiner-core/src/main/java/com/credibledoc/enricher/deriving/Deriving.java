package com.credibledoc.enricher.deriving;

import java.io.PrintWriter;
import java.util.List;

/**
 * This data object contains a report document state.
 *
 * @author Kyrylo Semenko
 */
public interface Deriving {
    
    /**
     * @return PrintWriter is an object where transformed lines will be printed to a file.
     */
    PrintWriter getPrintWriter();

    /**
     * @return This list contains transformed lines prepared for printing to the {@link #getPrintWriter()}.
     */
    List<String> getCacheLines();
}
