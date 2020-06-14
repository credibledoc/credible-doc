package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.reporting.report.Report;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocuments()} list.
     *
     * @param reportDocument will be appended if it contains the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     */
    public void addReportDocument(ReportDocument reportDocument) {
        ReportDocumentRepository.getInstance().getReportDocuments().add(reportDocument);
    }

    /**
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocuments()} list.
     *
     * @param reportDocuments will be appended if all items contain the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     */
    public void addAll(Collection<ReportDocument> reportDocuments) {
        for (ReportDocument reportDocument : reportDocuments) {
            addReportDocument(reportDocument);
        }
    }

    /**
     * Call the {@link ReportDocumentRepository#getReportDocumentsForAddition()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    public List<ReportDocument> getReportDocumentsForAddition() {
        return ReportDocumentRepository.getInstance().getReportDocumentsForAddition();
    }

    /**
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocumentsForAddition()} list.
     *
     * @param reportDocument will be appended if it contains the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     */
    public void addReportDocumentForAddition(ReportDocument reportDocument) {
        if (reportDocument.getReport() == null) {
            throw new SubstitutionRuntimeException("Report is mandatory for ReportDocument: " + reportDocument);
        }
        ReportDocumentRepository.getInstance().getReportDocumentsForAddition().add(reportDocument);
    }

    /**
     * For avoiding of {@link ConcurrentModificationException} a newly created {@link ReportDocument} is
     * appended to the {@link #getReportDocumentsForAddition()} collection. This method will append this
     * collection to the {@link #getReportDocuments()} collection and clear it.
     */
    public void mergeReportDocumentsForAddition() {
        List<ReportDocument> reportDocuments = getReportDocuments();
        reportDocuments.addAll(getReportDocumentsForAddition());
        getReportDocumentsForAddition().clear();
    }

    /**
     * Find all {@link ReportDocument}s with report from parameter.
     * @param report the {@link ReportDocument#getReport()} value
     * @return 'null' if not found
     */
    public List<ReportDocument> getReportDocuments(Report report) {
        ReportDocumentList<ReportDocument> reportDocuments = (ReportDocumentList<ReportDocument>) getReportDocuments();
        return reportDocuments.get(report);
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
