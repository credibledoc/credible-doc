package com.credibledoc.substitution.reporting.report;

import java.util.ArrayList;
import java.util.List;

/**
 * This repository contains a list of {@link Report} objects.
 */
class ReportRepository {

    /**
     * Singleton.
     */
    private static ReportRepository instance;

    /**
     * @return The {@link ReportRepository} singleton.
     */
    public static ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }
        return instance;
    }

    /**
     * The global state of the application.
     */
    private List<Report> reports = new ArrayList<>();

    /**
     * @return The {@link #reports} field value.
     */
    List<Report> getReports() {
        return reports;
    }

    /**
     * Add all reports to the {@link #reports}.
     * @param reports for appending
     */
    void addReports(List<Report> reports) {
        this.reports.addAll(reports);
    }
}
