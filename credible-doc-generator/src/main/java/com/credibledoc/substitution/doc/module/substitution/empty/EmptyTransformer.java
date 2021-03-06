package com.credibledoc.substitution.doc.module.substitution.empty;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.transformer.Transformer;
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
    public String transform(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader,
                            CombinerContext combinerContext) {
        if (printable.getCacheLines().size() < MIN_LINES_COUNT_FOR_DECISION) {
            printable.getCacheLines().add(multiLine.get(0));
        }
        return String.join(System.lineSeparator(), multiLine);
    }

}
