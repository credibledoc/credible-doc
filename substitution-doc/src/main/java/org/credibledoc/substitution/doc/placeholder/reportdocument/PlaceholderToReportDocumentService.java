package org.credibledoc.substitution.doc.placeholder.reportdocument;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * The stateless service for working with relationship between {@link ReportDocument}
 * and {@link com.credibledoc.substitution.core.placeholder.Placeholder}.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderToReportDocumentService {
    @NonNull
    private PlaceholderToReportDocumentRepository placeholderToReportDocumentRepository;

    /**
     * Find a value from {@link PlaceholderToReportDocumentRepository#getPlaceholderToReportDocumentMap()} by the key
     * @param placeholder a key
     * @return ReportDocument from the map.
     */
    public ReportDocument getReportDocument(Placeholder placeholder) {
        return placeholderToReportDocumentRepository.getPlaceholderToReportDocumentMap().get(placeholder);
    }

    /**
     * Put arguments to the {@link PlaceholderToReportDocumentRepository#getPlaceholderToReportDocumentMap()} map.
     * @param placeholder a key
     * @param reportDocument a value
     */
    public void putPlaceholderToReportDocument(Placeholder placeholder, ReportDocument reportDocument) {
        placeholderToReportDocumentRepository.getPlaceholderToReportDocumentMap().put(placeholder, reportDocument);
    }
}
