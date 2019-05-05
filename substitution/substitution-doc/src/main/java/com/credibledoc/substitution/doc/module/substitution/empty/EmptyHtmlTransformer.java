package com.credibledoc.substitution.doc.module.substitution.empty;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.transformer.Transformer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * Escapes the multiLine obtained in the
 * {@link Transformer#transform(Deriving, List, LogBufferedReader)} method
 * by applying the
 * {@link StringEscapeUtils#escapeHtml4(String)} method.
 * 
 * @author Kyrylo Semenko
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmptyHtmlTransformer implements Transformer {

    @Override
    public String transform(Deriving deriving, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String line = multiLine.get(0);
        if (deriving.getCacheLines().size() < EmptyTransformer.MIN_LINES_COUNT_FOR_DECISION) {
            deriving.getCacheLines().add(line);
        }
        String joined = String.join(System.lineSeparator(), multiLine);
        String nodeName = NodeLogService.getInstance().findNodeName(logBufferedReader);
        Application application = ApplicationService.getInstance().findApplication(logBufferedReader);
        return StringEscapeUtils.escapeHtml4(nodeName + " " + application.getShortName() + " " + joined);
    }

}
