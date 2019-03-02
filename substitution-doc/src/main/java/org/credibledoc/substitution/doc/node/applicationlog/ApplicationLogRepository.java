package org.credibledoc.substitution.doc.node.applicationlog;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful object. Contains a list of {@link ApplicationLog}.
 */
@Repository
public class ApplicationLogRepository {

    /**
     * Contains {@link ApplicationLog}s of the {@link org.credibledoc.substitution.doc.report.Report}
     */
    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    /**
     * @return the {@link #applicationLogs} value
     */
    public List<ApplicationLog> getApplicationLogs() {
        return applicationLogs;
    }

    /**
     * @param applicationLogs see the {@link #applicationLogs} field
     */
    public void setApplicationLogs(List<ApplicationLog> applicationLogs) {
        this.applicationLogs = applicationLogs;
    }
}
