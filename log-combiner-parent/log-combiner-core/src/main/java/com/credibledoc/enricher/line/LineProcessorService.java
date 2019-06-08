package com.credibledoc.enricher.line;

import com.credibledoc.enricher.deriving.Deriving;

import java.util.*;

/**
 * A service for working with {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessorService {
    private Map<Deriving, List<LineProcessor>> derivingToLineProcessorsMap = new HashMap<>();

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
        if (derivingToLineProcessorsMap.isEmpty()) {
            initializeCache();
        }
        if (derivingToLineProcessorsMap.containsKey(deriving)) {
            return derivingToLineProcessorsMap.get(deriving);
        }
        return Collections.emptyList();
    }

    private void initializeCache() {
        for (LineProcessor lineProcessor : getLineProcessors()) {
            Deriving deriving = lineProcessor.getDeriving();
            if (derivingToLineProcessorsMap.containsKey(deriving)) {
                derivingToLineProcessorsMap.get(deriving).add(lineProcessor);
            } else {
                List<LineProcessor> list = new ArrayList<>();
                list.add(lineProcessor);
                derivingToLineProcessorsMap.put(deriving, list);
            }
        }
    }
}
