package com.credibledoc.combiner.node.applicationlog;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful object. Contains a list of {@link ApplicationLog}.
 */
class ApplicationLogRepository {
    // TODO Kyrylo Semenko - nesmi byt singleton

    /**
     * Singleton.
     */
    private static ApplicationLogRepository instance;

    /**
     * @return The {@link ApplicationLogRepository} singleton.
     */
    public static ApplicationLogRepository getInstance() {
        if (instance == null) {
            instance = new ApplicationLogRepository();
        }
        return instance;
    }

    /**
     * Contains a list of {@link ApplicationLog}s
     */
    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    /**
     * @return the {@link #applicationLogs} value
     */
    List<ApplicationLog> getApplicationLogs() {
        return applicationLogs;
    }

}
