package com.credibledoc.substitution.doc.module.substitution.activity;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.substitution.doc.module.substitution.activity.everyline.EveryLineSearchCommand;
import com.credibledoc.substitution.doc.module.substitution.logmessage.LogMessageService;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.report.document.Document;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import com.credibledoc.enricher.transformer.Transformer;
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

    /**
     * Create a stateful object of {@link ReportDocument} type.
     *
     * @param enricherContext the current state
     * @return The stateful object, which {@link ReportDocument#getCacheLines()} method.
     * It is used for generating PlantUML activity diagram.
     */
    public ReportDocument prepareReportDocument(EnricherContext enricherContext) {
        Document document = new Document();
        document.setReportDocumentType(UmlDiagramType.class);

        List<LineProcessor> lineProcessors = new ArrayList<>();
        lineProcessors.add(
                new LineProcessor(
                        applicationContext.getBean(EveryLineSearchCommand.class),
                        applicationContext.getBean(AnyLineTransformer.class),
                        document));

        LineProcessorService.getInstance().getLineProcessors(enricherContext).addAll(lineProcessors);
        log.info("Line processors prepared");
        return document;
    }

    @Override
    public Class<? extends ReportDocumentType> getReportDocumentType() {
        return UmlDiagramType.class;
    }

    /**
     * Create a part of PlantUML activity diagram, for example
     * <pre>
     *     |Swimlane1|
     *         :foo4;
     * </pre>
     * from the <i>04.03.2019 18:41:13.658|main|INFO |com.credibledoc.substitution.core.configuration.ConfigurationService - Properties loaded by ClassLoader from the resource: file..</i> line.
     */
    @Service
    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    public static class AnyLineTransformer implements Transformer {

        @NonNull
        public final LogMessageService logMessageService;

        @Override
        public String transform(Printable printable, List<String> multiLine,
                                LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
            String currentSwimlane = parseClassName(multiLine.get(0));
            int maxRowLength = currentSwimlane.length() * 2 + currentSwimlane.length() / 2;
            String message = logMessageService.parseMessage(multiLine.get(0), maxRowLength);
            String result = "|" + currentSwimlane + "|" + LogMessageService.LINE_SEPARATOR +
                LogMessageService.FOUR_SPACES + ":" + message + ";" + LogMessageService.LINE_SEPARATOR;

            printable.getCacheLines().add(result);

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
