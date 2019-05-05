package com.credibledoc.enricher.line;

import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import com.credibledoc.enricher.transformer.Transformer;

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
     * The {@link Deriving} this {@link LineProcessor}
     * belongs to.
     */
    private Deriving deriving;

    /**
     * Constructor sets the fields:
     * @param searchCommand {@link #searchCommand}
     * @param transformer {@link #transformer}
     * @param deriving {@link #deriving}
     */
    public LineProcessor(SearchCommand searchCommand, Transformer transformer, Deriving deriving) {
        this.searchCommand = searchCommand;
        this.setTransformer(transformer);
        this.deriving = deriving;
    }

    /**
     * @return The {@link LineProcessor#searchCommand} field
     */
    public SearchCommand getSearchCommand() {
        return searchCommand;
    }

    /**
     * See the {@link LineProcessor#searchCommand} field
     * @param searchCommand {@link #searchCommand}
     */
    public void setSearchCommand(SearchCommand searchCommand) {
        this.searchCommand = searchCommand;
    }

    /**
     * @return The {@link LineProcessor#transformer} field
     */
    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * @param transformer see the {@link LineProcessor#transformer} field
     */
    private void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * @return the {@link #deriving} value
     */
    public Deriving getDeriving() {
        return deriving;
    }

    /**
     * @param deriving see the {@link #deriving} field
     */
    public void setDeriving(Deriving deriving) {
        this.deriving = deriving;
    }
}
