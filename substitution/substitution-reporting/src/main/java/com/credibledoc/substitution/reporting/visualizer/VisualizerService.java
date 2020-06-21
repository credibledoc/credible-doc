package com.credibledoc.substitution.reporting.visualizer;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.enricher.transformer.TransformerService;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.reporting.context.ReportingContext;
import com.credibledoc.substitution.reporting.report.Report;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentService;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Visualizer creates reports. The reports describes scenarios recorded in log files,
 * see the {@link #createReports(Collection, Context, ReportingContext, EnricherContext)} method.
 *
 * @author Kyrylo Semenko
 */
public class VisualizerService {

    private static final Logger logger = LoggerFactory.getLogger(VisualizerService.class);
    
    /**
     * Should an exception be thrown in case when the exception occurred?
     * Default is true. In case when System property -DcredibledocIgnoreFailures=true set, exception will not thrown.
     */
    private static final String IGNORE_FAILURES = "credibledocIgnoreFailures";

    /**
     * Singleton.
     */
    private static VisualizerService instance;

    /**
     * @return The {@link VisualizerService} singleton.
     */
    public static VisualizerService getInstance() {
        if (instance == null) {
            instance = new VisualizerService();
        }
        return instance;
    }

    /**
     * Read files(s), parse them and create reports.
     *
     * @param reportDocumentTypes defines which {@link ReportDocumentType}s
     *                            can be transformed in a particular invocation
     * @param context the current state
     * @param reportingContext the current state
     * @param enricherContext the current state
     */
    public void createReports(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes, Context context,
                              ReportingContext reportingContext, EnricherContext enricherContext) {
        logger.info("Method createReports started, reportDocumentTypes: '{}'", reportDocumentTypes);
        List<Report> reports = reportingContext.getReportRepository().getReports();
        for (Report report : reports) {
            createReport(reportDocumentTypes, report, context, reportingContext, enricherContext);
        }
    }

    private void createReport(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes,
                              Report report, Context context, ReportingContext reportingContext,
                              EnricherContext enricherContext) {
        logger.info("Method createReports started. Report: {}", report);
        ReportDocumentService reportDocumentService = ReportDocumentService.getInstance();
        List<ReportDocument> reportDocuments = reportDocumentService.getReportDocuments(report, reportingContext);
        Set<NodeFile> nodeFiles = reportDocumentService.getNodeFiles(reportDocuments);
        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(context);
        String line = null;

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(nodeFiles);

        LogBufferedReader currentReader = null;
        int currentLineNumber = 0;
        TransformerService transformerService = TransformerService.getInstance();
        try {
            line = readerService.readLineFromReaders(filesMergerState, context);
            String substring = line.substring(0, 35);
            logger.info("The first line read from {}. Line: '{}...'", ReaderService.class.getSimpleName(), substring);
            while (line != null) {
                currentReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
                List<String> multiLine = readerService.readMultiline(line, currentReader, context);

                currentLineNumber = transformMultiLine(multiLine, reportDocumentTypes, report, reportDocuments,
                    currentReader, currentLineNumber, transformerService, context, enricherContext);

                reportDocumentService.mergeReportDocumentsForAddition(reportingContext);
                reportDocuments = reportDocumentService.getReportDocuments(report, reportingContext);

                line = readerService.readLineFromReaders(filesMergerState, context);
            }
            logger.debug("{} lines processed (100%)", currentLineNumber);
        } catch (Exception e) {
            String fileName = "null";
            if (currentReader != null) {
                fileName = readerService.getFile(currentReader).getAbsolutePath();
            }
            String message =
                "Creation of reports failed. File: '" + fileName +
                    "', ReportDirectory: '" + getReportDirectoryPath(report) +
                    "', line: '" + line + "'";
            throw new SubstitutionRuntimeException(message, e);
        } finally {
            for (ReportDocument reportDocument : reportDocuments) {
                if (reportDocument.getFooterMethod() != null) {
                    reportDocument.getFooterMethod().accept(reportDocument);
                }
            }
        }
    }

    private int transformMultiLine(List<String> multiLine,
                                   Collection<Class<? extends ReportDocumentType>> reportDocumentTypes,
                                   Report report, List<ReportDocument> reportDocuments,
                                   LogBufferedReader currentReader,
                                   int currentLineNumber,
                                   TransformerService transformerService,
                                   Context context,
                                   EnricherContext enricherContext) {
        currentLineNumber = currentLineNumber + multiLine.size();
        try {
            if (report.getLinesNumber() > 0 && currentLineNumber % 100000 == 0) {
                int perCent = (int) (currentLineNumber * 100f) / report.getLinesNumber();
                logger.debug("{} lines processed ({}%)", currentLineNumber, perCent);
            }

            for (ReportDocument reportDocument : reportDocuments) {
                if (reportDocumentTypes.contains(reportDocument.getReportDocumentType())) {
                    transformerService.transformToReport(reportDocument, multiLine, currentReader,
                        context, enricherContext);
                }
            }
        } catch (Exception e) {
            String message =
                "Creation of reports failed." +
                    " ReportDirectory: '" + getReportDirectoryPath(report) +
                    "', line: '" + multiLine.get(0) + "'";
            if ("true".equals(System.getProperty(IGNORE_FAILURES))) {
                logger.error(message);
            } else {
                throw new SubstitutionRuntimeException(message, e);
            }
        }
        return currentLineNumber;
    }

    private String getReportDirectoryPath(Report report) {
        if (report.getDirectory() != null) {
            return report.getDirectory().getAbsolutePath();
        }
        return null;
    }
}
