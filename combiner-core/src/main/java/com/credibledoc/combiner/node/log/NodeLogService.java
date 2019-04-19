package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public NodeLog createNodeLog(File nodeFileFile) {
        NodeLog nodeLog = new NodeLog();
        nodeLog.setName(nodeFileFile.getParentFile().getName());
        NodeLogRepository.getInstance().getNodeLogs().add(nodeLog);
        return nodeLog;
    }

    public List<NodeLog> findNodeLogs(ApplicationLog applicationLog) {
        List<NodeLog> result = new ArrayList<>();
        for (NodeLog nodeLog : NodeLogRepository.getInstance().getNodeLogs()) {
            if (nodeLog.getApplicationLog() == applicationLog) {
                result.add(nodeLog);
            }
        }
        return result;
    }

    /**
     * Find out {@link NodeLog} with the same {@link NodeLog#getLogBufferedReader()}
     * as the parameter.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return found {@link NodeLog}
     */
    private NodeLog findNodeLog(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : ApplicationLogService.getInstance().getApplicationLogs()) {
            for (NodeLog nodeLog : findNodeLogs(applicationLog)) {
                if (logBufferedReader == nodeLog.getLogBufferedReader()) {
                    return nodeLog;
                }
            }
        }
        throw new CombinerRuntimeException("NodeLog cannot be 'null'");
    }

    /**
     * Call the {@link NodeLogService#findNodeLog(LogBufferedReader)} method
     * and return {@link NodeLog#getName()}.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return {@link NodeLog#getName()}
     */
    public String findNodeName(LogBufferedReader logBufferedReader) {
        return findNodeLog(logBufferedReader).getName();
    }
}
