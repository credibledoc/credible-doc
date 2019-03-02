package org.credibledoc.substitution.doc.visualizer;

import com.credibledoc.substitution.exception.SubstitutionRuntimeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.log.reader.ReaderService;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLogService;
import org.credibledoc.substitution.doc.report.Report;
import org.credibledoc.substitution.doc.report.ReportService;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentService;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentType;
import org.credibledoc.substitution.doc.transformer.TransformerService;
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
    public ReaderService readerService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private final ApplicationLogService applicationLogService;

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
        Report report = reportService.getReport();
        readerService.prepareBufferedReaders(applicationLogService.getApplicationLogs());
        String line = null;

        List<ReportDocument> reportDocuments = reportDocumentService.getReportDocuments();
        LogBufferedReader currentReader = null;
        int currentLineNumber = 0;
        try {
            line = readerService.readLineFromReaders();
            String substring = line.substring(0, 35);
            logger.info("The first line read from {}. Line: '{}...'", ReaderService.class.getSimpleName(), substring);
            while (line != null) {
                currentReader = readerService.getCurrentReader(report);
                List<String> multiline = readerService.readMultiline(line, currentReader);

                currentLineNumber = currentLineNumber + multiline.size();
                if (currentLineNumber % 100000 == 0) {
                    int perCent = (int)(currentLineNumber * 100f) / report.getLinesNumber();
                    logger.debug("{} lines processed ({}%)", currentLineNumber, perCent);
                }

                for (ReportDocument reportDocument : reportDocuments) {
                    if (reportDocumentTypes.contains(reportDocument.getReportDocumentType())) {
                        transformerService.transformToReport(reportDocument, multiline, currentReader);
                    }
                }

                reportDocumentService.appendReportDocumentsForAddition();

                line = readerService.readLineFromReaders();
            }
            logger.debug("{} lines processed (100%)", currentLineNumber);
        } catch (Exception e) {
            String fileName = "null";
            if (currentReader != null) {
                fileName = readerService.getFile(currentReader).getAbsolutePath();
            }
            String message =
                "Creation of reports failed. File: '" + fileName +
                        "', ReportDirectory: '" + report.getDirectory().getAbsolutePath() +
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
}
