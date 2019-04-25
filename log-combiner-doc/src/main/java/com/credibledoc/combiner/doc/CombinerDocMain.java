package com.credibledoc.combiner.doc;

import com.credibledoc.combiner.application.identifier.ApplicationIdentifierService;
import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.content.ContentGeneratorService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.combiner.doc.markdown.MarkdownService;
import com.credibledoc.combiner.doc.module.combiner.SubstitutionApplicationIdentifier;
import com.credibledoc.combiner.doc.module.combiner.SubstitutionTactic;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentType;
import com.credibledoc.combiner.doc.reportdocument.creator.ReportDocumentCreator;
import com.credibledoc.combiner.doc.reportdocument.creator.ReportDocumentCreatorService;
import com.credibledoc.combiner.doc.visualizer.VisualizerService;
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
 * The main class for generation of documentation of the credibledoc-combiner tool.
 *
 * @author Kyrylo Semenko
 */
@ComponentScan(basePackages = "com.credibledoc.combiner.doc")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class CombinerDocMain {

    public static final String APPLICATION_SUBSTITUTION_DOC_LAUNCHED = "Application combiner-doc launched.";

    public static final String SUBSTITUTION_DOC = "combiner-doc";

    public static final String APPLICATION_SUBSTITUTION_DOC_FINISHED = "Application finished.";

    @NonNull
    private final MarkdownService markdownService;

    @NonNull
    private final VisualizerService visualizerService;

    @NonNull
    private final List<ContentGenerator> markdownGenerators;

    @NonNull
    private final List<ReportDocumentCreator> reportDocumentCreators;

    @NonNull
    private final ReportDocumentCreatorService reportDocumentCreatorService;

    @NonNull
    private final SubstitutionTactic substitutionSpecificTactic;

    @NonNull
    private final SubstitutionApplicationIdentifier substitutionApplicationIdentifier;

    /**
     * The main method for generation of documentation of the credibledoc-combiner tool.
     */
    public static void main(String[] args) {
        log.info(APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
        try (AnnotationConfigApplicationContext applicationContext
                     = new AnnotationConfigApplicationContext(CombinerDocMain.class)) {
            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            CombinerDocMain combinerDocMain = applicationContext.getBean(CombinerDocMain.class);
            combinerDocMain.substitute();
        }
        log.info(APPLICATION_SUBSTITUTION_DOC_FINISHED);
    }

    private void substitute() {
        TacticService.getInstance().getTactics().add(substitutionSpecificTactic);
        ApplicationIdentifierService.getInstance().getApplicationIdentifiers().add(substitutionApplicationIdentifier);
//        ApplicationService.getInstance().
        ContentGeneratorService.getInstance().addContentGenerators(markdownGenerators);
        reportDocumentCreatorService.addReportDocumentCreators(reportDocumentCreators);
        markdownService.reportDocumentCreatorService.createReportDocuments();
        copyResourcesToTargetDirectory();
        visualizerService.createReports(Collections.singletonList(ReportDocumentType.DOCUMENT_PART_UML));
        markdownService.generateContentFromTemplates();
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
