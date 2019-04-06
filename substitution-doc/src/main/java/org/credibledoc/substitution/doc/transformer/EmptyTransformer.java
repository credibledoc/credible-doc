package org.credibledoc.substitution.doc.transformer;

import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Copy lines as is, without transformation.
 * @author Kyrylo Semenko
 */
@Component
public class EmptyTransformer implements Transformer {

    /**
     * We need to know, if the current document is empty or contains some rows.
     * We need it for decision, if wee should create a new document, or we
     * should remain the current document for filling.
     */
    static final int MIN_LINES_COUNT_FOR_DECISION = 2;

    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        if (reportDocument.getCacheLines().size() < MIN_LINES_COUNT_FOR_DECISION) {
            reportDocument.getCacheLines().add(multiLine.get(0));
        }
        return String.join(System.lineSeparator(), multiLine);
    }

}
