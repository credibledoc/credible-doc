package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileWithSources;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Contains methods for working with {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeFileService {

    /**
     * Singleton.
     */
    private static final NodeFileService instance = new NodeFileService();

    /**
     * @return The {@link NodeFileService} singleton.
     */
    public static NodeFileService getInstance() {
        return instance;
    }

    public NodeFile createNodeFile(Date date, FileWithSources fileWithSources, CombinerContext combinerContext, NodeLog nodeLog) {
        NodeFile nodeFile = new NodeFile();
        nodeFile.setFileWithSources(fileWithSources);
        nodeFile.setDate(date);
        nodeFile.setNodeLog(nodeLog);
        combinerContext.getNodeFileRepository().getNodeFiles().add(nodeFile);
        return nodeFile;
    }

    /**
     * Find out {@link NodeFile} with the same {@link LogBufferedReader} file.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @param combinerContext the current state
     * @return found {@link NodeFile}
     */
    public NodeFile findNodeFile(LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        LogInputStreamReader logInputStreamReader = (LogInputStreamReader) logBufferedReader.getReader();
        LogConcatenatedInputStream logConcatenatedInputStream = (LogConcatenatedInputStream) logInputStreamReader.getInputStream();
        LogFileInputStream logFileInputStream = logConcatenatedInputStream.getCurrentStream();
        for (NodeFile nodeFile : combinerContext.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getFileWithSources().getFile() == logFileInputStream.getFile()) {
                return nodeFile;
            }
        }
        throw new CombinerRuntimeException("Cannot find NodeFile");
    }

    private void createOrAddToNodeFile(Tactic tactic, Set<NodeLog> nodeLogs, Date date,
                                       FileWithSources fileWithSources, CombinerContext combinerContext) {
        String folderName = fileWithSources.getFile().getParentFile().getName();
        boolean nodeLogFound = false;
        for (NodeLog nodeLog : nodeLogs) {
            if (nodeLog.getName().equals(folderName)) {
                Set<NodeFile> nodeFiles = findNodeFiles(nodeLog, combinerContext);
                if (!containsName(nodeFiles, fileWithSources.getFile().getName())) {
                    NodeFile nodeFile = createNodeFile(date, fileWithSources, combinerContext, nodeLog);
                    nodeFiles.add(nodeFile);
                    nodeFile.setNodeLog(nodeLog);
                }
                nodeLogFound = true;
            }
        }
        if (!nodeLogFound) {
            nodeLogs = new TreeSet<>();
            NodeLog nodeLog = NodeLogService.getInstance().createNodeLog(fileWithSources, combinerContext, tactic);
            NodeFile nodeFile = createNodeFile(date, fileWithSources, combinerContext, nodeLog);
            nodeLogs.add(nodeLog);
            nodeFile.setNodeLog(nodeLog);
        }
    }

    public void appendToNodeLogs(FileWithSources fileWithSources, Date date, Tactic tactic, CombinerContext combinerContext) {
        Set<NodeLog> nodeLogs = NodeLogService.getInstance().findNodeLogs(tactic, combinerContext);
        createOrAddToNodeFile(tactic, nodeLogs, date, fileWithSources, combinerContext);
    }

    public SortedSet<NodeFile> findNodeFiles(NodeLog nodeLog, CombinerContext combinerContext) {
        Comparator<NodeFile> comparator = NodeFileComparator.getInstance();
        TreeSet<NodeFile> treeSet = new TreeSet<>(comparator);
        SortedSet<NodeFile> result = Collections.synchronizedSortedSet(treeSet);
        for (NodeFile nodeFile : combinerContext.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getNodeLog() == nodeLog) {
                result.add(nodeFile);
            }
        }
        return result;
    }

    private boolean containsName(Set<NodeFile> nodeFiles, String name) {
        for (NodeFile nodeFile : nodeFiles) {
            if (nodeFile.getFileWithSources().getFile().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
