package com.credibledoc.generator;

import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.substitution.doc.module.substitution.SubstitutionTactic;
import com.credibledoc.substitution.doc.module.substitution.report.UmlDiagramType;
import com.credibledoc.substitution.reporting.markdown.MarkdownService;
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

    public static final String APPLICATION_SUBSTITUTION_DOC_LAUNCHED = "Application substitution-doc launched.";

    public static final String APPLICATION_SUBSTITUTION_DOC_FINISHED = "Application finished.";
    public static final String MODULE_NAME = "credible-doc-generator";

    @NonNull
    private final List<ReportDocumentCreator> reportDocumentCreators;

    @NonNull
    private final SubstitutionTactic substitutionSpecificTactic;

    /**
     * The main method for generation of documentation of the credibledoc-substitution tool.
     * <p>
     * This method should be launched within its parent (../substitution) folder.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        log.info(APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
        try (AnnotationConfigApplicationContext applicationContext
                     = new AnnotationConfigApplicationContext(CredibleDocGeneratorMain.class)) {
            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            CredibleDocGeneratorMain substitutionDocMain = applicationContext.getBean(CredibleDocGeneratorMain.class);
            substitutionDocMain.substitute();
        }
        log.info(APPLICATION_SUBSTITUTION_DOC_FINISHED);
    }

    private void substitute() {
        TacticService.getInstance().getTactics().add(substitutionSpecificTactic);
        ReportDocumentCreatorService reportDocumentCreatorService = ReportDocumentCreatorService.getInstance();
        reportDocumentCreatorService.addReportDocumentCreators(reportDocumentCreators);
        reportDocumentCreatorService.createReportDocuments();
        copyResourcesToTargetDirectory();
        List<Class<? extends ReportDocumentType>> reportDocumentTypes = Collections.singletonList(UmlDiagramType.class);
        VisualizerService.getInstance().createReports(reportDocumentTypes);
        MarkdownService.getInstance().generateContentFromTemplates();
    }

    private void copyResourcesToTargetDirectory() {
        Configuration configuration = ConfigurationService.getInstance().getConfiguration();
        ResourceService resourceService = ResourceService.getInstance();
        List<String> allResources = resourceService.getResources(null, configuration.getTemplatesResource());
        TemplateService templateService = TemplateService.getInstance();
        for (String resource : allResources) {
            if (!resource.endsWith(MarkdownService.MARKDOWN_FILE_EXTENSION) && containsDotInName(resource)) {
                String targetFilePath = resourceService.generatePlaceholderResourceRelativePath(resource);
                String targetFileAbsolutePath = configuration.getTargetDirectory() + targetFilePath;
                log.info("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'",
                    resource, targetFileAbsolutePath);
                File file = templateService.exportResource(resource, targetFileAbsolutePath);
                log.info("Resource copied to file: '{}'", file.getAbsolutePath());
            }
        }
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
