package com.credibledoc.substitution.reporting.reportdocument.creator;

import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;

/**
 * Classes which implement this interface are able to create
 * a {@link ReportDocument} of a {@link ReportDocumentType}.
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
     * Returns the {@link ReportDocumentType} class of the {@link ReportDocument}
     * @return the current {@link ReportDocument} type.
     */
    Class<? extends ReportDocumentType> getReportDocumentType();
}
