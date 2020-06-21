package com.credibledoc.substitution.reporting.context;

import com.credibledoc.substitution.reporting.report.ReportRepository;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentRepository;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreatorRepository;

/**
 * Contains instances of stateful objects (repositories) used in Reporting:
 * <ul>
 *     <li>{@link #reportDocumentCreatorRepository}</li>
 *     <li>{@link #reportDocumentRepository}</li>
 *     <li>{@link #reportRepository}</li>
 * </ul>
 * 
 * @author Kyrylo Semenko
 */
public class ReportingContext {
    /**
     * Contains {@link com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator} instances.
     */
    private ReportDocumentCreatorRepository reportDocumentCreatorRepository;

    /**
     * Contains {@link com.credibledoc.substitution.reporting.reportdocument.ReportDocument} instances.
     */
    private ReportDocumentRepository reportDocumentRepository;

    /**
     * Contains {@link com.credibledoc.substitution.reporting.report.Report} instances.
     */
    private ReportRepository reportRepository;

    @Override
    public String toString() {
        return "ReportingContext{" +
            "reportDocumentCreatorRepository=" + reportDocumentCreatorRepository +
            ", reportDocumentRepository=" + reportDocumentRepository +
            ", reportRepository=" + reportRepository +
            '}';
    }

    /**
     * @return The {@link #reportDocumentCreatorRepository} field value.
     */
    public ReportDocumentCreatorRepository getReportDocumentCreatorRepository() {
        return reportDocumentCreatorRepository;
    }

    /**
     * @param reportDocumentCreatorRepository see the {@link #reportDocumentCreatorRepository} field description.
     */
    public void setReportDocumentCreatorRepository(ReportDocumentCreatorRepository reportDocumentCreatorRepository) {
        this.reportDocumentCreatorRepository = reportDocumentCreatorRepository;
    }

    /**
     * @return The {@link #reportDocumentRepository} field value.
     */
    public ReportDocumentRepository getReportDocumentRepository() {
        return reportDocumentRepository;
    }

    /**
     * @param reportDocumentRepository see the {@link #reportDocumentRepository} field description.
     */
    public void setReportDocumentRepository(ReportDocumentRepository reportDocumentRepository) {
        this.reportDocumentRepository = reportDocumentRepository;
    }

    /**
     * @return The {@link #reportRepository} field value.
     */
    public ReportRepository getReportRepository() {
        return reportRepository;
    }

    /**
     * @param reportRepository see the {@link #reportRepository} field description.
     */
    public void setReportRepository(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Create new instances of {@link #reportDocumentRepository} and {@link #reportDocumentCreatorRepository}.
     * @return the current instance of {@link ReportingContext}.
     */
    public ReportingContext init() {
        this.reportDocumentCreatorRepository = new ReportDocumentCreatorRepository();
        this.reportDocumentRepository = new ReportDocumentRepository();
        this.reportRepository = new ReportRepository();
        return this;
    }
}
