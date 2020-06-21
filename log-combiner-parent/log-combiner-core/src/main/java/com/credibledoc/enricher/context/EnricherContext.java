package com.credibledoc.enricher.context;

import com.credibledoc.enricher.line.LineProcessorRepository;

/**
 * Contains instances of stateful objects (repositories) used in Combiner:
 * <ul>
 *     <li>{@link #lineProcessorRepository}</li>
 * </ul>
 *
 * @author Kyrylo Semenko
 */
public class EnricherContext {
    /**
     * Contains {@link com.credibledoc.enricher.line.LineProcessor}s.
     */
    private LineProcessorRepository lineProcessorRepository;
    
    public EnricherContext init() {
        lineProcessorRepository = new LineProcessorRepository();
        return this;
    }

    @Override
    public String toString() {
        return "EnricherContext{" +
            "lineProcessorRepository=" + lineProcessorRepository +
            '}';
    }

    /**
     * @return The {@link #lineProcessorRepository} field value.
     */
    public LineProcessorRepository getLineProcessorRepository() {
        return lineProcessorRepository;
    }

    /**
     * @param lineProcessorRepository see the {@link #lineProcessorRepository} field description.
     */
    public void setLineProcessorRepository(LineProcessorRepository lineProcessorRepository) {
        this.lineProcessorRepository = lineProcessorRepository;
    }
}
