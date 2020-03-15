package com.credibledoc.combiner.node.log;

import java.util.HashSet;
import java.util.Set;

/**
 * A stateful object. Contains a list of {@link NodeLog}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeLogRepository {

    /**
     * The set of {@link NodeLog}s with files for parsing.
     */
    private Set<NodeLog> nodeLogs = new HashSet<>();

    /**
     * @return the {@link #nodeLogs} value
     */
    Set<NodeLog> getNodeLogs() {
        return nodeLogs;
    }

}
