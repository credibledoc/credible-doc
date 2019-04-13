package com.credibledoc.substitution.doc.transformer;

import com.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.searchcommand.SearchCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Contains the {@link #transformToReport(ReportDocument, List, LogBufferedReader)}
 * method.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TransformerService {
    
    public static final String INDENTATION = "    ";

    @NonNull
    private final LineProcessorService lineProcessorService;
    
    /**
     * For each {@link LineProcessor}:<br>
     * <ul>
     *
     * <li>
     * Check the multiLine by {@link SearchCommand}
     * for decision a {@link Transformer} should be applied, and if so, then
     * </li>
     *
     * <li>
     * Apply a {@link Transformer} to the multiLine
     * </li>
     *
     * </ul>
     * Flush {@link ReportDocument#getPrintWriter()} at the end.
     *
     * @param reportDocument the state of the current report
     * @param multiline a log record from the {@link LogBufferedReader}
     * @param logBufferedReader data source created from a log file. It can be
     * useful in case when additional lines should be found in the
     * {@link LogBufferedReader}.
     */
    public void transformToReport(ReportDocument reportDocument,
                                  List<String> multiline,
                                  LogBufferedReader logBufferedReader) {
        List<LineProcessor> lineProcessors = lineProcessorService.getLineProcessors(reportDocument);
        for (LineProcessor lineProcessor : lineProcessors) {
            boolean isApplicable = lineProcessor.getSearchCommand().isApplicable(reportDocument, multiline, logBufferedReader);
            if (isApplicable) {
                String transformed = lineProcessor.getTransformer().transform(reportDocument, multiline, logBufferedReader);
                if (transformed != null) {
                    reportDocument.getPrintWriter().write(transformed + System.lineSeparator());
                }
                break;
            }
        }
    }

}
