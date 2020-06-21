package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.substitution.reporting.context.ReportingContext;
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
        
        ReportingContext reportingContext = new ReportingContext().init();
        reportingContext.getReportRepository().getReports().add(report);
        reportingContext.getReportDocumentRepository().getReportDocuments().add(reportDocument);

        ReportDocumentService reportDocumentService = ReportDocumentService.getInstance();
        reportDocumentService.mergeReportDocumentsForAddition(reportingContext);
        
        assertEquals(reportDocument, reportDocumentService.getReportDocuments(report, reportingContext).get(0));
        assertTrue(reportingContext.getReportDocumentRepository().getReportDocumentsForAddition().isEmpty());
    }
}
