package org.credibledoc.substitution.doc;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.content.ContentGeneratorService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.credibledoc.substitution.doc.file.FileService;
import org.credibledoc.substitution.doc.markdown.MarkdownService;
import org.credibledoc.substitution.doc.module.tactic.TacticHolder;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLog;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLogService;
import org.credibledoc.substitution.doc.node.file.NodeFile;
import org.credibledoc.substitution.doc.node.file.NodeFileService;
import org.credibledoc.substitution.doc.node.log.NodeLog;
import org.credibledoc.substitution.doc.node.log.NodeLogService;
import org.credibledoc.substitution.doc.report.Report;
import org.credibledoc.substitution.doc.report.ReportService;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentType;
import org.credibledoc.substitution.doc.visualizer.VisualizerService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

/**
 * The main class for generation of documentation of the credibledoc-substitution tool.
 *
 * @author Kyrylo Semenko
 */
@ComponentScan(basePackages = "org.credibledoc.substitution.doc")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class SubstitutionDocMain {

    public static final String APPLICATION_SUBSTITUTION_DOC_LAUNCHED = "Application substitution-doc launched.";

    public static final String SUBSTITUTION_DOC = "substitution-doc";
    private static final String RELATIVE_PATH_TO_SUBSTITUTION_DOC_LOG_FILE = "log\\substitution-doc.log";

    @NonNull
    private final MarkdownService markdownService;

    @NonNull
    private final ApplicationLogService applicationLogService;

    @NonNull
    private final VisualizerService visualizerService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private final FileService fileService;

    @NonNull
    private final NodeFileService nodeFileService;

    @NonNull
    private final NodeLogService nodeLogService;

    /**
     * The main method for generation of documentation of the credibledoc-substitution tool.
     */
    public static void main(String[] args) {
        log.info(APPLICATION_SUBSTITUTION_DOC_LAUNCHED);

        try (AnnotationConfigApplicationContext applicationContext
                     = new AnnotationConfigApplicationContext(SubstitutionDocMain.class)) {

            applicationContext.start();
            log.info("Spring ApplicationContext created and started");
            SubstitutionDocMain substitutionDocMain = applicationContext.getBean(SubstitutionDocMain.class);
            substitutionDocMain.substitute(applicationContext);

        }
        log.info("Application finished.");
    }

    private void substitute(ApplicationContext applicationContext) {
        Map<String, ContentGenerator> contentGeneratorMap = applicationContext.getBeansOfType(ContentGenerator.class);
        ContentGeneratorService.getInstance().addContentGenerators(new HashSet<>(contentGeneratorMap.values()));
        markdownService.createReportDocuments();
        Report report = new Report();
        File logFile = new File(RELATIVE_PATH_TO_SUBSTITUTION_DOC_LOG_FILE);
        prepareVisualizer(logFile, report);
        copyResourcesToTargetDirectory();
        visualizerService.createReports(Collections.singletonList(ReportDocumentType.DOCUMENT_PART_UML));
        markdownService.generateMarkdownFromTemplates();
    }

    /**
     * @param logFile a source file
     * @param report a state object
     */
    private void prepareVisualizer(File logFile, Report report) {
        reportService.setReport(report);
        report.setDirectory(new File(logFile.getPath() + FileService.REPORT_FOLDER_EXTENSION));
        log.info("The Report object will be stored by " +
            "{} in the '{}' directory.", ReportService.class.getSimpleName(), report.getDirectory());
        ApplicationLog applicationLog = new ApplicationLog();
        TacticHolder tacticHolder = fileService.findOutApplicationType(logFile);

        applicationLog.setTacticHolder(tacticHolder);
        Date date = fileService.findDate(logFile, tacticHolder);
        NodeFile nodeFile = nodeFileService.createNodeFile(date, logFile);
        NodeLog nodeLog = nodeLogService.createNodeLog(nodeFile.getFile());
        nodeLog.setApplicationLog(applicationLog);
        nodeFile.setNodeLog(nodeLog);
        nodeLogService.findNodeLogs(applicationLog).add(nodeLog);
        applicationLogService.addApplicationLog(applicationLog);

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
                log.info("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'", resource, targetFileAbsolutePath);
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
