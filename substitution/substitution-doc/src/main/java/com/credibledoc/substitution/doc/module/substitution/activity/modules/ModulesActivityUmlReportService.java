package com.credibledoc.substitution.doc.module.substitution.activity.modules;

import com.credibledoc.substitution.doc.module.substitution.activity.AnyLineSearchCommand;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates document with data for dependency UML diagram.
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ModulesActivityUmlReportService implements ReportDocumentCreator {

    @NonNull
    private final ApplicationContext applicationContext;

    /**
     * Create a stateful object of {@link ReportDocument} type.
     *
     * @return The stateful object, which {@link ReportDocument#getCacheLines()} method
     * will be used for generation of PlantUML activity diagram.
     */
    public ReportDocument prepareReportDocument() {
        ReportDocument reportDocument = new ReportDocument();
        reportDocument.setReportDocumentType(UmlDiagramType.class);

        List<LineProcessor> lineProcessors = new ArrayList<>();
        lineProcessors.add(
                new LineProcessor(
                        applicationContext.getBean(AnyLineSearchCommand.class),
                        applicationContext.getBean(ModulesActivityTransformer.class),
                        reportDocument));

        LineProcessorService.getInstance().getLineProcessors().addAll(lineProcessors);
        log.info("Line processors prepared");
        return reportDocument;
    }

    @Override
    public Class<? extends ReportDocumentType> getReportDocumentType() {
        return UmlDiagramType.class;
    }

}
