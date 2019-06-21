package com.credibledoc.substitution.reporting.report.document;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.substitution.reporting.report.Report;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocumentType;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Contains a state of a single generated report document.
 *
 * @author Kyrylo Semenko
 */
public class Document implements ReportDocument {

    /**
     * {@link Writer} of the report document
     */
    private PrintWriter printWriter;

    /**
     * One report may have more files, for example application.0.log.expanded, application.1.log.expanded and so on.
     */
    private int fileNumber;

    /**
     * This method will be called as the last method before closing of a {@link Document} file
     */
    private Consumer<ReportDocument> footerMethod;

    /**
     * Transformed lines prepared to print out, for example PlantUml lines
     */
    private List<String> cacheLines;

    /**
     * A {@link ReportDocumentType} of this {@link Document}
     */
    private Class<? extends ReportDocumentType> reportDocumentType;

    /**
     * Contains {@link NodeFile}s, from which this {@link Document} obtain the data.
     */
    private Set<NodeFile> nodeFiles;

    /**
     * The {@link Report} this {@link Document} belongs to
     */
    private Report report;

    public Document() {
        fileNumber = 1;
        cacheLines = new ArrayList<>();
        nodeFiles = new LinkedHashSet<>();
    }

    @Override
    public String toString() {
        return "ReportDocument{" +
            ", fileNumber=" + fileNumber +
            ", footerMethod=" + footerMethod +
            ", cacheLines=" + cacheLines +
            ", reportDocumentType=" + reportDocumentType +
            ", nodeFiles=" + nodeFiles +
            ", report=" + report +
            '}';
    }

    @Override
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    /**
     * @param printWriter see the {@link Document#printWriter} field
     */
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    /**
     * @return The {@link Document#footerMethod} field
     */
    public Consumer<ReportDocument> getFooterMethod() {
        return footerMethod;
    }

    /**
     * @param footerMethod see the {@link Document#footerMethod} field
     */
    public void setFooterMethod(Consumer<ReportDocument> footerMethod) {
        this.footerMethod = footerMethod;
    }

    @Override
    public List<String> getCacheLines() {
        return cacheLines;
    }

    /**
     * @param cacheLines see the {@link Document#cacheLines} field
     */
    public void setCacheLines(List<String> cacheLines) {
        this.cacheLines = cacheLines;
    }

    /**
     * @return The {@link #reportDocumentType} field value.
     */
    public Class<? extends ReportDocumentType> getReportDocumentType() {
        return reportDocumentType;
    }

    /**
     * @param reportDocumentType see the {@link #reportDocumentType} field description.
     */
    public void setReportDocumentType(Class<? extends ReportDocumentType> reportDocumentType) {
        this.reportDocumentType = reportDocumentType;
    }

    /**
     * @return The {@link #nodeFiles} field value.
     */
    public Set<NodeFile> getNodeFiles() {
        return nodeFiles;
    }

    /**
     * @param nodeFiles see the {@link #nodeFiles} field
     */
    public void setNodeFiles(Set<NodeFile> nodeFiles) {
        this.nodeFiles = nodeFiles;
    }

    /**
     * @return The {@link #report} field value.
     */
    public Report getReport() {
        return report;
    }

    /**
     * @param report see the {@link #report} field description.
     */
    public void setReport(Report report) {
        this.report = report;
    }
}
