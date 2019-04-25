package com.credibledoc.combiner.doc.report;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * This repository contains a list of {@link Report} objects.
 */
@Repository
class ReportRepository {

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
