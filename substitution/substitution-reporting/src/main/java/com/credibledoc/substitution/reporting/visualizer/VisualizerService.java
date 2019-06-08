package com.credibledoc.substitution.reporting.visualizer;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.enricher.transformer.TransformerService;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.reporting.report.Report;
import com.credibledoc.substitution.reporting.report.ReportService;
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
 * see the {@link #createReports(Collection)} method.
 *
 * @author Kyrylo Semenko
 */
public class VisualizerService {

    private static final Logger logger = LoggerFactory.getLogger(VisualizerService.class);

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
     */
    public void createReports(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes) {
        logger.info("Method createReports started, reportDocumentTypes: '{}'", reportDocumentTypes);
        List<Report> reports = ReportService.getInstance().getReports();
        for (Report report : reports) {
            createReport(reportDocumentTypes, report);
        }
    }

    private void createReport(Collection<Class<? extends ReportDocumentType>> reportDocumentTypes, Report report) {
        logger.info("Method createReports started. Report: {}", report.hashCode());
        ReportDocumentService reportDocumentService = ReportDocumentService.getInstance();
        List<ReportDocument> reportDocuments = reportDocumentService.getReportDocuments(report);
        Set<NodeFile> nodeFiles = reportDocumentService.getNodeFiles(reportDocuments);
        Set<Tactic> tactics = TacticService.getInstance().getTactics();
        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(tactics);
        String line = null;

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(nodeFiles);

        LogBufferedReader currentReader = null;
        int currentLineNumber = 0;
        TransformerService transformerService = TransformerService.getInstance();
        try {
            line = readerService.readLineFromReaders(filesMergerState);
            String substring = line.substring(0, 35);
            logger.info("The first line read from {}. Line: '{}...'", ReaderService.class.getSimpleName(), substring);
            while (line != null) {
                currentReader = readerService.getCurrentReader(filesMergerState);
                List<String> multiline = readerService.readMultiline(line, currentReader);

                currentLineNumber = currentLineNumber + multiline.size();
                if (currentLineNumber % 100000 == 0) {
                    int perCent = (int) (currentLineNumber * 100f) / report.getLinesNumber();
                    logger.debug("{} lines processed ({}%)", currentLineNumber, perCent);
                }

                for (ReportDocument reportDocument : reportDocuments) {
                    if (reportDocumentTypes.contains(reportDocument.getReportDocumentType())) {
                        transformerService.transformToReport(reportDocument, multiline, currentReader);
                    }
                }

                reportDocumentService.appendReportDocumentsForAddition(report);

                line = readerService.readLineFromReaders(filesMergerState);
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

    private String getReportDirectoryPath(Report report) {
        if (report.getDirectory() != null) {
            return report.getDirectory().getAbsolutePath();
        }
        return null;
    }
}
