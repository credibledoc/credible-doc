package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.reporting.context.ReportingContext;
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
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocuments()} list.
     *
     * @param reportDocument will be appended if it contains the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     * @param reportingContext the current state
     */
    public void addReportDocument(ReportDocument reportDocument, ReportingContext reportingContext) {
        if (reportDocument.getReport() == null) {
            throw new SubstitutionRuntimeException("Report cannot be empty in ReportDocument. " +
                "ReportDocument: " + reportDocument);
        }
        reportingContext.getReportDocumentRepository().getReportDocuments().add(reportDocument);
    }

    /**
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocuments()} list.
     *
     * @param reportDocuments will be appended if all items contain the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     * @param reportingContext the current state
     */
    public void addAll(Collection<ReportDocument> reportDocuments, ReportingContext reportingContext) {
        for (ReportDocument reportDocument : reportDocuments) {
            addReportDocument(reportDocument, reportingContext);
        }
    }

    /**
     * Validate and append the {@link ReportDocument} to the
     * {@link ReportDocumentRepository#getReportDocumentsForAddition()} list.
     *
     * @param reportDocument will be appended if it contains the {@link ReportDocument#getReport()} field,
     *                       else an exception will be thrown.
     * @param reportingContext the current state
     */
    public void addReportDocumentForAddition(ReportDocument reportDocument, ReportingContext reportingContext) {
        if (reportDocument.getReport() == null) {
            throw new SubstitutionRuntimeException("Report is mandatory for ReportDocument: " + reportDocument);
        }
        reportingContext.getReportDocumentRepository().getReportDocumentsForAddition().add(reportDocument);
    }

    /**
     * For avoiding {@link ConcurrentModificationException} a newly created {@link ReportDocument} is
     * appended to the {@link ReportDocumentRepository#getReportDocumentsForAddition()} collection.
     * The current method will move all {@link ReportDocument}s from {@link ReportDocumentRepository#getReportDocumentsForAddition()}
     * to {@link ReportDocumentRepository#getReportDocuments()}.
     * @param reportingContext the current state
     */
    public void mergeReportDocumentsForAddition(ReportingContext reportingContext) {
        List<ReportDocument> reportDocuments = reportingContext.getReportDocumentRepository().getReportDocuments();
        List<ReportDocument> reportDocumentsForAddition =
            reportingContext.getReportDocumentRepository().getReportDocumentsForAddition();
        reportDocuments.addAll(reportDocumentsForAddition);
        reportDocumentsForAddition.clear();
    }

    /**
     * Find all {@link ReportDocument}s with report from parameter.
     * @param report the {@link ReportDocument#getReport()} value
     * @return 'null' if not found
     */
    public List<ReportDocument> getReportDocuments(Report report, ReportingContext reportingContext) {
        ReportDocumentList<ReportDocument> reportDocuments =
            reportingContext.getReportDocumentRepository().getReportDocuments();
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
