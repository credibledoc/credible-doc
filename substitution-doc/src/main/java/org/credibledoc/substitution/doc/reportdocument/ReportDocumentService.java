package org.credibledoc.substitution.doc.reportdocument;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * A stateless service for working with {@link ReportDocument}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportDocumentService {
    @NonNull
    private ReportDocumentRepository reportDocumentRepository;

    /**
     * Call the {@link ReportDocumentRepository#getReportDocuments()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    public List<ReportDocument> getReportDocuments() {
        return reportDocumentRepository.getReportDocuments();
    }

    /**
     * Call the {@link ReportDocumentRepository#getReportDocumentsForAddition()} method.
     * @return all {@link ReportDocument}s from the {@link ReportDocumentRepository}.
     */
    public List<ReportDocument> getReportDocumentsForAddition() {
        return reportDocumentRepository.getReportDocumentsForAddition();
    }

    /**
     * Wee need to avoid {@link ConcurrentModificationException}, so we
     * can`t modify {@link ReportDocumentService#getReportDocuments()} directly.
     */
    public void appendReportDocumentsForAddition() {
        getReportDocuments().addAll(getReportDocumentsForAddition());
        getReportDocumentsForAddition().clear();
    }
}
