package org.credibledoc.substitution.doc.module.substitution.report;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.credibledoc.substitution.doc.module.substitution.launching.*;
import org.credibledoc.substitution.doc.report.ReportDocumentCreator;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentType;
import org.credibledoc.substitution.doc.transformer.LineProcessor;
import org.credibledoc.substitution.doc.transformer.LineProcessorService;
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
                        applicationContext.getBean(LaunchingSearchCommand.class),
                        applicationContext.getBean(LaunchingTransformer.class),
                        reportDocument));
        lineProcessors.add(
                new LineProcessor(
                        applicationContext.getBean(ConfigurationLoadingSearchCommand.class),
                        applicationContext.getBean(ConfigurationLoadingTransformer.class),
                        reportDocument));
        lineProcessors.add(
                new LineProcessor(
                        applicationContext.getBean(ContentReplacedSearchCommand.class),
                        applicationContext.getBean(ContentReplacedTransformer.class),
                        reportDocument));

        lineProcessorService.getLineProcessors().addAll(lineProcessors);

        return reportDocument;
    }

    @Override
    public ReportDocumentType getReportDocumentType() {
        return ReportDocumentType.DOCUMENT_PART_UML;
    }

}
