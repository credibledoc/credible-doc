package org.credibledoc.substitution.doc.filesmerger.node.file;

import org.credibledoc.substitution.doc.filesmerger.exception.FilesmergerRuntimeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogConcatenatedInputStream;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogFileInputStream;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogInputStreamReader;
import org.credibledoc.substitution.doc.filesmerger.node.applicationlog.ApplicationLog;
import org.credibledoc.substitution.doc.filesmerger.node.log.NodeLog;
import org.credibledoc.substitution.doc.filesmerger.node.log.NodeLogService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Contains methods for working with {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NodeFileService {

    @NonNull
    private NodeFileRepository nodeFileRepository;

    @NonNull
    private NodeLogService nodeLogService;

    public NodeFile createNodeFile(Date date, File file) {
        NodeFile nodeFile = new NodeFile();
        nodeFile.setFile(file);
        nodeFile.setDate(date);
        nodeFileRepository.getNodeFiles().add(nodeFile);
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
        for (NodeFile nodeFile : nodeFileRepository.getNodeFiles()) {
            if (nodeFile.getFile() == logFileInputStream.getFile()) {
                return nodeFile;
            }
        }
        throw new FilesmergerRuntimeException("Cannot find out NodeFile");
    }

    public List<NodeFile> getNodeFiles() {
        return nodeFileRepository.getNodeFiles();
    }

    /**
     * Iterate files and nodeLogs.
     * Find out {@link NodeLog} with the same name as a parent directory of a file
     * and append the file to the {@link NodeLog}.
     * @param dateFileMap log files and theirs dates ordered by date
     * @param applicationLog the {@link ApplicationLog} this {@link NodeLog} belongs to
     */
    public void appendToNodeLogs(Map<Date, File> dateFileMap, ApplicationLog applicationLog) {
        List<NodeLog> nodeLogs = nodeLogService.findNodeLogs(applicationLog);
        for (Map.Entry<Date, File> entry : dateFileMap.entrySet()) {
            Date date = entry.getKey();
            File file = entry.getValue();
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
                NodeLog nodeLog = nodeLogService.createNodeLog(nodeFile.getFile());
                nodeLog.setApplicationLog(applicationLog);
                nodeLogs.add(nodeLog);
                nodeFile.setNodeLog(nodeLog);
            }
        }
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
     * @param applicationLogs contains log files
     * @return 'false' if a single report is parsed
     */
    public boolean containsMoreThenOneSourceFiles(List<ApplicationLog> applicationLogs) {
        int filesNumber = 0;
        for (ApplicationLog applicationLog : applicationLogs) {
            for (NodeLog nodeLog : nodeLogService.findNodeLogs(applicationLog)) {
                filesNumber = filesNumber + findNodeFiles(nodeLog).size();
                if (filesNumber > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
