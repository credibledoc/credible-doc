package com.credibledoc.combiner.doc.module.combiner.activity.anyline;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.combiner.doc.module.combiner.activity.AnyLineSearchCommand;
import com.credibledoc.combiner.doc.module.combiner.logmessage.LogMessageService;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentType;
import com.credibledoc.combiner.doc.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.combiner.doc.transformer.LineProcessor;
import com.credibledoc.combiner.doc.transformer.LineProcessorService;
import com.credibledoc.combiner.doc.transformer.Transformer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates document with UML part of a {@link Placeholder}.
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ActivityUmlReportService implements ReportDocumentCreator {

    @NonNull
    private final ApplicationContext applicationContext;

    @NonNull
    private final LineProcessorService lineProcessorService;

    /**
     * Create a stateful object of {@link ReportDocument} type.
     *
     * @return The stateful object, which {@link ReportDocument#getCacheLines()} method
     * will be used for generation of PlantUML activity diagram.
     */
    public ReportDocument prepareReportDocument() {
        ReportDocument reportDocument = applicationContext.getBean(ReportDocument.class);
        reportDocument.setReportDocumentType(ReportDocumentType.DOCUMENT_PART_UML);

        List<LineProcessor> lineProcessors = new ArrayList<>();
        lineProcessors.add(
                new LineProcessor(
                        applicationContext.getBean(AnyLineSearchCommand.class),
                        applicationContext.getBean(AnyLineTransformer.class),
                        reportDocument));

        lineProcessorService.getLineProcessors().addAll(lineProcessors);
        log.info("Line processors prepared");
        return reportDocument;
    }

    @Override
    public ReportDocumentType getReportDocumentType() {
        return ReportDocumentType.DOCUMENT_PART_UML;
    }

    /**
     * Create a part of PlantUML activity diagram, for example
     * <pre>
     *     |Swimlane1|
     *         :foo4;
     * </pre>
     * from the <i>04.03.2019 18:41:13.658|main|INFO |com.credibledoc.combiner.core.configuration.ConfigurationService - Properties loaded by ClassLoader from the resource: file..</i> line.
     */
    @Service
    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    public static class AnyLineTransformer implements Transformer {

        @NonNull
        public final LogMessageService logMessageService;

        @Override
        public String transform(ReportDocument reportDocument, List<String> multiLine,
                                LogBufferedReader logBufferedReader) {
            String currentSwimlane = parseClassName(multiLine.get(0));
            int maxRowLength = currentSwimlane.length() * 2 + currentSwimlane.length() / 2;
            String message = logMessageService.parseMessage(multiLine.get(0), maxRowLength);
            String result = "|" + currentSwimlane + "|" + LogMessageService.LINE_SEPARATOR +
                LogMessageService.FOUR_SPACES + ":" + message + ";" + LogMessageService.LINE_SEPARATOR;

            reportDocument.getCacheLines().add(result);

            return null;
        }

        private String parseClassName(String line) {
            int separatorIndex = line.indexOf(LogMessageService.LOG_SEPARATOR);
            String firstPart = line.substring(0, separatorIndex);
            int lastDotIndex = firstPart.lastIndexOf(LogMessageService.DOT);
            return firstPart.substring(lastDotIndex + LogMessageService.DOT.length());
        }
    }
}
