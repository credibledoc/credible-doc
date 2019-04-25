package com.credibledoc.combiner.doc.module.combiner.activity.modules;

import com.credibledoc.combiner.doc.module.combiner.activity.AnyLineSearchCommand;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentType;
import com.credibledoc.combiner.doc.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.combiner.doc.transformer.LineProcessor;
import com.credibledoc.combiner.doc.transformer.LineProcessorService;
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
                        applicationContext.getBean(ModulesActivityTransformer.class),
                        reportDocument));

        lineProcessorService.getLineProcessors().addAll(lineProcessors);
        log.info("Line processors prepared");
        return reportDocument;
    }

    @Override
    public ReportDocumentType getReportDocumentType() {
        return ReportDocumentType.DOCUMENT_PART_UML;
    }

}
