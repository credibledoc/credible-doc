package com.credibledoc.substitution.reporting.report;

import java.util.List;

/**
 * Provides access to the {@link ReportRepository} and serves {@link Report}s.
 *
 * @author Kyrylo Semenko
 */
public class ReportService {
    public static final String MODULE_NAME = "substitution-reporting";

    /**
     * Singleton.
     */
    private static ReportService instance;

    /**
     * @return The {@link ReportService} singleton.
     */
    public static ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    /**
     * Call the {@link ReportRepository#getReports()} method.
     * @return the global application state.
     */
    public List<Report> getReports() {
        return ReportRepository.getInstance().getReports();
    }

    /**
     * Add all reports to the {@link ReportRepository}.
     * @param reports for appending
     */
    public void addReports(List<Report> reports) {
        ReportRepository.getInstance().addReports(reports);
    }
}
