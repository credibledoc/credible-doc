package com.credibledoc.substitution.doc.record;

import com.credibledoc.combiner.node.file.NodeFile;

import java.util.Date;
import java.util.List;

/**
 * This data object represents a log line. If a line has multiple rows,
 * all these rows will be represented here.
 *
 * @author Kyrylo Semenko
 */
public class Record {

    /**
     * A log record. It may have one or more rows.
     */
    private List<String> multiLine;

    /**
     * A date parsed from the {@link #multiLine} line.
     */
    private Date date;

    /**
     * The source of this {@link Record}.
     */
    private NodeFile nodeFile;

    public Record() {
        // Empty
    }

    public Record(List<String> multiLine, Date date) {
        this.multiLine = multiLine;
        this.date = date;
    }

    /**
     * @return the {@link #multiLine} value
     */
    public List<String> getMultiLine() {
        return multiLine;
    }

    /**
     * @param multiLine see the {@link #multiLine} field
     */
    public void setMultiLine(List<String> multiLine) {
        this.multiLine = multiLine;
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
     * @return the {@link #nodeFile} value
     */
    public NodeFile getNodeFile() {
        return nodeFile;
    }

    /**
     * @param nodeFile see the {@link #nodeFile} field
     */
    public void setNodeFile(NodeFile nodeFile) {
        this.nodeFile = nodeFile;
    }
}
