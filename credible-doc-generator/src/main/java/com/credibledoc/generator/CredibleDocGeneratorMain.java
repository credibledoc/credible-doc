package com.credibledoc.generator;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.doc.module.substitution.SubstitutionTactic;
import com.credibledoc.substitution.doc.module.substitution.activity.ActivityUmlReportService;
import com.credibledoc.substitution.doc.module.substitution.activity.modules.ModulesActivityUmlReportService;
import com.credibledoc.substitution.doc.module.substitution.launching.LaunchingUmlReportService;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.context.ReportingContext;
import com.credibledoc.substitution.reporting.replacement.ReplacementService;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreatorService;
import com.credibledoc.substitution.reporting.tracking.TrackingService;
import com.credibledoc.substitution.reporting.visualizer.VisualizerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The main class for generating documentation of the credibledoc-substitution tool.
 * <p>
 * This main method should be launched in the credible-doc\substitution (parent) working directory.
 *
 * @author Kyrylo Semenko
 */
@ComponentScan(basePackages = "com.credibledoc")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class CredibleDocGeneratorMain {
    public static final String APPLICATION_SUBSTITUTION_DOC_FINISHED = "Application finished.";
    public static final String MODULE_NAME = "credible-doc-generator";
    public static final String APPLICATION_SUBSTITUTION_DOC_LAUNCHED = "Application credible-doc-generator launched.";
    public static final String CREDIBLE_DOC_GENERATOR = "credible-doc-generator";
    private static final String WATCH = "-watch";

    @NonNull
    private final LaunchingUmlReportService launchingUmlReportService;

    @NonNull
    private final ActivityUmlReportService activityUmlReportService;

    @NonNull
    private final ModulesActivityUmlReportService modulesActivityUmlReportService;

    @NonNull
    private final SubstitutionTactic substitutionTactic;

    /**
     * The main method for generating the documentation for the 'credible-doc' repository.
     * <p>
     * The main method should be launched from the credibledoc/credible-doc folder. The method generates documents
     * to the credibledoc/credible-doc/target directory. These documents can be copied to the credibledoc/credible-doc
     * folder instead of old existing documents.
     *
     * @param args may contain {@link #WATCH} argument. In the case all directories in a templates source directory will be
     *             watched for changes to files.
     */
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext applicationContext
                 = new AnnotationConfigApplicationContext(CredibleDocGeneratorMain.class)) {
            log.info(APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
            boolean watchChanges = false;
            if (args != null) {
                List<String> arguments = Arrays.asList(args);
                if (arguments.contains(WATCH)) {
                    watchChanges = true;
                }
            }
            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            CredibleDocGeneratorMain credibleDocGeneratorMain =
                applicationContext.getBean(CredibleDocGeneratorMain.class);
            credibleDocGeneratorMain.substitute(watchChanges);
            log.info(APPLICATION_SUBSTITUTION_DOC_FINISHED);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private void substitute(boolean watchChanges) throws IOException, InterruptedException {
        ReportDocumentCreatorService reportDocumentCreatorService = ReportDocumentCreatorService.getInstance();
        SubstitutionContext substitutionContext = new SubstitutionContext().init().loadConfiguration();
        ReplacementService replacementService = ReplacementService.getInstance();
        List<TemplateResource> templateResources = replacementService.copyResourcesToTargetDirectory(substitutionContext);
        log.debug("Markdown templates will be loaded from the templateResources. " +
            "Templates number: {}", templateResources.size());
        List<ReportDocumentCreator> reportDocumentCreators = Arrays.asList(launchingUmlReportService,
            activityUmlReportService, modulesActivityUmlReportService);
        for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreators) {
            CombinerContext combinerContext = new CombinerContext().init();
            EnricherContext enricherContext = new EnricherContext().init();
            ReportingContext reportingContext = new ReportingContext().init();
            combinerContext.getTacticRepository().getTactics().add(substitutionTactic);
            reportDocumentCreatorService
                .addReportDocumentCreators(Collections.singletonList(reportDocumentCreator), reportingContext);
            reportDocumentCreatorService.createReportDocuments(combinerContext, reportingContext, substitutionContext, enricherContext, templateResources);
            List<Class<? extends ReportDocumentType>> reportDocumentTypes = Collections.singletonList(UmlDiagramType.class);
            VisualizerService.getInstance().createReports(reportDocumentTypes, combinerContext, reportingContext, enricherContext);
        }
        log.info("Templates placeholders will be substituted with the generated content. " +
            "Templates number: {}", templateResources.size());
        for (TemplateResource templateResource : templateResources) {
            replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
        }
        if (watchChanges) {
            TrackingService trackingService = new TrackingService(substitutionContext);
            trackingService.track();
        }
    }
}
