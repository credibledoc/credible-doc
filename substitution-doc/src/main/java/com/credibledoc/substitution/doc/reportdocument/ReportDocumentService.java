package com.credibledoc.substitution.doc.reportdocument;

import com.credibledoc.substitution.doc.filesmerger.node.file.NodeFile;
import com.credibledoc.substitution.doc.report.Report;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The stateless service for working with {@link ReportDocument}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportDocumentService {
    @NonNull
    private ReportDocumentRepository reportDocumentRepository;

    /**
     * Call the {@link ReportDocumentRepository#getReportDocuments()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    public List<ReportDocument> getReportDocuments() {
        return reportDocumentRepository.getReportDocuments();
    }

    /**
     * Call the {@link ReportDocumentRepository#getReportDocumentsForAddition()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    private List<ReportDocument> getReportDocumentsForAddition() {
        return reportDocumentRepository.getReportDocumentsForAddition();
    }

    /**
     * Wee need to avoid {@link ConcurrentModificationException}, so we
     * can`t modify {@link ReportDocumentService#getReportDocuments()} directly.
     *
     * @param report which {@link ReportDocument}s belong to
     */
    public void appendReportDocumentsForAddition(Report report) {
        getReportDocuments(report).addAll(getReportDocumentsForAddition());
        getReportDocumentsForAddition().clear();
    }

    public List<ReportDocument> getReportDocuments(Report report) {
        return getReportDocuments().stream()
            .filter(reportDocument -> reportDocument.getReport() == report)
            .collect(Collectors.toList());
    }

    /**
     * Collect {@link NodeFile}s which belong to the {@link ReportDocument}
     * @param reportDocuments that contains {@link NodeFile}s
     * @return List of unordered unique {@link NodeFile}s
     */
    public List<NodeFile> getNodeFiles(List<ReportDocument> reportDocuments) {
        List<NodeFile> nodeFiles = new ArrayList<>();
        for (ReportDocument reportDocument : reportDocuments) {
            Set<NodeFile> nodeFilesSet = reportDocument.getNodeFiles();
            for (NodeFile nodeFile : nodeFilesSet) {
                if (!nodeFiles.contains(nodeFile)) {
                    nodeFiles.add(nodeFile);
                }
            }
        }
        return nodeFiles;
    }
}
