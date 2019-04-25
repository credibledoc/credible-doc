package com.credibledoc.combiner.doc.reportdocument.creator;

import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.reportdocument.ReportDocumentType;

/**
 * Classes which implement this interface should be able to create {@link ReportDocument}
 * of a {@link ReportDocumentType}.
 *
 * @author Kyrylo Semenko
 */
public interface ReportDocumentCreator {

    /**
     * Create a new {@link ReportDocument}.
     * @return a stateful object which can write lines to a single report file.
     */
    ReportDocument prepareReportDocument();

    /**
     * Returns the {@link ReportDocumentType} of the {@link ReportDocument}
     * @return the current {@link ReportDocument} type.
     */
    ReportDocumentType getReportDocumentType();
}
