package com.credibledoc.substitution.reporting.report;

import java.util.ArrayList;
import java.util.List;

/**
 * This repository contains a list of {@link Report} objects.
 */
public class ReportRepository {

    /**
     * The global state of the application.
     */
    private List<Report> reports = new ArrayList<>();

    /**
     * @return The {@link #reports} field value.
     */
    public List<Report> getReports() {
        return reports;
    }

    /**
     * Add all reports to the {@link #reports}.
     * @param reports for appending
     */
    public void addReports(List<Report> reports) {
        this.reports.addAll(reports);
    }
}
