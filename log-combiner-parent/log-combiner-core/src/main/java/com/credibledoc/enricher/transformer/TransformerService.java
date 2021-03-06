package com.credibledoc.enricher.transformer;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import com.credibledoc.enricher.searchcommand.SearchCommand;

import java.util.List;

/**
 * Contains the {@link #transformToReport(Printable, List, LogBufferedReader, CombinerContext, EnricherContext)}
 * method.
 *
 * @author Kyrylo Semenko
 */
public class TransformerService {

    /**
     * Singleton.
     */
    private static final TransformerService instance = new TransformerService();

    /**
     * @return The {@link TransformerService} singleton.
     */
    public static TransformerService getInstance() {
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
     * @param combinerContext           the current state
     * @param enricherContext   the current state
     */
    public void transformToReport(Printable printable,
                                  List<String> multiline,
                                  LogBufferedReader logBufferedReader,
                                  CombinerContext combinerContext,
                                  EnricherContext enricherContext) {
        List<LineProcessor> lineProcessors = LineProcessorService.getInstance().getLineProcessors(printable, enricherContext);
        for (LineProcessor lineProcessor : lineProcessors) {
            boolean isApplicable = lineProcessor.getSearchCommand().isApplicable(printable, multiline, logBufferedReader);
            if (isApplicable) {
                String transformed = lineProcessor.getTransformer()
                    .transform(printable, multiline, logBufferedReader, combinerContext);
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
