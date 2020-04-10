package com.credibledoc.substitution.reporting.reportdocument.creator;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateful singleton. Contains the {@link #map}.
 *
 * @author Kyrylo Semenko
 */
public class ReportDocumentCreatorRepository {

    /**
     * This map contains {@link ReportDocumentCreator}s created by a client application where the keys are
     * the map values implementation types.
     */
    private Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map = new HashMap<>();

    /**
     * @return The {@link #map} field value.
     */
    public Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> getMap() {
        return map;
    }

    /**
     * @param map see the {@link #map} field description.
     */
    public void setMap(Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map) {
        this.map = map;
    }
}
