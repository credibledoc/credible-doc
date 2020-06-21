package com.credibledoc.enricher.line;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful singleton. Repository of {@link LineProcessor}s.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessorRepository {
    /**
     * All {@link LineProcessor}s of Visualizer.
     */
    private List<LineProcessor> lineProcessors = new ArrayList<>();

    /**
     * @return the {@link #lineProcessors} value
     */
    public List<LineProcessor> getLineProcessors() {
        return lineProcessors;
    }

    /**
     * @param lineProcessors see the {@link #lineProcessors} field
     */
    public void setLineProcessors(List<LineProcessor> lineProcessors) {
        this.lineProcessors = lineProcessors;
    }
}
