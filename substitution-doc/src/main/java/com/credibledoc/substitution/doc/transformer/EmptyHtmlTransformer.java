package com.credibledoc.substitution.doc.transformer;

import com.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.filesmerger.node.log.NodeLogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import com.credibledoc.substitution.doc.filesmerger.application.Application;
import com.credibledoc.substitution.doc.filesmerger.application.ApplicationService;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * Escapes the multiLine obtained in the
 * {@link #transform(ReportDocument, List, LogBufferedReader)} method
 * by applying the
 * {@link StringEscapeUtils#escapeHtml4(String)} method.
 * 
 * @author Kyrylo Semenko
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmptyHtmlTransformer implements Transformer {

    @NonNull
    private final NodeLogService nodeLogService;

    @NonNull
    private final ApplicationService applicationService;

    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String line = multiLine.get(0);
        if (reportDocument.getCacheLines().size() < EmptyTransformer.MIN_LINES_COUNT_FOR_DECISION) {
            reportDocument.getCacheLines().add(line);
        }
        String joined = String.join(System.lineSeparator(), multiLine);
        String nodeName = nodeLogService.findNodeName(logBufferedReader);
        Application application = applicationService.findApplication(logBufferedReader);
        return StringEscapeUtils.escapeHtml4(nodeName + " " + application.getShortName() + " " + joined);
    }

}
