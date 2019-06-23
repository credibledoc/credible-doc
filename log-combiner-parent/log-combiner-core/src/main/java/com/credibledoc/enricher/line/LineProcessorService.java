package com.credibledoc.enricher.line;

import com.credibledoc.enricher.printable.Printable;

import java.util.*;

/**
 * A service for working with {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessorService {
    private Map<Printable, List<LineProcessor>> derivingToLineProcessorsMap = new HashMap<>();

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
     *
     * @return all {@link LineProcessor}s.
     */
    public List<LineProcessor> getLineProcessors() {
        return LineProcessorRepository.getInstance().getLineProcessors();
    }

    /**
     * Find {@link LineProcessor}s which belong to a {@link Printable}.
     * @param printable an object in the {@link LineProcessor#getPrintable()} value
     * @return list of {@link LineProcessor}s
     */
    public List<LineProcessor> getLineProcessors(Printable printable) {
        if (derivingToLineProcessorsMap.isEmpty()) {
            initializeCache();
        }
        if (derivingToLineProcessorsMap.containsKey(printable)) {
            return derivingToLineProcessorsMap.get(printable);
        }
        return Collections.emptyList();
    }

    /**
     * Evict the {@link #derivingToLineProcessorsMap} cache and add all {@link LineProcessor}s
     * to the {@link LineProcessorRepository}. Please use this method instead of direct addition to the
     * {@link #getLineProcessors()} list.
     *
     * @param lineProcessors rules for searching and transformation.
     */
    public void addAll(List<LineProcessor> lineProcessors) {
        derivingToLineProcessorsMap.clear();
        LineProcessorRepository.getInstance().getLineProcessors().addAll(lineProcessors);
    }

    private void initializeCache() {
        for (LineProcessor lineProcessor : getLineProcessors()) {
            Printable printable = lineProcessor.getPrintable();
            if (derivingToLineProcessorsMap.containsKey(printable)) {
                derivingToLineProcessorsMap.get(printable).add(lineProcessor);
            } else {
                List<LineProcessor> list = new ArrayList<>();
                list.add(lineProcessor);
                derivingToLineProcessorsMap.put(printable, list);
            }
        }
    }
}
