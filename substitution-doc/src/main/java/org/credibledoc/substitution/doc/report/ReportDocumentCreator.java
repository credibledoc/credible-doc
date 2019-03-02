package org.credibledoc.substitution.doc.report;

import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentType;

/**
 * Classes which implement this interface should be able to create {@link ReportDocument}
 * of concrete {@link ReportDocumentType}.
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
