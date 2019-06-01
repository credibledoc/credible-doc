package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;

import java.io.File;
import java.util.*;

/**
 * Contains methods for working with {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeFileService {

    /**
     * Singleton.
     */
    private static NodeFileService instance;

    /**
     * @return The {@link NodeFileService} singleton.
     */
    public static NodeFileService getInstance() {
        if (instance == null) {
            instance = new NodeFileService();
        }
        return instance;
    }

    public NodeFile createNodeFile(Date date, File file) {
        NodeFile nodeFile = new NodeFile();
        nodeFile.setFile(file);
        nodeFile.setDate(date);
        NodeFileRepository.getInstance().getNodeFiles().add(nodeFile);
        return nodeFile;
    }

    /**
     * Find out {@link NodeFile} with the same {@link LogBufferedReader} file.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @return found {@link NodeFile}
     */
    public NodeFile findNodeFile(LogBufferedReader logBufferedReader) {
        LogInputStreamReader logInputStreamReader = (LogInputStreamReader) logBufferedReader.getReader();
        LogConcatenatedInputStream logConcatenatedInputStream = (LogConcatenatedInputStream) logInputStreamReader.getInputStream();
        LogFileInputStream logFileInputStream = logConcatenatedInputStream.getCurrentStream();
        for (NodeFile nodeFile : NodeFileRepository.getInstance().getNodeFiles()) {
            if (nodeFile.getFile() == logFileInputStream.getFile()) {
                return nodeFile;
            }
        }
        throw new CombinerRuntimeException("Cannot find out NodeFile");
    }

    /**
     * Call the {@link NodeFileRepository#getNodeFiles()} method
     * @return All {@link NodeFile}s
     */
    public Set<NodeFile> getNodeFiles() {
        return NodeFileRepository.getInstance().getNodeFiles();
    }

    /**
     * Iterate files and nodeLogs.
     * Find out {@link NodeLog} with the same name as a parent directory of a file
     * and append the file to the {@link NodeLog}.
     * @param dateFileMap log files and theirs dates ordered by date
     * @param tactic the {@link Tactic} this {@link NodeLog} belongs to
     */
    public void appendToNodeLogs(Map<Date, File> dateFileMap, Tactic tactic) {
        List<NodeLog> nodeLogs = NodeLogService.getInstance().findNodeLogs(tactic);
        for (Map.Entry<Date, File> entry : dateFileMap.entrySet()) {
            Date date = entry.getKey();
            File file = entry.getValue();
            createOrAddToNodeFile(tactic, nodeLogs, date, file);
        }
    }

    private void createOrAddToNodeFile(Tactic tactic, List<NodeLog> nodeLogs, Date date,
                                       File file) {
        String folderName = file.getParentFile().getName();
        boolean nodeLogFound = false;
        for (NodeLog nodeLog : nodeLogs) {
            if (nodeLog.getName().equals(folderName)) {
                List<NodeFile> nodeFiles = findNodeFiles(nodeLog);
                if (!containsName(nodeFiles, file.getName())) {
                    NodeFile nodeFile = createNodeFile(date, file);
                    nodeFiles.add(nodeFile);
                    nodeFile.setNodeLog(nodeLog);
                }
                nodeLogFound = true;
            }
        }
        if (!nodeLogFound) {
            NodeFile nodeFile = createNodeFile(date, file);
            NodeLog nodeLog = NodeLogService.getInstance().createNodeLog(nodeFile.getFile());
            nodeLog.setTactic(tactic);
            nodeLogs.add(nodeLog);
            nodeFile.setNodeLog(nodeLog);
        }
    }

    public void appendToNodeLogs(File file, Date date, Tactic tactic) {
        List<NodeLog> nodeLogs = NodeLogService.getInstance().findNodeLogs(tactic);
        createOrAddToNodeFile(tactic, nodeLogs, date, file);
    }

    public List<NodeFile> findNodeFiles(NodeLog nodeLog) {
        List<NodeFile> result = new ArrayList<>();
        for (NodeFile nodeFile : getNodeFiles()) {
            if (nodeFile.getNodeLog() == nodeLog) {
                result.add(nodeFile);
            }
        }
        return result;
    }

    private boolean containsName(List<NodeFile> nodeFiles, String name) {
        for (NodeFile nodeFile : nodeFiles) {
            if (nodeFile.getFile().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether more than one log file is parsed for this report.
     * @param tactics contains log files
     * @return 'false' if a single report is parsed
     */
    public boolean containsMoreThenOneSourceFiles(List<Tactic> tactics) {
        int filesNumber = 0;
        for (Tactic tactic : tactics) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(tactic)) {
                filesNumber = filesNumber + findNodeFiles(nodeLog).size();
                if (filesNumber > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
