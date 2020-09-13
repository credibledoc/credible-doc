package com.credibledoc.combiner.node.log;

/**
 * A stateful object. Contains a list of {@link NodeLog}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeLogRepository {

    /**
     * The set of {@link NodeLog}s with files for parsing.
     */
    private final NodeLogTreeSet<NodeLog> nodeLogs = new NodeLogTreeSet<>();

    /**
     * @return the {@link #nodeLogs} value
     */
    public NodeLogTreeSet<NodeLog> getNodeLogs() {
        return nodeLogs;
    }

}
