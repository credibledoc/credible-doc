package org.credibledoc.substitution.doc.filesmerger.node.applicationlog;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.credibledoc.substitution.doc.module.tactic.TacticHolder;

/**
 * <p>
 * This data object represents a list of log files from the concrete {@link TacticHolder}.
 *
 * <p>
 * These files will be parsed and visualized.
 *
 * @author Kyrylo Semenko
 */
public class ApplicationLog {

    /**
     * The tacticHolder the log {@link org.credibledoc.substitution.doc.filesmerger.node.log.NodeLog}s belong to
     */
    private TacticHolder tacticHolder;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    /**
     * An empty constructor
     */
    public ApplicationLog() {
        // empty
    }

    /**
     * @return The {@link ApplicationLog#tacticHolder} field
     */
    public TacticHolder getTacticHolder() {
        return tacticHolder;
    }

    /**
     * @param tacticHolder see the {@link ApplicationLog#tacticHolder} field
     */
    public void setTacticHolder(TacticHolder tacticHolder) {
        this.tacticHolder = tacticHolder;
    }

}
