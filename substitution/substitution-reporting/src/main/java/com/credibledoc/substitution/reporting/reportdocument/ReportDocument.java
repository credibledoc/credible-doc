package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.substitution.reporting.report.Report;

import java.io.File;
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
public class ReportDocument implements Deriving {

    /**
     * {@link Writer} of the report document
     */
    private PrintWriter printWriter;

    /**
     * One report may have more files, for example application.0.log.expanded, application.1.log.expanded and so on.
     */
    private int fileNumber;

    /**
     * Root directory of all reports. It placed next to the source file(s)
     */
    private File reportDirectory;

    /**
     * File extension without dot, for example 'html'
     */
    private String fileExtension;

    /**
     * This method will be called as the first method after creation of each report file
     */
    private Consumer<ReportDocument> headerMethod;

    /**
     * This method will be called as the last method before closing of a {@link ReportDocument} file
     */
    private Consumer<ReportDocument> footerMethod;

    /**
     * Transformed lines prepared to print out, for example PlantUml lines
     */
    private List<String> cacheLines;

    /**
     * A link to another file, for example
     * 'c:\temp\application.0.log.report\public\application.0.log.expanded.html'
     */
    private String linkResource;

    /**
     * A {@link ReportDocumentType} of this {@link ReportDocument}
     */
    private Class<? extends ReportDocumentType> reportDocumentType;

    /**
     * Contains {@link NodeFile}s, from which this {@link ReportDocument} obtain the data.
     */
    private Set<NodeFile> nodeFiles;

    /**
     * This {@link ReportDocument} file.
     */
    private File file;

    /**
     * The {@link Report} this {@link ReportDocument} belongs to
     */
    private Report report;

    public ReportDocument() {
        fileNumber = 1;
        cacheLines = new ArrayList<>();
        nodeFiles = new LinkedHashSet<>();
    }

    @Override
    public String toString() {
        return "ReportDocument{" +
            ", fileNumber=" + fileNumber +
            ", reportDirectory=" + reportDirectory +
            ", fileExtension='" + fileExtension + '\'' +
            ", headerMethod=" + headerMethod +
            ", footerMethod=" + footerMethod +
            ", cacheLines=" + cacheLines +
            ", linkResource='" + linkResource + '\'' +
            ", reportDocumentType=" + reportDocumentType +
            ", nodeFiles=" + nodeFiles +
            ", file=" + file +
            ", report=" + report +
            '}';
    }

    @Override
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    /**
     * @param printWriter see the {@link ReportDocument#printWriter} field
     */
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    /**
     * @return The {@link ReportDocument#fileNumber} field
     */
    public int getFileNumber() {
        return fileNumber;
    }

    /**
     * @param fileNumber see the {@link ReportDocument#fileNumber} field
     */
    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    /**
     * @return The {@link ReportDocument#reportDirectory} field
     */
    public File getReportDirectory() {
        return reportDirectory;
    }

    /**
     * @param reportDirectory see the {@link ReportDocument#reportDirectory} field
     */
    public void setReportDirectory(File reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    /**
     * @return The {@link ReportDocument#fileExtension} field
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * @param fileExtension see the {@link ReportDocument#fileExtension} field
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * @return The {@link ReportDocument#headerMethod} field
     */
    public Consumer<ReportDocument> getHeaderMethod() {
        return headerMethod;
    }

    /**
     * @param headerMethod see the {@link ReportDocument#headerMethod} field
     */
    public void setHeaderMethod(Consumer<ReportDocument> headerMethod) {
        this.headerMethod = headerMethod;
    }

    /**
     * @return The {@link ReportDocument#footerMethod} field
     */
    public Consumer<ReportDocument> getFooterMethod() {
        return footerMethod;
    }

    /**
     * @param footerMethod see the {@link ReportDocument#footerMethod} field
     */
    public void setFooterMethod(Consumer<ReportDocument> footerMethod) {
        this.footerMethod = footerMethod;
    }

    @Override
    public List<String> getCacheLines() {
        return cacheLines;
    }

    /**
     * @param cacheLines see the {@link ReportDocument#cacheLines} field
     */
    public void setCacheLines(List<String> cacheLines) {
        this.cacheLines = cacheLines;
    }

    /**
     * @return The {@link ReportDocument#linkResource} field
     */
    public String getLinkResource() {
        return linkResource;
    }

    /**
     * @param linkResource see the {@link ReportDocument#linkResource} field
     */
    public void setLinkResource(String linkResource) {
        this.linkResource = linkResource;
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
     * @return The {@link #file} field value.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file see the {@link #file} field
     */
    public void setFile(File file) {
        this.file = file;
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
