package org.credibledoc.substitution.doc.reportdocument.creator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The stateless service for working with {@link ReportDocumentCreator}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportDocumentCreatorService {
    @NonNull
    private ReportDocumentCreatorRepository reportDocumentCreatorRepository;

    /**
     * Add all items to the {@link ReportDocumentCreatorRepository#getMap()} entries.
     *
     * @param reportDocumentCreators items for addition
     */
    public void addReportDocumentCreators(Collection<ReportDocumentCreator> reportDocumentCreators) {
        Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map = new HashMap<>();
        for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreators) {
            map.put(reportDocumentCreator.getClass(), reportDocumentCreator);
        }
        reportDocumentCreatorRepository.getMap().putAll(map);
    }

    public ReportDocumentCreator getReportDocumentCreator(Class<?> placeholderClass) {
        return reportDocumentCreatorRepository.getMap().get(placeholderClass);
    }
}
