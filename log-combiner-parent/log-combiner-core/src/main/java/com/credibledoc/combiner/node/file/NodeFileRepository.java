package com.credibledoc.combiner.node.file;

/**
 * A stateful bean. Contains {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
public class NodeFileRepository {

    /**
     * Contains files for parsing.
     */
    private NodeFileTreeSet<NodeFile> nodeFiles = new NodeFileTreeSet<>();

    /**
     * @return the {@link #nodeFiles} value
     */
    public NodeFileTreeSet<NodeFile> getNodeFiles() {
        return nodeFiles;
    }

    /**
     * @param nodeFiles see the {@link #nodeFiles} field
     */
    public void setNodeFiles(NodeFileTreeSet<NodeFile> nodeFiles) {
        this.nodeFiles = nodeFiles;
    }
}
