package org.credibledoc.substitution.doc.reportdocument.creator;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateful singleton. Contains the {@link #map}.
 *
 * @author Kyrylo Semenko
 */
@Repository
public class ReportDocumentCreatorRepository {

    /**
     * This map contains {@link ReportDocumentCreator}s created by a client application where the keys are
     * the map values implementation types.
     */
    private Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map = new HashMap<>();

    /**
     * @return The {@link #map} field value.
     */
    Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> getMap() {
        return map;
    }

    /**
     * @param map see the {@link #map} field description.
     */
    void setMap(Map<Class<? extends ReportDocumentCreator>, ReportDocumentCreator> map) {
        this.map = map;
    }
}
