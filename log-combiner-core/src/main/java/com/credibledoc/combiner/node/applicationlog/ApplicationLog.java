package com.credibledoc.combiner.node.applicationlog;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.node.log.NodeLog;

/**
 * <p>
 * This data object represents a list of log files from the concrete {@link Application}.
 *
 * <p>
 * These files will be parsed and visualized.
 *
 * @author Kyrylo Semenko
 */
public class ApplicationLog {

    /**
     * The {@link Application} the log {@link NodeLog}s belong to
     */
    private Application application;

    @Override
    public String toString() {
        return "ApplicationLog{" + "application=\"" + application + "\"}";
    }

    /**
     * An empty constructor
     */
    public ApplicationLog() {
        // empty
    }

    /**
     * @return The {@link #application} field value.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * @param application see the {@link #application} field description.
     */
    public void setApplication(Application application) {
        this.application = application;
    }
}
