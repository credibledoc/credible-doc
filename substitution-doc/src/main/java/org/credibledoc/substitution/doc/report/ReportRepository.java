package org.credibledoc.substitution.doc.report;

import org.springframework.stereotype.Repository;

/**
 * This repository contains a single {@link #report} object.
 */
@Repository
public class ReportRepository {

    /**
     * The global state of the application.
     */
    private Report report;

    /**
     * @return The {@link #report} field value.
     */
    public Report getReport() {
        return report;
    }

    /**
     * @param report see the {@link #report} field
     */
    public void setReport(Report report) {
        this.report = report;
    }
}
