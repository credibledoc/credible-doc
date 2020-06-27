package com.credibledoc.substitution.reporting.visualizer;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileTreeSet;
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

/**
 * Visualizer creates reports. The reports describes scenarios recorded in log files,
 * see the {@link #createReports(Collection, CombinerContext, ReportingContext, EnricherContext)} method.
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
     * @param combinerContext the current state
     * @param reportingContext the current state
     * @param enricherContext the current state
     */
    public void createReports(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes, CombinerContext combinerContext,
                              ReportingContext reportingContext, EnricherContext enricherContext) {
        logger.info("Method createReports started, reportDocumentTypes: '{}'", reportDocumentTypes);
        List<Report> reports = reportingContext.getReportRepository().getReports();
        for (Report report : reports) {
            createReport(reportDocumentTypes, report, combinerContext, reportingContext, enricherContext);
        }
    }

    private void createReport(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes,
                              Report report, CombinerContext combinerContext, ReportingContext reportingContext,
                              EnricherContext enricherContext) {
        logger.info("Method createReports started. Report: {}", report);
        ReportDocumentService reportDocumentService = ReportDocumentService.getInstance();
        List<ReportDocument> reportDocuments = reportDocumentService.getReportDocuments(report, reportingContext);
        NodeFileTreeSet<NodeFile> nodeFiles = (NodeFileTreeSet<NodeFile>) reportDocumentService.getNodeFiles(reportDocuments);
        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(combinerContext, nodeFiles);
        String line = null;

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(nodeFiles);

        LogBufferedReader currentReader = null;
        int currentLineNumber = 0;
        TransformerService transformerService = TransformerService.getInstance();
        try {
            line = readerService.readLineFromReaders(filesMergerState, combinerContext);
            int endIndex = Math.max(line.length(), 35);
            String substring = line.substring(0, endIndex);
            logger.trace("The first line is read from {}. Line: '{}...'", getClass().getSimpleName(), substring);
            while (line != null) {
                currentReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
                List<String> multiLine = readerService.readMultiline(line, currentReader, combinerContext);

                currentLineNumber = transformMultiLine(multiLine, reportDocumentTypes, report, reportDocuments,
                    currentReader, currentLineNumber, transformerService, combinerContext, enricherContext);

                reportDocumentService.mergeReportDocumentsForAddition(reportingContext);
                reportDocuments = reportDocumentService.getReportDocuments(report, reportingContext);

                line = readerService.readLineFromReaders(filesMergerState, combinerContext);
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
                                   CombinerContext combinerContext,
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
                        combinerContext, enricherContext);
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
