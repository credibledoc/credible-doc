package com.credibledoc.combiner.node.file;

import java.util.ArrayList;
import java.util.List;

/**
 * A stateful bean. Contains {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeFileRepository {
    // TODO Kyrylo Semenko - nesmi byt singleton

    /**
     * Singleton.
     */
    private static NodeFileRepository instance;

    /**
     * @return The {@link NodeFileRepository} singleton.
     */
    public static NodeFileRepository getInstance() {
        if (instance == null) {
            instance = new NodeFileRepository();
        }
        return instance;
    }

    private List<NodeFile> nodeFiles = new ArrayList<>();

    /**
     * @return the {@link #nodeFiles} value
     */
    public List<NodeFile> getNodeFiles() {
        return nodeFiles;
    }

    /**
     * @param nodeFiles see the {@link #nodeFiles} field
     */
    public void setNodeFiles(List<NodeFile> nodeFiles) {
        this.nodeFiles = nodeFiles;
    }
}
