package com.credibledoc.enricher.context;

import com.credibledoc.enricher.line.LineProcessor;
import com.credibledoc.enricher.line.LineProcessorRepository;
import com.credibledoc.enricher.printable.Printable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains instances of stateful objects (repositories) used in Combiner:
 * <ul>
 *     <li>{@link #lineProcessorRepository}</li>
 *     <li>{@link #derivingToLineProcessorsMap}</li>
 * </ul>
 *
 * @author Kyrylo Semenko
 */
public class EnricherContext {
    /**
     * Contains {@link com.credibledoc.enricher.line.LineProcessor}s.
     */
    private LineProcessorRepository lineProcessorRepository;

    /**
     * Map where a key is a report document and a value is a list of parsers.
     */
    // TODO Kyrylo Semenko - rename to printableToLineProcessors
    private Map<Printable, List<LineProcessor>> derivingToLineProcessorsMap = new HashMap<>();

    /**
     * Create new instances of
     * <ul>
     *     <li>{@link #lineProcessorRepository}</li>
     *     <li>{@link #derivingToLineProcessorsMap}</li>
     * </ul>
     * @return the current instance
     */
    public EnricherContext init() {
        lineProcessorRepository = new LineProcessorRepository();
        derivingToLineProcessorsMap = new HashMap<>();
        return this;
    }

    @Override
    public String toString() {
        return "EnricherContext{" +
            "lineProcessorRepository=" + lineProcessorRepository +
            ", derivingToLineProcessorsMap=" + derivingToLineProcessorsMap +
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

    /**
     * @return The {@link #derivingToLineProcessorsMap} field value.
     */
    public Map<Printable, List<LineProcessor>> getDerivingToLineProcessorsMap() {
        return derivingToLineProcessorsMap;
    }

    /**
     * @param derivingToLineProcessorsMap see the {@link #derivingToLineProcessorsMap} field description.
     */
    public void setDerivingToLineProcessorsMap(Map<Printable, List<LineProcessor>> derivingToLineProcessorsMap) {
        this.derivingToLineProcessorsMap = derivingToLineProcessorsMap;
    }
}
