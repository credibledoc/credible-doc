package com.credibledoc.substitution.reporting.reportdocument.creator;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
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
     * @param combinerContext the current state
     * @param reportingContext the current state
     * @param substitutionContext the current state
     * @param enricherContext the current state
     * @param templateResources contains templates with placeholders
     */
    public void createReportDocuments(CombinerContext combinerContext, ReportingContext reportingContext,
                                      SubstitutionContext substitutionContext, EnricherContext enricherContext,
                                      List<TemplateResource> templateResources) {
        TemplateResource lastTemplateResource = null;
        String lastTemplatePlaceholder = null;
        ReportDocumentCreatorRepository reportDocumentCreatorRepository
            = reportingContext.getReportDocumentCreatorRepository();
        try {
            PlaceholderService placeholderService = PlaceholderService.getInstance();
            for (TemplateResource templateResource : templateResources) {
                lastTemplateResource = templateResource;
                List<String> placeholders = placeholderService.parsePlaceholders(templateResource, substitutionContext);
                int position = 1;
                for (String templatePlaceholder : placeholders) {
                    lastTemplatePlaceholder = templatePlaceholder;
                    Placeholder placeholder =
                        placeholderService.parseJsonFromPlaceholder(templatePlaceholder, templateResource, substitutionContext);
                    placeholder.setId(Integer.toString(position++));
                    Class<?> placeholderClass = Class.forName(placeholder.getClassName());
                    ReportDocumentCreator reportDocumentCreator =
                        reportDocumentCreatorRepository.getMap().get(placeholderClass);
                    if (reportDocumentCreator != null && ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                        createReportDocumentForPlaceholder(placeholder,
                            reportDocumentCreator, combinerContext, substitutionContext, reportingContext, enricherContext);
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
     * Create {@link ReportDocument}, see the {@link ReportDocumentCreator#prepareReportDocument(EnricherContext)} method.
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
     * @param combinerContext               the current state
     * @param substitutionContext   the current state
     * @param reportingContext      the current state
     * @param enricherContext       the current state
     */
    private void createReportDocumentForPlaceholder(Placeholder placeholder,
                                                    ReportDocumentCreator reportDocumentCreator,
                                                    CombinerContext combinerContext,
                                                    SubstitutionContext substitutionContext,
                                                    ReportingContext reportingContext,
                                                    EnricherContext enricherContext) {
        ReportDocument reportDocument = reportDocumentCreator.prepareReportDocument(enricherContext);
        PlaceholderToReportDocumentService.getInstance().putPlaceholderToReportDocument(placeholder, reportDocument);
        substitutionContext.getPlaceholderRepository().getPlaceholders().add(placeholder);
        if (placeholder.getParameters() != null &&
            placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER) != null) {
            File file = new File(placeholder.getParameters().get(SOURCE_FILE_RELATIVE_PATH_PLACEHOLDER_PARAMETER));
            if (!file.exists()) {
                logger.info("File not exists. Report will not be created. File: '{}'", file.getAbsolutePath());
            } else {
                logger.trace("File will be parsed: {}", file.getAbsolutePath());
                prepareReport(file, reportDocument, combinerContext, reportingContext);
            }
        }
        reportingContext.getReportDocumentRepository().getReportDocuments().add(reportDocument);
    }

    /**
     * Create a new {@link Report}
     * @param logFile a source file
     * @param reportDocument belonging to the {@link Report}
     * @param combinerContext the current state
     * @param reportingContext the current state
     */
    private void prepareReport(File logFile, ReportDocument reportDocument, CombinerContext combinerContext, ReportingContext reportingContext) {
        Report report = new Report();
        reportingContext.getReportRepository().addReports(Collections.singletonList(report));
        reportDocument.setReport(report);
        FileService fileService = FileService.getInstance();
        Tactic tactic = fileService.findTactic(logFile, combinerContext);

        Date date = fileService.findDate(logFile, tactic);
        NodeLogService nodeLogService = NodeLogService.getInstance();
        NodeLog nodeLog = nodeLogService.createNodeLog(logFile, combinerContext, tactic);
        nodeLog.setTactic(tactic);
        NodeFile nodeFile = NodeFileService.getInstance().createNodeFile(date, logFile, combinerContext, nodeLog);
        reportDocument.getNodeFiles().add(nodeFile);
        nodeLogService.findNodeLogs(tactic, combinerContext).add(nodeLog);
        logger.info("Report prepared. Report: {}", report.hashCode());
    }
}
