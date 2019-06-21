package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.substitution.reporting.report.Report;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Contains a state of a single report document during its generation.
 * 
 * {@link ReportDocument} can be a representation of a markdown file, html file, UML diagram and so on.
 *
 * @author Kyrylo Semenko
 */
public interface ReportDocument extends Deriving {

    /**
     * @return A method for filling out this {@link ReportDocument}. This method will be called as the last method
     * before closing of {@link ReportDocument#getPrintWriter()} object.
     */
    Consumer<ReportDocument> getFooterMethod();

    /**
     * @return A {@link ReportDocumentType} this {@link ReportDocument} belongs to.
     */
    Class<? extends ReportDocumentType> getReportDocumentType();

    /**
     * @return Set of {@link NodeFile}s this {@link ReportDocument} obtain data from.
     */
    Set<NodeFile> getNodeFiles();

    /**
     * @return The {@link Report} this {@link ReportDocument} belongs to.
     */
    Report getReport();

    /**
     * @param report the {@link Report} instance this {@link ReportDocument} will be belonging to.
     */
    void setReport(Report report);

}
