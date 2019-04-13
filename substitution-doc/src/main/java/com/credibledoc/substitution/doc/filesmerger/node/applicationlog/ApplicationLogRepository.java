package com.credibledoc.substitution.doc.filesmerger.node.applicationlog;

import com.credibledoc.substitution.doc.report.Report;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful object. Contains a list of {@link ApplicationLog}.
 */
@Repository
class ApplicationLogRepository {

    /**
     * Contains {@link ApplicationLog}s of the {@link Report}
     */
    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    /**
     * @return the {@link #applicationLogs} value
     */
    List<ApplicationLog> getApplicationLogs() {
        return applicationLogs;
    }

}
