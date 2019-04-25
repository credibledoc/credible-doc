package com.credibledoc.combiner.doc.transformer;

import com.credibledoc.combiner.doc.searchcommand.SearchCommand;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;

/**
 * Data object. Contains a {@link SearchCommand} used for searching
 * for a line in log files, and {@link Transformer}
 * for transformation of the line to another format.
 *
 * @author Kyrylo Semenko
 */
public class LineProcessor {
    
    /**
     * Defines how to find out a line which will be transformed
     */
    private SearchCommand searchCommand;
    
    /**
     * Defines how to transform the data
     */
    private Transformer transformer;

    /**
     * The {@link ReportDocument} this {@link LineProcessor}
     * belongs to.
     */
    private ReportDocument reportDocument;

    /**
     * Constructor sets the fields:
     * @param searchCommand {@link #searchCommand}
     * @param transformer {@link #transformer}
     */
    public LineProcessor(SearchCommand searchCommand, Transformer transformer, ReportDocument reportDocument) {
        this.searchCommand = searchCommand;
        this.setTransformer(transformer);
        this.reportDocument = reportDocument;
    }

    /**
     * @return The {@link LineProcessor#searchCommand} field
     */
    public SearchCommand getSearchCommand() {
        return searchCommand;
    }

    /** See the {@link LineProcessor#searchCommand} field<br> */
    public void setSearchCommand(SearchCommand searchCommand) {
        this.searchCommand = searchCommand;
    }

    /** @return The {@link LineProcessor#transformer} field */
    public Transformer getTransformer() {
        return transformer;
    }

    /** @param transformer see the {@link LineProcessor#transformer} field */
    private void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * @return the {@link #reportDocument} value
     */
    public ReportDocument getReportDocument() {
        return reportDocument;
    }

    /**
     * @param reportDocument see the {@link #reportDocument} field
     */
    public void setReportDocument(ReportDocument reportDocument) {
        this.reportDocument = reportDocument;
    }
}
