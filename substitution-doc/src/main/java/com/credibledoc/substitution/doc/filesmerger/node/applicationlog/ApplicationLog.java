package com.credibledoc.substitution.doc.filesmerger.node.applicationlog;

import com.credibledoc.substitution.doc.filesmerger.application.Application;
import com.credibledoc.substitution.doc.filesmerger.node.log.NodeLog;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
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
