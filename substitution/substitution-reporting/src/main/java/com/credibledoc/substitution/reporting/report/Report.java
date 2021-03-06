package com.credibledoc.substitution.reporting.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a global state of generated reports. It contains for example
 * {@link #directory}, {@link #linesNumber} and {@link #transactionsFilter}.
 */
public class Report {
    /**
     * Where all reports will be placed.
     */
    private File directory;

    /**
     * If this list is empty, all transactions will be parsed.
     * Else only defined in the list transactions will be parsed.
     */
    private List<String> transactionsFilter;

    /**
     * A total number of lines in all log files.
     */
    private int linesNumber;

    /**
     * When this field is 'true', then a special behavior will be applied.
     * Visualizer will create its own documentation.
     */
    private boolean creationOfSelfDocumentation;

    /**
     * Initializes all lists.
     */
    public Report() {
        transactionsFilter = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Report{" +
            "directory=" + directory +
            ", transactionsFilter=" + transactionsFilter +
            ", linesNumber=" + linesNumber +
            ", creationOfSelfDocumentation=" + creationOfSelfDocumentation +
            '}';
    }

    /**
     * @return The {@link #directory} field value.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @param directory see the {@link #directory} field
     */
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    /**
     * @return The {@link #transactionsFilter} field value.
     */
    public List<String> getTransactionsFilter() {
        return transactionsFilter;
    }

    /**
     * @param transactionsFilter see the {@link #transactionsFilter} field
     */
    public void setTransactionsFilter(List<String> transactionsFilter) {
        this.transactionsFilter = transactionsFilter;
    }

    /**
     * @return The {@link #linesNumber} field value.
     */
    public int getLinesNumber() {
        return linesNumber;
    }

    /**
     * @param linesNumber see the {@link #linesNumber} field
     */
    public void setLinesNumber(int linesNumber) {
        this.linesNumber = linesNumber;
    }

    /**
     * @return The {@link #creationOfSelfDocumentation} field value.
     */
    public boolean isCreationOfSelfDocumentation() {
        return creationOfSelfDocumentation;
    }

    /**
     * @param creationOfSelfDocumentation see the {@link #creationOfSelfDocumentation} field
     */
    public void setCreationOfSelfDocumentation(boolean creationOfSelfDocumentation) {
        this.creationOfSelfDocumentation = creationOfSelfDocumentation;
    }
}
