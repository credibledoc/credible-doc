package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.report.document.Document;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
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
public class LaunchingUmlReportService implements ReportDocumentCreator {

    @NonNull
    private final ApplicationContext applicationContext;

    /**
     * Create a stateful object of {@link ReportDocument} type.
     *
     * @param enricherContext the current state
     * @return The stateful object, which {@link ReportDocument#getCacheLines()} method
     * will be used for generating PlantUML activity diagram.
     */
    public ReportDocument prepareReportDocument(EnricherContext enricherContext) {
        Document document = new Document();
        document.setReportDocumentType(UmlDiagramType.class);

        List<LineProcessor> lineProcessors = new ArrayList<>();
        lineProcessors.add(
            new LineProcessor(
                    applicationContext.getBean(LaunchingSearchCommand.class),
                    applicationContext.getBean(LaunchingTransformer.class),
                    document));
        
        lineProcessors.add(
            new LineProcessor(
                    applicationContext.getBean(ConfigurationLoadingSearchCommand.class),
                    applicationContext.getBean(ConfigurationLoadingTransformer.class),
                    document));
        
        lineProcessors.add(
            new LineProcessor(
                    applicationContext.getBean(ContentReplacedSearchCommand.class),
                    applicationContext.getBean(ContentReplacedTransformer.class),
                    document));
        
        lineProcessors.add(
            new LineProcessor(
                applicationContext.getBean(FinishedSearchCommand.class),
                applicationContext.getBean(FinishedTransformer.class),
                document));

        LineProcessorService.getInstance().getLineProcessors(enricherContext).addAll(lineProcessors);
        log.info("Line processors prepared");
        return document;
    }

    @Override
    public Class<? extends ReportDocumentType> getReportDocumentType() {
        return UmlDiagramType.class;
    }

}
