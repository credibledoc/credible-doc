package com.credibledoc.enricher.line;

import com.credibledoc.enricher.deriving.Deriving;

import java.util.*;

/**
 * A service for working with {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessorService {
    private Map<Deriving, List<LineProcessor>> reportDocumentLineProcessorMap = new HashMap<>();

    /**
     * Singleton.
     */
    private static LineProcessorService instance;

    /**
     * @return The {@link LineProcessorService} singleton.
     */
    public static LineProcessorService getInstance() {
        if (instance == null) {
            instance = new LineProcessorService();
        }
        return instance;
    }

    /**
     * Call the {@link LineProcessorRepository#getLineProcessors()} method
     * @return all {@link LineProcessor}s.
     */
    public List<LineProcessor> getLineProcessors() {
        return LineProcessorRepository.getInstance().getLineProcessors();
    }

    /**
     * Find {@link LineProcessor}s which belong to a {@link Deriving}.
     * @param deriving for searching {@link LineProcessor#getDeriving()}
     * @return list of {@link LineProcessor}s
     */
    public List<LineProcessor> getLineProcessors(Deriving deriving) {
        if (reportDocumentLineProcessorMap.isEmpty()) {
            initializeCache();
        }
        if (reportDocumentLineProcessorMap.containsKey(deriving)) {
            return reportDocumentLineProcessorMap.get(deriving);
        }
        return Collections.emptyList();
    }

    private void initializeCache() {
        for (LineProcessor lineProcessor : getLineProcessors()) {
            Deriving deriving = lineProcessor.getDeriving();
            if (reportDocumentLineProcessorMap.containsKey(deriving)) {
                reportDocumentLineProcessorMap.get(deriving).add(lineProcessor);
            } else {
                List<LineProcessor> list = new ArrayList<>();
                list.add(lineProcessor);
                reportDocumentLineProcessorMap.put(deriving, list);
            }
        }
    }
}
