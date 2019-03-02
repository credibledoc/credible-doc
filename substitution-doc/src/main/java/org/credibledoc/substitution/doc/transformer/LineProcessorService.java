package org.credibledoc.substitution.doc.transformer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * A service for working with {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LineProcessorService {
    private Map<ReportDocument, List<LineProcessor>> reportDocumentLineProcessorMap = new HashMap<>();

    @NonNull
    private final LineProcessorRepository lineProcessorRepository;

    /**
     * Call the {@link LineProcessorRepository#getLineProcessors()} method
     * @return all {@link LineProcessor}s.
     */
    public List<LineProcessor> getLineProcessors() {
        return lineProcessorRepository.getLineProcessors();
    }

    /**
     * Find {@link LineProcessor}s which belong to a {@link ReportDocument}.
     * @param reportDocument for searching {@link LineProcessor#getReportDocument()}
     * @return list of {@link LineProcessor}s
     */
    public List<LineProcessor> getLineProcessors(ReportDocument reportDocument) {
        if (reportDocumentLineProcessorMap.isEmpty()) {
            initializeCache();
        }
        if (reportDocumentLineProcessorMap.containsKey(reportDocument)) {
            return reportDocumentLineProcessorMap.get(reportDocument);
        }
        return Collections.emptyList();
    }

    public void initializeCache() {
        for (LineProcessor lineProcessor : getLineProcessors()) {
            ReportDocument nextReportDocument = lineProcessor.getReportDocument();
            if (reportDocumentLineProcessorMap.containsKey(nextReportDocument)) {
                reportDocumentLineProcessorMap.get(nextReportDocument).add(lineProcessor);
            } else {
                List<LineProcessor> list = new ArrayList<>();
                list.add(lineProcessor);
                reportDocumentLineProcessorMap.put(nextReportDocument, list);
            }
        }
    }
}
