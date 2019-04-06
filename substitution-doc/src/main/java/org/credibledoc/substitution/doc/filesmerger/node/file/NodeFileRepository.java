package org.credibledoc.substitution.doc.filesmerger.node.file;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * A stateful bean. Contains {@link NodeFile}s.
 *
 * @author Kyrylo Semenko
 */
@Repository
public class NodeFileRepository {

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
