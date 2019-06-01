package com.credibledoc.combiner.node.applicationlog;

import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.tactic.Tactic;

/**
 * <p>
 * This data object represents a list of log files from the concrete {@link Tactic}.
 *
 * <p>
 * These files will be parsed and visualized.
 *
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - zrusit tuto domenu
public class ApplicationLog {

    /**
     * The {@link Tactic} the log {@link NodeLog}s belong to
     */
    private com.credibledoc.combiner.tactic.Tactic Tactic;

    @Override
    public String toString() {
        return "ApplicationLog{" + "Tactic=\"" + Tactic + "\"}";
    }

    /**
     * An empty constructor
     */
    public ApplicationLog() {
        // empty
    }

    /**
     * @return The {@link #Tactic} field value.
     */
    public Tactic getTactic() {
        return Tactic;
    }

    /**
     * @param Tactic see the {@link #Tactic} field description.
     */
    public void setTactic(Tactic Tactic) {
        this.Tactic = Tactic;
    }
}
