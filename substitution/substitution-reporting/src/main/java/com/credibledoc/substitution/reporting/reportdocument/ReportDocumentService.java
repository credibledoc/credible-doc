package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.substitution.reporting.report.Report;

import java.util.*;

/**
 * The stateless service for working with {@link ReportDocument}s.
 *
 * @author Kyrylo Semenko
 */
public class ReportDocumentService {

    /**
     * Singleton.
     */
    private static ReportDocumentService instance;

    /**
     * @return The {@link ReportDocumentService} singleton.
     */
    public static ReportDocumentService getInstance() {
        if (instance == null) {
            instance = new ReportDocumentService();
        }
        return instance;
    }

    /**
     * Call the {@link ReportDocumentRepository#getReportDocuments()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    public List<ReportDocument> getReportDocuments() {
        return ReportDocumentRepository.getInstance().getReportDocuments();
    }

    /**
     * Call the {@link ReportDocumentRepository#getReportDocumentsForAddition()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    private List<ReportDocument> getReportDocumentsForAddition() {
        return ReportDocumentRepository.getInstance().getReportDocumentsForAddition();
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
        List<ReportDocument> result = new ArrayList<>();
        for (ReportDocument reportDocument : getReportDocuments()) {
            if (report == reportDocument.getReport()) {
                result.add(reportDocument);
            }
        }
        return result;
    }

    /**
     * Collect {@link NodeFile}s which belong to the {@link ReportDocument}
     * @param reportDocuments that contains {@link NodeFile}s
     * @return List of unordered unique {@link NodeFile}s
     */
    public Set<NodeFile> getNodeFiles(List<ReportDocument> reportDocuments) {
        Set<NodeFile> nodeFiles = new HashSet<>();
        for (ReportDocument reportDocument : reportDocuments) {
            Set<NodeFile> nodeFilesSet = reportDocument.getNodeFiles();
            nodeFiles.addAll(nodeFilesSet);
        }
        return nodeFiles;
    }
}
