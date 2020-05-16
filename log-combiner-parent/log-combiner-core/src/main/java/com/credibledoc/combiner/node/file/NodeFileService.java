package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.context.Context;
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

    public NodeFile createNodeFile(Date date, File file, Context context) {
        NodeFile nodeFile = new NodeFile();
        nodeFile.setFile(file);
        nodeFile.setDate(date);
        context.getNodeFileRepository().getNodeFiles().add(nodeFile);
        return nodeFile;
    }

    /**
     * Find out {@link NodeFile} with the same {@link LogBufferedReader} file.
     *
     * @param logBufferedReader from {@link NodeLog}
     * @param context the current state
     * @return found {@link NodeFile}
     */
    public NodeFile findNodeFile(LogBufferedReader logBufferedReader, Context context) {
        LogInputStreamReader logInputStreamReader = (LogInputStreamReader) logBufferedReader.getReader();
        LogConcatenatedInputStream logConcatenatedInputStream = (LogConcatenatedInputStream) logInputStreamReader.getInputStream();
        LogFileInputStream logFileInputStream = logConcatenatedInputStream.getCurrentStream();
        for (NodeFile nodeFile : context.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getFile() == logFileInputStream.getFile()) {
                return nodeFile;
            }
        }
        throw new CombinerRuntimeException("Cannot find out NodeFile");
    }

    private void createOrAddToNodeFile(Tactic tactic, Set<NodeLog> nodeLogs, Date date,
                                       File file, Context context) {
        String folderName = file.getParentFile().getName();
        boolean nodeLogFound = false;
        for (NodeLog nodeLog : nodeLogs) {
            if (nodeLog.getName().equals(folderName)) {
                Set<NodeFile> nodeFiles = findNodeFiles(nodeLog, context);
                if (!containsName(nodeFiles, file.getName())) {
                    NodeFile nodeFile = createNodeFile(date, file, context);
                    nodeFiles.add(nodeFile);
                    nodeFile.setNodeLog(nodeLog);
                }
                nodeLogFound = true;
            }
        }
        if (!nodeLogFound) {
            NodeFile nodeFile = createNodeFile(date, file, context);
            NodeLog nodeLog = NodeLogService.getInstance().createNodeLog(nodeFile.getFile(), context);
            nodeLog.setTactic(tactic);
            nodeLogs.add(nodeLog);
            nodeFile.setNodeLog(nodeLog);
        }
    }

    public void appendToNodeLogs(File file, Date date, Tactic tactic, Context context) {
        Set<NodeLog> nodeLogs = NodeLogService.getInstance().findNodeLogs(tactic, context);
        createOrAddToNodeFile(tactic, nodeLogs, date, file, context);
    }

    public SortedSet<NodeFile> findNodeFiles(NodeLog nodeLog, Context context) {
        Comparator<NodeFile> comparator = NodeFileComparator.getInstance();
        TreeSet<NodeFile> treeSet = new TreeSet<>(comparator);
        SortedSet<NodeFile> result = Collections.synchronizedSortedSet(treeSet);
        for (NodeFile nodeFile : context.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getNodeLog() == nodeLog) {
                result.add(nodeFile);
            }
        }
        return result;
    }

    private boolean containsName(Set<NodeFile> nodeFiles, String name) {
        for (NodeFile nodeFile : nodeFiles) {
            if (nodeFile.getFile().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
