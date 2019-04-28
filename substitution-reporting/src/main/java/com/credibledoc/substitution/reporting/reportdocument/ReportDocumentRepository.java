package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.substitution.reporting.visualizer.VisualizerService;

import java.util.ArrayList;
import java.util.List;

/**
 * A stateful class. Contains {@link #reportDocuments} and {@link #reportDocumentsForAddition}.
 *
 * @author Kyrylo Semenko
 */
public class ReportDocumentRepository {

    /**
     * Singleton.
     */
    private static ReportDocumentRepository instance;

    /**
     * @return The {@link ReportDocumentRepository} singleton.
     */
    public static ReportDocumentRepository getInstance() {
        if (instance == null) {
            instance = new ReportDocumentRepository();
        }
        return instance;
    }

    /**
     * All {@link ReportDocument}s that will be completed during parsing.
     * {@link VisualizerService} will read line by
     * line source files and for each line will try to apply all
     * {@link LineProcessor}s related with all reportDocuments.
     * If the current log line is applicable to the
     * current {@link ReportDocument}, it can be transformed and appended
     * to the {@link ReportDocument#getCacheLines()} or / and returned from the
     * current {@link LineProcessor}. Returned
     * transformed line will be written to a report file immediately.
     */
    private List<ReportDocument> reportDocuments = new ArrayList<>();

    /**
     * During parsing of files we can add {@link ReportDocument}s to the
     * {@link ReportDocument}s list. We can`t add them to the list directly,
     * because {@link java.util.ConcurrentModificationException} is throwing.
     * So wee need to add them tho this reportDocumentsForAddition
     * first, and merge this list to the {@link ReportDocument}s after the
     * current log line has been processed by all items from the
     * {@link ReportDocument}s list. See the
     * {@link VisualizerService} createReports method.
     */
    private List<ReportDocument> reportDocumentsForAddition = new ArrayList<>();

    /**
     * @return The {@link #reportDocuments} field value.
     */
    public List<ReportDocument> getReportDocuments() {
        return reportDocuments;
    }

    /**
     * @param reportDocuments see the {@link #reportDocuments} field
     */
    public void setReportDocuments(List<ReportDocument> reportDocuments) {
        this.reportDocuments = reportDocuments;
    }

    /**
     * @return The {@link #reportDocumentsForAddition} field value.
     */
    public List<ReportDocument> getReportDocumentsForAddition() {
        return reportDocumentsForAddition;
    }

    /**
     * @param reportDocumentsForAddition see the {@link #reportDocumentsForAddition} field
     */
    public void setReportDocumentsForAddition(List<ReportDocument> reportDocumentsForAddition) {
        this.reportDocumentsForAddition = reportDocumentsForAddition;
    }
}
