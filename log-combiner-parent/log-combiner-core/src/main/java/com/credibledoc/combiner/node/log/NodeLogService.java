package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.tactic.Tactic;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
     * @return created {@link NodeLog}
     */
    public NodeLog createNodeLog(File nodeFileFile, Context context) {
        NodeLog nodeLog = new NodeLog();
        nodeLog.setName(nodeFileFile.getParentFile().getName());
        context.getNodeLogRepository().getNodeLogs().add(nodeLog);
        return nodeLog;
    }

    public Set<NodeLog> findNodeLogs(Tactic tactic, Context context) {
        Set<NodeLog> result = new HashSet<>();
        for (NodeLog nodeLog : context.getNodeLogRepository().getNodeLogs()) {
            if (nodeLog.getTactic() == tactic) {
                result.add(nodeLog);
            }
        }
        return result;
    }

    /**
     * Find the {@link NodeLog} with the same {@link NodeLog#getLogBufferedReader()}.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return The found {@link NodeLog}
     */
    private NodeLog findNodeLog(LogBufferedReader logBufferedReader, Context context) {
        for (Tactic tactic : context.getTacticRepository().getTactics()) {
            for (NodeLog nodeLog : findNodeLogs(tactic, context)) {
                if (logBufferedReader == nodeLog.getLogBufferedReader()) {
                    return nodeLog;
                }
            }
        }
        throw new CombinerRuntimeException("NodeLog cannot be 'null'");
    }

    /**
     * Call the {@link NodeLogService#findNodeLog(LogBufferedReader, Context)} method
     * and return {@link NodeLog#getName()}.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return {@link NodeLog#getName()}
     */
    public String findNodeName(LogBufferedReader logBufferedReader, Context context) {
        return findNodeLog(logBufferedReader, context).getName();
    }
}
