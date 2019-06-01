package com.credibledoc.combiner.node.file;

import java.util.HashSet;
import java.util.Set;

/**
 * A stateful bean. Contains {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeFileRepository {

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

    private Set<NodeFile> nodeFiles = new HashSet<>();

    /**
     * @return the {@link #nodeFiles} value
     */
    public Set<NodeFile> getNodeFiles() {
        return nodeFiles;
    }

    /**
     * @param nodeFiles see the {@link #nodeFiles} field
     */
    public void setNodeFiles(Set<NodeFile> nodeFiles) {
        this.nodeFiles = nodeFiles;
    }
}
