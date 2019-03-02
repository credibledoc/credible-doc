package org.credibledoc.substitution.doc;

import com.credibledoc.substitution.content.ContentGenerator;
import com.credibledoc.substitution.content.ContentGeneratorService;
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
        visualizerService.createReports(Collections.singletonList(ReportDocumentType.DOCUMENT_PART_UML));
        markdownService.generateMarkdownFromTemplates();

    }

    /**
     * <p>
     * If the {@link File} from parameter is a file, run visualization.
     *
     * <p>
     * Else collect files from the directory, see the
     * {@link FileService#collectApplicationLogs(File, List)}
     * method and run visualization.
     *
     * @param fileOrDir source file or directory
     * @param report the application state
     */
    private void prepareVisualizer(File fileOrDir, Report report) {
        reportService.setReport(report);
        report.setDirectory(new File(fileOrDir.getPath() + FileService.REPORT_FOLDER_EXTENSION));
        log.info("The Report object will be stored by " +
            "{} in the '{}' directory.", ReportService.class.getSimpleName(), report.getDirectory());
        ApplicationLog applicationLog = new ApplicationLog();
        if (fileOrDir.getName().endsWith(".zip")) {
            fileOrDir = fileService.unzipIfNotExists(fileOrDir, new File[0]);
        }
        TacticHolder tacticHolder = fileService.findOutApplicationType(fileOrDir);

        applicationLog.setTacticHolder(tacticHolder);
        Date date = fileService.findOutDate(fileOrDir, tacticHolder);
        NodeFile nodeFile = nodeFileService.createNodeFile(date, fileOrDir);
        NodeLog nodeLog = nodeLogService.createNodeLog(nodeFile.getFile());
        nodeLog.setApplicationLog(applicationLog);
        nodeFile.setNodeLog(nodeLog);
        nodeLogService.findNodeLogs(applicationLog).add(nodeLog);
        applicationLogService.addApplicationLog(applicationLog);
    }
}
