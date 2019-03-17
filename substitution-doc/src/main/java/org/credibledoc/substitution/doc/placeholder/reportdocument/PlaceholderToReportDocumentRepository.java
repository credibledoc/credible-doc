package org.credibledoc.substitution.doc.placeholder.reportdocument;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateful class. Contains a {@link #placeholderToReportDocumentMap}.
 *
 * @author Kyrylo Semenko
 */
@Repository
public class PlaceholderToReportDocumentRepository {

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
