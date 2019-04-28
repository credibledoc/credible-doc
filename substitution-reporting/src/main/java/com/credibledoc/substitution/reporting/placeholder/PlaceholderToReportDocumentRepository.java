package com.credibledoc.substitution.reporting.placeholder;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateful class. Contains a {@link #placeholderToReportDocumentMap}.
 *
 * @author Kyrylo Semenko
 */
public class PlaceholderToReportDocumentRepository {

    /**
     * Singleton.
     */
    private static PlaceholderToReportDocumentRepository instance;

    /**
     * @return The {@link PlaceholderToReportDocumentRepository} singleton.
     */
    public static PlaceholderToReportDocumentRepository getInstance() {
        if (instance == null) {
            instance = new PlaceholderToReportDocumentRepository();
        }
        return instance;
    }

    /**
     * This map is filled out during a preparation phase and used in a generation phase.
     * It maps {@link Placeholder} to {@link ReportDocument}.
     */
    private Map<Placeholder, ReportDocument> placeholderToReportDocumentMap = new HashMap<>();

    /**
     * @return The {@link #placeholderToReportDocumentMap} field value.
     */
    public Map<Placeholder, ReportDocument> getPlaceholderToReportDocumentMap() {
        return placeholderToReportDocumentMap;
    }

    /**
     * @param placeholderToReportDocumentMap see the {@link #placeholderToReportDocumentMap} field description.
     */
    public void setPlaceholderToReportDocumentMap(Map<Placeholder, ReportDocument> placeholderToReportDocumentMap) {
        this.placeholderToReportDocumentMap = placeholderToReportDocumentMap;
    }
}
