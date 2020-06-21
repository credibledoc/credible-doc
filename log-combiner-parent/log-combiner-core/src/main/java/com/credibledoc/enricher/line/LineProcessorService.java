package com.credibledoc.enricher.line;

import com.credibledoc.enricher.context.EnricherContext;
import com.credibledoc.enricher.printable.Printable;

import java.util.*;

/**
 * A service for working with {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessorService {
    // TODO Kyrylo Semenko - move to context
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
     * @param enricherContext the current state
     * @return all {@link LineProcessor}s.
     */
    public List<LineProcessor> getLineProcessors(EnricherContext enricherContext) {
        return enricherContext.getLineProcessorRepository().getLineProcessors();
    }

    /**
     * Find {@link LineProcessor}s which belong to a {@link Printable}.
     * @param printable an object in the {@link LineProcessor#getPrintable()} value
     * @param enricherContext the current state
     * @return list of {@link LineProcessor}s
     */
    public List<LineProcessor> getLineProcessors(Printable printable, EnricherContext enricherContext) {
        if (derivingToLineProcessorsMap.isEmpty()) {
            initializeCache(enricherContext);
        }
        if (derivingToLineProcessorsMap.containsKey(printable)) {
            return derivingToLineProcessorsMap.get(printable);
        }
        return Collections.emptyList();
    }

    /**
     * Evict the {@link #derivingToLineProcessorsMap} cache and add all {@link LineProcessor}s
     * to the {@link LineProcessorRepository}. Please use this method instead of direct addition to the
     * {@link #getLineProcessors(EnricherContext)} list.
     *
     * @param lineProcessors rules for searching and transformation.
     * @param enricherContext the current state
     */
    public void addAll(List<LineProcessor> lineProcessors, EnricherContext enricherContext) {
        derivingToLineProcessorsMap.clear();
        enricherContext.getLineProcessorRepository().getLineProcessors().addAll(lineProcessors);
    }

    private void initializeCache(EnricherContext enricherContext) {
        for (LineProcessor lineProcessor : getLineProcessors(enricherContext)) {
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
