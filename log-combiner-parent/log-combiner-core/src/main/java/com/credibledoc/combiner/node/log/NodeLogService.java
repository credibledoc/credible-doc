package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.tactic.Tactic;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service for working with {@link NodeLog}.
 *
 * @author Kyrylo Semenko
 */
public class NodeLogService {

    /**
     * Singleton.
     */
    private static NodeLogService instance;

    /**
     * @return The {@link NodeLogService} singleton.
     */
    public static NodeLogService getInstance() {
        if (instance == null) {
            instance = new NodeLogService();
        }
        return instance;
    }

    /**
     * Create a new {@link NodeLog}
     * @param nodeFileFile the first item of the {@link NodeLog}
     * @param context the current state
     * @param tactic cannot be 'null'
     * @return created {@link NodeLog}
     */
    public NodeLog createNodeLog(File nodeFileFile, Context context, Tactic tactic) {
        NodeLog nodeLog = new NodeLog();
        nodeLog.setTactic(tactic);
        nodeLog.setName(nodeFileFile.getParentFile().getName());
        context.getNodeLogRepository().getNodeLogs().add(nodeLog);
        return nodeLog;
    }

    public Set<NodeLog> findNodeLogs(Tactic tactic, Context context) {
        NodeLogTreeSet<NodeLog> nodeLogs = context.getNodeLogRepository().getNodeLogs();
        TreeSet<NodeLog> treeSet = nodeLogs.get(tactic);
        if (treeSet == null) {
            return Collections.emptySet();
        }
        return treeSet;
    }

    /**
     * Find the {@link NodeLog} with the same {@link com.credibledoc.combiner.node.file.NodeFile#getLogBufferedReader()}.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @param context the current state
     * @return The found {@link NodeLog}
     */
    private NodeLog findNodeLog(LogBufferedReader logBufferedReader, Context context) {
        for (NodeFile nodeFile : context.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getLogBufferedReader() == logBufferedReader) {
                return nodeFile.getNodeLog();
            }
        }
        throw new CombinerRuntimeException("NodeLog cannot be 'null'");
    }

    /**
     * Call the {@link NodeLogService#findNodeLog(LogBufferedReader, Context)} method
     * and return {@link NodeLog#getName()}.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @param context the current state
     * @return {@link NodeLog#getName()}
     */
    public String findNodeName(LogBufferedReader logBufferedReader, Context context) {
        return findNodeLog(logBufferedReader, context).getName();
    }
}
