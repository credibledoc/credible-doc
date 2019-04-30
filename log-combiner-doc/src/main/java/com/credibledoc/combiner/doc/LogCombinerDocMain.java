package com.credibledoc.combiner.doc;

import com.credibledoc.substitution.reporting.markdown.MarkdownService;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.visualizer.VisualizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import java.util.Collections;

/**
 * The main class for generation of documentation for the log-combiner library and tool.
 *
 * @author Kyrylo Semenko
 */
@ComponentScan(basePackages = "com.credibledoc.combiner.doc")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class LogCombinerDocMain {

    /**
     * The main method for generation of documentation of the credibledoc-substitution tool.
     */
    public static void main(String[] args) {
        log.info("Application '{}' launched.", LogCombinerDocMain.class.getSimpleName());
        try (AnnotationConfigApplicationContext applicationContext
                 = new AnnotationConfigApplicationContext(LogCombinerDocMain.class)) {
            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            LogCombinerDocMain logCombinerDocMain = applicationContext.getBean(LogCombinerDocMain.class);
            logCombinerDocMain.substitute();
        }
        log.info("Application finished");
    }

    private void substitute() {
//        TacticService.getInstance().getTactics().add(substitutionSpecificTactic);
//        ApplicationIdentifierService.getInstance().getApplicationIdentifiers().add(substitutionApplicationIdentifier);
//        ReportDocumentCreatorService reportDocumentCreatorService = ReportDocumentCreatorService.getInstance();
//        reportDocumentCreatorService.addReportDocumentCreators(reportDocumentCreators);
//        reportDocumentCreatorService.createReportDocuments();
//        copyResourcesToTargetDirectory();
        VisualizerService.getInstance().createReports(Collections.singletonList(ReportDocumentType.DOCUMENT_PART_UML));
        MarkdownService.getInstance().generateContentFromTemplates();
    }

}
