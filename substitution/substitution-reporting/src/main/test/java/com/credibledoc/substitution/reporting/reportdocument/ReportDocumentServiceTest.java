package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.substitution.reporting.report.Report;
import com.credibledoc.substitution.reporting.report.document.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportDocumentServiceTest {

    @Test
    public void appendReportDocumentsForAddition() {
        Report report = new Report();
        
        ReportDocument reportDocument = new Document();
        reportDocument.setReport(report);
        ReportDocumentService reportDocumentService = ReportDocumentService.getInstance();
        reportDocumentService.getReportDocumentsForAddition().add(reportDocument);
        
        reportDocumentService.mergeReportDocumentsForAddition();
        
        assertEquals(reportDocument, reportDocumentService.getReportDocuments(report).get(0));
        assertTrue(reportDocumentService.getReportDocumentsForAddition().isEmpty());
    }
}
