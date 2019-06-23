package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.node.log.NodeLog;

import java.io.File;
import java.util.Date;

/**
 * Data object. Contains for example a {@link #file} and {@link #date}.
 *
 * @author Kyrylo Semenko
 */
public class NodeFile {

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

}
