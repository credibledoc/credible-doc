package com.credibledoc.combiner.doc.reportdocument.creator;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.combiner.doc.markdown.MarkdownService;
import com.credibledoc.combiner.doc.placeholder.reportdocument.PlaceholderToReportDocumentRepository;
import com.credibledoc.combiner.doc.placeholder.reportdocument.PlaceholderToReportDocumentService;
import com.credibledoc.combiner.doc.report.Report;
import com.credibledoc.combiner.doc.report.ReportService;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

/**
 * A stateless service for working with {@link ReportDocumentCreator}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ReportDocumentCreatorService {
    private static final String SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER = "sourceFileRelativePath";

    @NonNull
    private ReportDocumentCreatorRepository reportDocumentCreatorRepository;

    @NonNull
    private final ReportDocumentService reportDocumentService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private final PlaceholderToReportDocumentService placeholderToReportDocumentService;

    /**
     * Add all items to the {@link ReportDocumentCreatorRepository#getMap()} entries.
     *
     * @param reportDocumentCreators items for addition
     */
    public void addReportDocumentCreators(Collection<ReportDocumentCreator> reportDocumentCreators) {
        Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map = new HashMap<>();
        for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreators) {
            map.put(reportDocumentCreator.getClass(), reportDocumentCreator);
        }
        reportDocumentCreatorRepository.getMap().putAll(map);
    }

    /**
     * Iterate {@link Placeholder}s from template resources and for each {@link Placeholder} find the appropriate
     * {@link ReportDocumentCreator} from the {@link ReportDocumentCreatorRepository}.
     * Then create a {@link ReportDocument} for the {@link Placeholder}.
     */
    public void createReportDocuments() {
        String lastTemplateResource = null;
        String lastTemplatePlaceholder = null;
        try {
            String templatesResource = ConfigurationService.getInstance().getConfiguration().getTemplatesResource();
            List<String> resources =
                    ResourceService.getInstance().getResources(MarkdownService.MARKDOWN_FILE_EXTENSION, templatesResource);
            log.info("Markdown templates will be loaded from the resources: {}", resources);
            PlaceholderService placeholderService = PlaceholderService.getInstance();
            for (String templateResource : resources) {
                lastTemplateResource = templateResource;
                String templateContent = TemplateService.getInstance().getTemplateContent(templateResource);
                List<String> placeholders = placeholderService.parsePlaceholders(templateContent, templateResource);
                int position = 1;
                for (String templatePlaceholder : placeholders) {
                    lastTemplatePlaceholder = templatePlaceholder;
                    Placeholder placeholder =
                        placeholderService.parseJsonFromPlaceholder(templatePlaceholder, templateResource);
                    placeholder.setId(Integer.toString(position++));
                    Class<?> placeholderClass = Class.forName(placeholder.getClassName());
                    if (ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                        ReportDocumentCreator reportDocumentCreator =
                            reportDocumentCreatorRepository.getMap().get(placeholderClass);
                        createReportDocumentForPlaceholder(placeholder, reportDocumentCreator);
                    }
                }
            }
            log.info("Report documents created");
        } catch (ClassNotFoundException e) {
            throw new CombinerRuntimeException("Class defined in the placeholder cannot be found, " +
                    "templateResource: '" +
                    lastTemplateResource +
                    "', " +
                    "templatePlaceholder: '" +
                    lastTemplatePlaceholder +
                    "'.", e);
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Prepare relations between {@link Placeholder}s and {@link ReportDocument}s.
     * <p>
     * Create {@link ReportDocument}, see the {@link ReportDocumentCreator#prepareReportDocument()} method.
     * <p>
     * Put the {@link Placeholder} and {@link ReportDocument} to the
     * {@link PlaceholderToReportDocumentRepository}.
     * <p>
     * Add the {@link ReportDocument} to the {@link ReportDocumentService#getReportDocuments()} list.
     * <p>
     * Add the {@link Placeholder} to the {@link PlaceholderService#getPlaceholders()} list.
     *
     * @param placeholder           for addition
     * @param reportDocumentCreator for addition
     */
    private void createReportDocumentForPlaceholder(Placeholder placeholder,
                                                    ReportDocumentCreator reportDocumentCreator) {
        ReportDocument reportDocument = reportDocumentCreator.prepareReportDocument();
        placeholderToReportDocumentService.putPlaceholderToReportDocument(placeholder, reportDocument);
        reportDocumentService.getReportDocuments().add(reportDocument);
        PlaceholderService.getInstance().getPlaceholders().add(placeholder);
        if (placeholder.getParameters() != null &&
            placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER) != null) {
            File file = new File(placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER));
            if (!file.exists()) {
                log.info("File not exists. Report will not be created. File: '{}'", file.getAbsolutePath());
            } else {
                log.info("File will be parsed: {}", file.getAbsolutePath());
                prepareReport(file, reportDocument);
            }
        }
    }

    /**
     * Create a new {@link Report}
     * @param logFile a source file
     * @param reportDocument which belongs to the {@link Report}
     */
    private void prepareReport(File logFile, ReportDocument reportDocument) {
        Report report = new Report();
        reportService.addReports(Collections.singletonList(report));
        ApplicationLog applicationLog = new ApplicationLog();
        reportDocument.setReport(report);
        FileService fileService = FileService.getInstance();
        Application application = fileService.findApplication(logFile);

        applicationLog.setApplication(application);
        Date date = fileService.findDate(logFile, application);
        NodeFile nodeFile = NodeFileService.getInstance().createNodeFile(date, logFile);
        NodeLogService nodeLogService = NodeLogService.getInstance();
        NodeLog nodeLog = nodeLogService.createNodeLog(nodeFile.getFile());
        nodeLog.setApplicationLog(applicationLog);
        nodeFile.setNodeLog(nodeLog);
        reportDocument.getNodeFiles().add(nodeFile);
        nodeLogService.findNodeLogs(applicationLog).add(nodeLog);
        ApplicationLogService.getInstance().addApplicationLog(applicationLog);
        log.info("Report prepared. Report: {}", report.hashCode());
    }
}
