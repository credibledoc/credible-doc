package com.credibledoc.enricher.printable;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;

import java.io.PrintWriter;
import java.util.List;

/**
 * This data object contains a report document state.
 *
 * @author Kyrylo Semenko
 */
public interface Printable {
    
    /**
     * @return PrintWriter is an object where transformed lines will be printed to a file.
     */
    PrintWriter getPrintWriter();

    /**
     * @return This list contains transformed lines prepared for printing by the {@link #getPrintWriter()}.
     */
    List<String> getCacheLines();

    /**
     * @return If this field contains 'true', all {@link com.credibledoc.enricher.line.LineProcessor}s belonging to
     * this {@link Printable} will be processed in the
     * {@link com.credibledoc.enricher.transformer.TransformerService#transformToReport(Printable, List, LogBufferedReader, com.credibledoc.combiner.context.Context)}
     * method. Else the first matched transformer will be processed only.
     */
    boolean checkAllLineProcessors();
}
