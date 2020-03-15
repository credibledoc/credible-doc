package com.credibledoc.substitution.doc.module.substitution.empty;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.transformer.Transformer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * Escapes the multiLine obtained in the
 * {@link Transformer#transform(Printable, List, LogBufferedReader, Context)} method
 * by applying the
 * {@link StringEscapeUtils#escapeHtml4(String)} method.
 * 
 * @author Kyrylo Semenko
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmptyHtmlTransformer implements Transformer {

    @Override
    public String transform(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader,
                            Context context) {
        String line = multiLine.get(0);
        if (printable.getCacheLines().size() < EmptyTransformer.MIN_LINES_COUNT_FOR_DECISION) {
            printable.getCacheLines().add(line);
        }
        String joined = String.join(System.lineSeparator(), multiLine);
        String nodeName = NodeLogService.getInstance().findNodeName(logBufferedReader, context);
        Tactic tactic = TacticService.getInstance().findTactic(logBufferedReader, context);
        return StringEscapeUtils.escapeHtml4(nodeName + " " + tactic.getShortName() + " " + joined);
    }

}
