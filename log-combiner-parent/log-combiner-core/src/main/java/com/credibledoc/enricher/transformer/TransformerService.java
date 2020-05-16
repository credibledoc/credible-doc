package com.credibledoc.enricher.transformer;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import com.credibledoc.enricher.searchcommand.SearchCommand;

import java.util.List;

/**
 * Contains the {@link #transformToReport(Printable, List, LogBufferedReader, Context)}
 * method.
 *
 * @author Kyrylo Semenko
 */
public class TransformerService {

    /**
     * Singleton.
     */
    private static TransformerService instance;

    /**
     * @return The {@link TransformerService} singleton.
     */
    public static TransformerService getInstance() {
        if (instance == null) {
            instance = new TransformerService();
        }
        return instance;
    }
    
    /**
     * For each {@link LineProcessor}:<br>
     * <ul>
     *
     * <li>
     * Check a multiLine by {@link SearchCommand}
     * for decision a {@link Transformer} should be applied, and if so, then
     *
     * <li>
     * Apply a {@link Transformer} to the multiLine
     *
     * <li>
     * Write transformed lines to the {@link Printable#getPrintWriter()} object.
     *
     * <li>
     * If the {@link Printable#checkAllLineProcessors()} is 'false', only the first applicable {@link LineProcessor}
     * will be processed. Else all applicable line processors will be processed.
     *
     * </ul>
     *
     * @param printable         report state
     * @param multiline         a log record from the {@link LogBufferedReader}
     * @param logBufferedReader data source created from a log file. It can be
     *                          useful in case when additional lines should be read from this reader
     *                          or for access to a source file.
     * @param context           the current state
     */
    public void transformToReport(Printable printable,
                                  List<String> multiline,
                                  LogBufferedReader logBufferedReader,
                                  Context context) {
        List<LineProcessor> lineProcessors = LineProcessorService.getInstance().getLineProcessors(printable);
        for (LineProcessor lineProcessor : lineProcessors) {
            boolean isApplicable = lineProcessor.getSearchCommand().isApplicable(printable, multiline, logBufferedReader);
            if (isApplicable) {
                String transformed = lineProcessor.getTransformer()
                    .transform(printable, multiline, logBufferedReader, context);
                if (transformed != null) {
                    printable.getPrintWriter().write(transformed + System.lineSeparator());
                }
                if (!printable.checkAllLineProcessors()) {
                    break;
                }
            }
        }
    }

}
