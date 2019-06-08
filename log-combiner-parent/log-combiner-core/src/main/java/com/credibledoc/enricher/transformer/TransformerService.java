package com.credibledoc.enricher.transformer;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import com.credibledoc.enricher.searchcommand.SearchCommand;

import java.util.List;

/**
 * Contains the {@link #transformToReport(Deriving, List, LogBufferedReader)}
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
     * </li>
     *
     * <li>
     * Apply a {@link Transformer} to the multiLine
     * </li>
     *
     * <li>
     * Write transformed lines to the {@link Deriving#getPrintWriter()} object.
     * </li>
     *
     * </ul>
     *
     * @param deriving          report state
     * @param multiline         a log record from the {@link LogBufferedReader}
     * @param logBufferedReader data source created from a log file. It can be
     *                          useful in case when additional lines should be read
     */
    public void transformToReport(Deriving deriving,
                                  List<String> multiline,
                                  LogBufferedReader logBufferedReader) {
        List<LineProcessor> lineProcessors = LineProcessorService.getInstance().getLineProcessors(deriving);
        for (LineProcessor lineProcessor : lineProcessors) {
            boolean isApplicable = lineProcessor.getSearchCommand().isApplicable(deriving, multiline, logBufferedReader);
            if (isApplicable) {
                String transformed = lineProcessor.getTransformer().transform(deriving, multiline, logBufferedReader);
                if (transformed != null) {
                    deriving.getPrintWriter().write(transformed + System.lineSeparator());
                }
                break;
            }
        }
    }

}
