package com.credibledoc.combiner.node.applicationlog;

import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.log.NodeLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This service contains methods for working with {@link ApplicationLog}s.
 *
 * @author Kyrylo Semenko
 */
public class ApplicationLogService {

    /**
     * Singleton.
     */
    private static ApplicationLogService instance;

    /**
     * @return The {@link ApplicationLogService} singleton.
     */
    public static ApplicationLogService getInstance() {
        if (instance == null) {
            instance = new ApplicationLogService();
        }
        return instance;
    }

    /**
     * Collect all {@link ApplicationLog}s of {@link NodeFile}s
     * from argument.
     *
     * @param nodeFiles each {@link NodeFile} contains {@link NodeFile#getNodeLog()}
     *                 with {@link NodeLog#getApplicationLog()}.
     * @return all {@link ApplicationLog}s which belongs to the
     * {@link NodeFile}s.
     */
    public List<ApplicationLog> getApplicationLogs(Collection<NodeFile> nodeFiles) {
        List<ApplicationLog> result = new ArrayList<>();
        for (NodeFile nodeFile : nodeFiles) {
            ApplicationLog applicationLog = nodeFile.getNodeLog().getApplicationLog();
            if (!result.contains(applicationLog)) {
                result.add(applicationLog);
            }
        }
        return result;
    }

    /**
     * Add the {@link ApplicationLog} to the {@link ApplicationLogRepository}
     * @param applicationLog for addition
     */
    public void addApplicationLog(ApplicationLog applicationLog) {
        ApplicationLogRepository.getInstance().getApplicationLogs().add(applicationLog);
    }

    /**
     * Call the {@link ApplicationLogRepository#getApplicationLogs()} method
     * @return All items from the {@link ApplicationLogRepository}
     */
    public List<ApplicationLog> getApplicationLogs() {
        return ApplicationLogRepository.getInstance().getApplicationLogs();
    }
}
