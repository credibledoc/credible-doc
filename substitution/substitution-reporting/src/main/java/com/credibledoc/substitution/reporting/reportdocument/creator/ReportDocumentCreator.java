package com.credibledoc.substitution.reporting.reportdocument.creator;

import com.credibledoc.enricher.context.EnricherContext;
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
     * @param enricherContext the current state of {@link EnricherContext#getLineProcessorRepository()}.
     * @return a stateful object which can write lines to a single report file.
     */
    ReportDocument prepareReportDocument(EnricherContext enricherContext);

    /**
     * Returns the {@link ReportDocumentType} class of the {@link ReportDocument}
     * @return the current {@link ReportDocument} type.
     */
    Class<? extends ReportDocumentType> getReportDocumentType();
}
