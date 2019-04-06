package org.credibledoc.substitution.doc.filesmerger.node.applicationlog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.filesmerger.node.file.NodeFile;
import org.credibledoc.substitution.doc.report.Report;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * This service contains methods for working with {@link ApplicationLog}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ApplicationLogService {

    @NonNull
    private final ApplicationLogRepository applicationLogRepository;

    @NonNull
    private final ReportDocumentService reportDocumentService;

    /**
     * Find all {@link ApplicationLog} filtered by {@link Report}
     * @return all {@link ApplicationLog}s which belongs to the {@link Report}
     */
    public List<ApplicationLog> getApplicationLogs(Report report) {
        List<ApplicationLog> result = new ArrayList<>();
        for (ApplicationLog applicationLog : applicationLogRepository.getApplicationLogs()) {
            for (ReportDocument reportDocument : reportDocumentService.getReportDocuments(report)) {
                NodeFile nodeFile = reportDocument.getNodeFiles().iterator().next();
                if (nodeFile.getNodeLog().getApplicationLog() == applicationLog) {
                    result.add(applicationLog);
                }
            }
        }
        return result;
    }

    /**
     * Add the {@link ApplicationLog} to the {@link ApplicationLogRepository}
     * @param applicationLog for addition
     */
    public void addApplicationLog(ApplicationLog applicationLog) {
        applicationLogRepository.getApplicationLogs().add(applicationLog);
    }

    /**
     * Call the {@link ApplicationLogRepository#getApplicationLogs()} method
     * @return All items from the {@link ApplicationLogRepository}
     */
    public List<ApplicationLog> getApplicationLogs() {
        return applicationLogRepository.getApplicationLogs();
    }
}
