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
 *     <li>{@link #printableToLineProcessorsMap}</li>
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
    private Map<Printable, List<LineProcessor>> printableToLineProcessorsMap = new HashMap<>();

    /**
     * Create new instances of
     * <ul>
     *     <li>{@link #lineProcessorRepository}</li>
     *     <li>{@link #printableToLineProcessorsMap}</li>
     * </ul>
     * @return the current instance
     */
    public EnricherContext init() {
        lineProcessorRepository = new LineProcessorRepository();
        printableToLineProcessorsMap = new HashMap<>();
        return this;
    }

    @Override
    public String toString() {
        return "EnricherContext{" +
            "lineProcessorRepository=" + lineProcessorRepository +
            ", derivingToLineProcessorsMap=" + printableToLineProcessorsMap +
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
     * @return The {@link #printableToLineProcessorsMap} field value.
     */
    public Map<Printable, List<LineProcessor>> getPrintableToLineProcessorsMap() {
        return printableToLineProcessorsMap;
    }

    /**
     * @param printableToLineProcessorsMap see the {@link #printableToLineProcessorsMap} field description.
     */
    public void setPrintableToLineProcessorsMap(Map<Printable, List<LineProcessor>> printableToLineProcessorsMap) {
        this.printableToLineProcessorsMap = printableToLineProcessorsMap;
    }
}
