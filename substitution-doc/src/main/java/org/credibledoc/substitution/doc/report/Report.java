package org.credibledoc.substitution.doc.report;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a global state of generated reports. It contains for example
 * {@link #directory}, {@link #lastUsedNodeLogIndex}, {@link #linesNumber}
 * and {@link #transactionsFilter}.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
     * Contains an order number of the last used
     * {@link org.credibledoc.substitution.doc.filesmerger.node.log.NodeLog#getLogBufferedReader()}
     */
    private int lastUsedNodeLogIndex;

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
     * @return The {@link #lastUsedNodeLogIndex} field value.
     */
    public int getLastUsedNodeLogIndex() {
        return lastUsedNodeLogIndex;
    }

    /**
     * @param lastUsedNodeLogIndex see the {@link #lastUsedNodeLogIndex} field
     */
    public void setLastUsedNodeLogIndex(int lastUsedNodeLogIndex) {
        this.lastUsedNodeLogIndex = lastUsedNodeLogIndex;
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
