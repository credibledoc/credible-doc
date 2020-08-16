package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.log.NodeLog;

import java.io.File;
import java.util.Date;
import java.util.Objects;

/**
 * Data object. Contains for example a {@link #file} and {@link #date}.
 *
 * @author Kyrylo Semenko
 */
public class NodeFile implements Comparable<NodeFile> {

    /**
     * Log file for parsing.
     */
    private File file;

    /**
     * The first date record found in the {@link #file}
     */
    private Date date;

    /**
     * The {@link NodeLog} this {@link NodeFile} belongs to.
     */
    private NodeLog nodeLog;

    /**
     * Contains {@link java.io.FileInputStream} of the {@link #file}.
     */
    private LogBufferedReader logBufferedReader;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeFile)) return false;
        NodeFile nodeFile = (NodeFile) o;
        return getFile().equals(nodeFile.getFile()) &&
            getDate().equals(nodeFile.getDate()) &&
            getNodeLog().equals(nodeFile.getNodeLog());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFile(), getDate(), getNodeLog());
    }

    @Override
    public int compareTo(NodeFile other) {
        if (other == null) {
            return 1;
        }
        if (this == other) {
            return 0;
        }
        return this.getFile().getAbsolutePath().compareTo(other.getFile().getAbsolutePath());
    }

    /**
     * @return the {@link #file} value
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
