package org.credibledoc.substitution.doc.node.log;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLog;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for working with {@link NodeLog}.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NodeLogService {

    @NonNull
    private final NodeLogRepository nodeLogRepository;

    @NonNull
    private final ApplicationLogService applicationLogService;

    /**
     * Create a new {@link NodeLog}
     * @param nodeFileFile the first item of the {@link NodeLog}
     * @return created {@link NodeLog}
     */
    public NodeLog createNodeLog(File nodeFileFile) {
        NodeLog nodeLog = new NodeLog();
        nodeLog.setName(nodeFileFile.getParentFile().getName());
        nodeLogRepository.getNodeLogs().add(nodeLog);
        return nodeLog;
    }

    public List<NodeLog> findNodeLogs(ApplicationLog applicationLog) {
        List<NodeLog> result = new ArrayList<>();
        for (NodeLog nodeLog : nodeLogRepository.getNodeLogs()) {
            if (nodeLog.getApplicationLog() == applicationLog) {
                result.add(nodeLog);
            }
        }
        return result;
    }

    /**
     * Find out {@link NodeLog} with the same {@link NodeLog#getLogBufferedReader()}
     * as the second parameter.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return found {@link NodeLog}
     */
    public NodeLog findNodeLog(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : applicationLogService.getApplicationLogs()) {
            for (NodeLog nodeLog : findNodeLogs(applicationLog)) {
                if (logBufferedReader == nodeLog.getLogBufferedReader()) {
                    return nodeLog;
                }
            }
        }
        throw new SubstitutionRuntimeException("NodeLog cannot be 'null'");
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
