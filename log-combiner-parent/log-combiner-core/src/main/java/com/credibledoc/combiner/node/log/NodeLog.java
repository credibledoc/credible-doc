package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.file.NodeFile;

/**
 * Each application contains one or more nodes.
 * Each node contains one or more {@link NodeFile}s.
 * This object represents node log files.
 * It contains {@link #logBufferedReader} as a source of lines for parsing.
 */
public class NodeLog {
    /**
     * The node name, the same as {@link NodeFile}s folder name.
     */
    private String name;

    /**
     * Contains concatenated {@link java.io.FileInputStream}s
     * of all {@link NodeFile}s.
     */
    private LogBufferedReader logBufferedReader;

    /**
     * The {@link ApplicationLog} this {@link NodeLog} belongs to.
     */
    private ApplicationLog applicationLog;

    /**
     * An empty constructor.
     */
    public NodeLog() {
        // empty
    }

    /**
     * @return the {@link #name} value
     */
    public String getName() {
        return name;
    }

    /**
     * @param name see the {@link #name} field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the {@link #logBufferedReader} value
     */
    public LogBufferedReader getLogBufferedReader() {
        return logBufferedReader;
    }

    /**
     * @param logBufferedReader see the {@link #logBufferedReader} field
     */
    public void setLogBufferedReader(LogBufferedReader logBufferedReader) {
        this.logBufferedReader = logBufferedReader;
    }

    /**
     * @return the {@link #applicationLog} value
     */
    public ApplicationLog getApplicationLog() {
        return applicationLog;
    }

    /**
     * @param applicationLog see the {@link #applicationLog} field
     */
    public void setApplicationLog(ApplicationLog applicationLog) {
        this.applicationLog = applicationLog;
    }
}
