package com.credibledoc.substitution.reporting.reportdocument.creator;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.reporting.context.ReportingContext;
import com.credibledoc.substitution.reporting.placeholder.PlaceholderToReportDocumentRepository;
import com.credibledoc.substitution.reporting.placeholder.PlaceholderToReportDocumentService;
import com.credibledoc.substitution.reporting.report.Report;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless service for working with {@link ReportDocumentCreator}s.
 *
 * @author Kyrylo Semenko
 */
public class ReportDocumentCreatorService {
    private static final Logger logger = LoggerFactory.getLogger(ReportDocumentCreatorService.class);
    private static final String SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER = "sourceFileRelativePath";
    private static final String MARKDOWN_FILE_EXTENSION = ".md";

    /**
     * Singleton.
     */
    private static ReportDocumentCreatorService instance;

    /**
     * @return The {@link ReportDocumentCreatorService} singleton.
     */
    public static ReportDocumentCreatorService getInstance() {
        if (instance == null) {
            instance = new ReportDocumentCreatorService();
        }
        return instance;
    }

    /**
     * Add all items to the {@link ReportDocumentCreatorRepository#getMap()} entries.
     *
     * @param reportDocumentCreators items for addition
     * @param reportingContext contains the {@link ReportDocumentCreatorRepository} data store
     */
    public void addReportDocumentCreators(Collection<ReportDocumentCreator> reportDocumentCreators, ReportingContext reportingContext) {
        Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map = new HashMap<>();
        for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreators) {
            map.put(reportDocumentCreator.getClass(), reportDocumentCreator);
        }
        reportingContext.getReportDocumentCreatorRepository().getMap().putAll(map);
    }

    /**
     * Iterate {@link Placeholder}s from template resources and for each {@link Placeholder} find the appropriate
     * {@link ReportDocumentCreator} from the {@link ReportDocumentCreatorRepository}.
     * Then create a {@link ReportDocument} for the {@link Placeholder}.
     * 
     * @param context the current state
     * @param reportingContext the current state
     * @param substitutionContext the current state
     */
    public void createReportDocuments(Context context, ReportingContext reportingContext, SubstitutionContext substitutionContext) {
        TemplateResource lastTemplateResource = null;
        String lastTemplatePlaceholder = null;
        ReportDocumentCreatorRepository reportDocumentCreatorRepository
            = reportingContext.getReportDocumentCreatorRepository();
        try {
            String templatesResource = substitutionContext.getConfiguration().getTemplatesResource();
            List<TemplateResource> resources =
                    ResourceService.getInstance().getResources(MARKDOWN_FILE_EXTENSION, templatesResource);
            logger.debug("Markdown templates will be loaded from the resources: {}", resources);
            PlaceholderService placeholderService = PlaceholderService.getInstance();
            for (TemplateResource templateResource : resources) {
                lastTemplateResource = templateResource;
                List<String> placeholders = placeholderService.parsePlaceholders(templateResource, substitutionContext);
                int position = 1;
                for (String templatePlaceholder : placeholders) {
                    lastTemplatePlaceholder = templatePlaceholder;
                    Placeholder placeholder =
                        placeholderService.parseJsonFromPlaceholder(templatePlaceholder, templateResource, substitutionContext);
                    placeholder.setId(Integer.toString(position++));
                    Class<?> placeholderClass = Class.forName(placeholder.getClassName());
                    if (ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                        ReportDocumentCreator reportDocumentCreator =
                            reportDocumentCreatorRepository.getMap().get(placeholderClass);
                        createReportDocumentForPlaceholder(placeholder,
                            reportDocumentCreator, context, substitutionContext, reportingContext);
                    }
                }
            }
            logger.info("Report documents created");
        } catch (ClassNotFoundException e) {
            throw new SubstitutionRuntimeException("Class defined in the placeholder cannot be found, " +
                    "templateResource: '" +
                    lastTemplateResource +
                    "', " +
                    "templatePlaceholder: '" +
                    lastTemplatePlaceholder +
                    "'.", e);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
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
     * Add the {@link ReportDocument} to the
     * {@link com.credibledoc.substitution.reporting.reportdocument.ReportDocumentRepository#getReportDocuments()} list.
     * <p>
     * Add the {@link Placeholder} to the {@link com.credibledoc.substitution.core.placeholder.PlaceholderRepository#getPlaceholders()} list.
     *
     * @param placeholder           for addition
     * @param reportDocumentCreator for addition
     * @param context               the current state
     * @param substitutionContext   the current state
     */
    private void createReportDocumentForPlaceholder(Placeholder placeholder,
                                                    ReportDocumentCreator reportDocumentCreator,
                                                    Context context,
                                                    SubstitutionContext substitutionContext,
                                                    ReportingContext reportingContext) {
        ReportDocument reportDocument = reportDocumentCreator.prepareReportDocument();
        PlaceholderToReportDocumentService.getInstance().putPlaceholderToReportDocument(placeholder, reportDocument);
        substitutionContext.getPlaceholderRepository().getPlaceholders().add(placeholder);
        if (placeholder.getParameters() != null &&
            placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER) != null) {
            File file = new File(placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER));
            if (!file.exists()) {
                logger.info("File not exists. Report will not be created. File: '{}'", file.getAbsolutePath());
            } else {
                logger.info("File will be parsed: {}", file.getAbsolutePath());
                prepareReport(file, reportDocument, context, reportingContext);
            }
        }
        reportingContext.getReportDocumentRepository().getReportDocuments().add(reportDocument);
    }

    /**
     * Create a new {@link Report}
     * @param logFile a source file
     * @param reportDocument belonging to the {@link Report}
     */
    private void prepareReport(File logFile, ReportDocument reportDocument, Context context, ReportingContext reportingContext) {
        Report report = new Report();
        reportingContext.getReportRepository().addReports(Collections.singletonList(report));
        reportDocument.setReport(report);
        FileService fileService = FileService.getInstance();
        Tactic tactic = fileService.findTactic(logFile, context);

        Date date = fileService.findDate(logFile, tactic);
        NodeLogService nodeLogService = NodeLogService.getInstance();
        NodeLog nodeLog = nodeLogService.createNodeLog(logFile, context, tactic);
        nodeLog.setTactic(tactic);
        NodeFile nodeFile = NodeFileService.getInstance().createNodeFile(date, logFile, context, nodeLog);
        reportDocument.getNodeFiles().add(nodeFile);
        nodeLogService.findNodeLogs(tactic, context).add(nodeLog);
        logger.info("Report prepared. Report: {}", report.hashCode());
    }
}
