package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.file.FileWithSources;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.log.NodeLog;

import java.util.Date;
import java.util.Objects;

/**
 * Data object. Contains for example a {@link #fileWithSources} and {@link #date}.
 *
 * @author Kyrylo Semenko
 */
public class NodeFile implements Comparable<NodeFile> {

    /**
     * Log file for parsing.
     */
    private FileWithSources fileWithSources;

    /**
     * The first date record found in the {@link #fileWithSources}
     */
    private Date date;

    /**
     * The {@link NodeLog} this {@link NodeFile} belongs to.
     */
    private NodeLog nodeLog;

    /**
     * Contains {@link java.io.FileInputStream} of the {@link #fileWithSources}.
     */
    private LogBufferedReader logBufferedReader;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeFile)) return false;
        NodeFile nodeFile = (NodeFile) o;
        return getFileWithSources().equals(nodeFile.getFileWithSources()) &&
            getDate().equals(nodeFile.getDate()) &&
            getNodeLog().equals(nodeFile.getNodeLog());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileWithSources(), getDate(), getNodeLog());
    }

    @Override
    public int compareTo(NodeFile other) {
        if (other == null) {
            return 1;
        }
        if (this == other) {
            return 0;
        }
        return this.getFileWithSources().getFile().getAbsolutePath()
            .compareTo(other.getFileWithSources().getFile().getAbsolutePath());
    }

    /**
     * @return The {@link #fileWithSources} field value.
     */
    public FileWithSources getFileWithSources() {
        return fileWithSources;
    }

    /**
     * @param fileWithSources see the {@link #fileWithSources} field description.
     */
    public void setFileWithSources(FileWithSources fileWithSources) {
        this.fileWithSources = fileWithSources;
    }

    /**
     * @return the {@link #date} value
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date see the {@link #date} field
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the {@link #nodeLog} value
     */
    public NodeLog getNodeLog() {
        return nodeLog;
    }

    /**
     * @param nodeLog see the {@link #nodeLog} field
     */
    public void setNodeLog(NodeLog nodeLog) {
        this.nodeLog = nodeLog;
    }

    /**
     * @return The {@link #logBufferedReader} field value.
     */
    public LogBufferedReader getLogBufferedReader() {
        return logBufferedReader;
    }

    /**
     * @param logBufferedReader see the {@link #logBufferedReader} field description.
     */
    public void setLogBufferedReader(LogBufferedReader logBufferedReader) {
        this.logBufferedReader = logBufferedReader;
    }

}
