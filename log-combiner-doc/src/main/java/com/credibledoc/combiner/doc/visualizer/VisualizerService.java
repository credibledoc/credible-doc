package com.credibledoc.combiner.doc.visualizer;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.doc.report.Report;
import com.credibledoc.combiner.doc.report.ReportService;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentService;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentType;
import com.credibledoc.combiner.doc.transformer.TransformerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Visualizer creates reports. The reports describes scenarios recorded in log files,
 * see the {@link #createReports(List)} method.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VisualizerService {

    private static final Logger logger = LoggerFactory.getLogger(VisualizerService.class);
    
    @NonNull
    private TransformerService transformerService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private ReportDocumentService reportDocumentService;

    /**
     * Read files(s), parse them and create reports.
     *
     * @param reportDocumentTypes defines which {@link ReportDocumentType}s
     *                            can be transformed in a particular invocation
     */
    public void createReports(List<ReportDocumentType> reportDocumentTypes) {
        logger.info("Method createReports started, reportDocumentTypes: '{}'", reportDocumentTypes);
        List<Report> reports = reportService.getReports();
        for (Report report : reports) {
            createReport(reportDocumentTypes, report);
        }
    }

    private void createReport(List<ReportDocumentType> reportDocumentTypes, Report report) {
        logger.info("Method createReports started. Report: {}", report.hashCode());
        List<ReportDocument> reportDocuments = reportDocumentService.getReportDocuments(report);
        List<NodeFile> nodeFiles = reportDocumentService.getNodeFiles(reportDocuments);
        List<ApplicationLog> applicationLogs = ApplicationLogService.getInstance().getApplicationLogs(nodeFiles);
        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(applicationLogs);
        String line = null;

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(nodeFiles);

        LogBufferedReader currentReader = null;
        int currentLineNumber = 0;
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
            throw new CombinerRuntimeException(message, e);
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
