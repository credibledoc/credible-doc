package com.credibledoc.substitution.reporting.placeholder;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;

/**
 * The stateless service for working with relationship between {@link ReportDocument}
 * and {@link com.credibledoc.substitution.core.placeholder.Placeholder}.
 *
 * @author Kyrylo Semenko
 */
public class PlaceholderToReportDocumentService {

    /**
     * Singleton.
     */
    private static final PlaceholderToReportDocumentService instance = new PlaceholderToReportDocumentService();

    /**
     * @return The {@link PlaceholderToReportDocumentService} singleton.
     */
    public static PlaceholderToReportDocumentService getInstance() {
        return instance;
    }

    /**
     * Find a value from {@link PlaceholderToReportDocumentRepository#getPlaceholderToReportDocumentMap()} by the key
     * @param placeholder a key
     * @return ReportDocument from the map.
     */
    public ReportDocument getReportDocument(Placeholder placeholder) {
        return PlaceholderToReportDocumentRepository.getInstance()
            .getPlaceholderToReportDocumentMap().get(placeholder);
    }

    /**
     * Put arguments to the {@link PlaceholderToReportDocumentRepository#getPlaceholderToReportDocumentMap()} map.
     * @param placeholder a key
     * @param reportDocument a value
     */
    public void putPlaceholderToReportDocument(Placeholder placeholder, ReportDocument reportDocument) {
        PlaceholderToReportDocumentRepository.getInstance()
            .getPlaceholderToReportDocumentMap().put(placeholder, reportDocument);
    }
}
