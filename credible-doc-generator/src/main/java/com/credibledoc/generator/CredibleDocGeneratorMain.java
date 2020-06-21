package com.credibledoc.generator;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.substitution.doc.module.substitution.SubstitutionTactic;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.tracking.TrackingService;
import com.credibledoc.substitution.reporting.context.ReportingContext;
import com.credibledoc.substitution.reporting.replacement.ReplacementService;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreatorService;
import com.credibledoc.substitution.reporting.visualizer.VisualizerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The main class for generation of documentation of the credibledoc-substitution tool.
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
    private final List<ReportDocumentCreator> reportDocumentCreators;

    @NonNull
    private final SubstitutionTactic substitutionTactic;

    /**
     * The main method for generation of the documentation for the 'credible-doc' repository.
     * <p>
     * The main method should be launched from the credibledoc/credible-doc folder. The method generates documents
     * to the credibledoc/credible-doc/target directory. These documents can be copied to the credibledoc/credible-doc
     * folder instead of old existing documents.
     *
     * @param args may contain {@link #WATCH} argument. In the case all directories in a templates source directory will be
     *             watched for changes to files.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info(APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
        boolean watchChanges = false;
        if (args != null) {
            List<String> arguments = Arrays.asList(args);
            if (arguments.contains(WATCH)) {
                watchChanges = true;
            }
        }
        try (AnnotationConfigApplicationContext applicationContext
                     = new AnnotationConfigApplicationContext(CredibleDocGeneratorMain.class)) {
            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            CredibleDocGeneratorMain credibleDocGeneratorMain =
                applicationContext.getBean(CredibleDocGeneratorMain.class);
            credibleDocGeneratorMain.substitute(watchChanges);
        }
        log.info(APPLICATION_SUBSTITUTION_DOC_FINISHED);
    }

    private void substitute(boolean watchChanges) throws IOException, InterruptedException {
        Context context = new Context().init();
        EnricherContext enricherContext = new EnricherContext().init();
        SubstitutionContext substitutionContext = new SubstitutionContext().init().loadConfiguration();
        ReportingContext reportingContext = new ReportingContext().init();
        context.getTacticRepository().getTactics().add(substitutionTactic);
        ReportDocumentCreatorService reportDocumentCreatorService = ReportDocumentCreatorService.getInstance();
        reportDocumentCreatorService.addReportDocumentCreators(reportDocumentCreators, reportingContext);
        reportDocumentCreatorService.createReportDocuments(context, reportingContext, substitutionContext, enricherContext);
        List<TemplateResource> templateResources = copyResourcesToTargetDirectory(substitutionContext);
        List<Class<? extends ReportDocumentType>> reportDocumentTypes = Collections.singletonList(UmlDiagramType.class);
        VisualizerService.getInstance().createReports(reportDocumentTypes, context, reportingContext, enricherContext);
        ReplacementService replacementService = ReplacementService.getInstance();
        for (TemplateResource templateResource : templateResources) {
            replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
        }
        if (watchChanges) {
            TrackingService trackingService = new TrackingService(substitutionContext);
            trackingService.track();
        }
    }

    private List<TemplateResource> copyResourcesToTargetDirectory(SubstitutionContext substitutionContext) throws IOException {
        List<TemplateResource> result = new ArrayList<>();
        Configuration configuration = substitutionContext.getConfiguration();
        ResourceService resourceService = ResourceService.getInstance();
        List<TemplateResource> allResources =
            resourceService.getResources(null, configuration.getTemplatesResource());
        TemplateService templateService = TemplateService.getInstance();
        PlaceholderService placeholderService = PlaceholderService.getInstance();
        for (TemplateResource templateResource : allResources) {
            List<String> placeholders = placeholderService.parsePlaceholders(templateResource, substitutionContext);
            if (!placeholders.isEmpty()) {
                result.add(templateResource);
            }
            if (templateResource.getType() == ResourceType.FILE) {
                String targetFileRelativePath =
                    resourceService.generatePlaceholderResourceRelativePath(templateResource, substitutionContext);
                String targetFileAbsolutePath = configuration.getTargetDirectory() + targetFileRelativePath;
                log.info("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'",
                    templateResource, targetFileAbsolutePath);
                Path targetPath = Paths.get(targetFileAbsolutePath);
                Files.createDirectories(targetPath.getParent());
                Files.copy(templateResource.getFile().toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else if (templateResource.getType() == ResourceType.CLASSPATH) {
                if (containsDotInName(templateResource.getPath())) {
                    String targetFileRelativePath =
                        resourceService.generatePlaceholderResourceRelativePath(templateResource, substitutionContext);
                    String targetFileAbsolutePath = configuration.getTargetDirectory() + targetFileRelativePath;
                    log.info("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'",
                        templateResource, targetFileAbsolutePath);
                    File file = templateService.exportResource(templateResource.getPath(), targetFileAbsolutePath);
                    log.info("Resource copied to file: '{}'", file.getAbsolutePath());
                }
            } else {
                throw new IllegalArgumentException("Unknown ResourceType " + templateResource.getType());
            }
        }
        return result;
    }

    /**
     * @param resource for example '/template/markdown/' is a directory, and '/template/markdown/README.md' is a file.
     * @return 'False' if this resource is directory
     */
    private boolean containsDotInName(String resource) {
        int index = resource.lastIndexOf('/');
        if (index == -1) {
            index = 0;
        }
        String fileName = resource.substring(index);
        return fileName.contains(".");
    }
}
