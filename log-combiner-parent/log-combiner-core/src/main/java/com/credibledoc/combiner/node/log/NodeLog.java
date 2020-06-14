package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.tactic.Tactic;

import java.util.Objects;

/**
 * Each application contains one or more nodes.
 * Each node contains one or more {@link NodeFile}s.
 * This object represents a node log with files. It contains a {@link #name} and {@link #tactic}.
 */
public class NodeLog implements Comparable<NodeLog> {
    /**
     * The node name, the same as {@link NodeFile}s folder name.
     */
    private String name;

    /**
     * A {@link Tactic} this {@link NodeLog} belongs to.
     */
    private Tactic tactic;
    
    /**
     * An empty constructor.
     */
    public NodeLog() {
        // empty
    }

    @Override
    public int compareTo(NodeLog other) {
        if (this == other) {
            return 0;
        }
        int names = this.name.compareTo(other.name);
        if (names != 0) {
            return names;
        }
        return this.getTactic().getClass().getName().compareTo(other.getTactic().getClass().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeLog)) return false;
        NodeLog nodeLog = (NodeLog) o;
        return getName().equals(nodeLog.getName()) &&
            getTactic().equals(nodeLog.getTactic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getTactic());
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
     * @return The {@link #tactic} field value.
     */
    public Tactic getTactic() {
        return tactic;
    }

    /**
     * @param tactic see the {@link #tactic} field description.
     */
    public void setTactic(Tactic tactic) {
        this.tactic = tactic;
    }
}
