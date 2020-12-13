package com.credibledoc.combiner.node.file;

import java.util.Comparator;

/**
 * Compares two {@link NodeFile}s by its {@link NodeFile#getDate()}s.
 * 
 * @author Kyrylo Semenko
 */
public class NodeFileComparator implements Comparator<NodeFile> {
    /**
     * Singleton.
     */
    private static final NodeFileComparator instance = new NodeFileComparator();

    /**
     * @return The {@link NodeFileComparator} singleton.
     */
    public static NodeFileComparator getInstance() {
        return instance;
    }
    
    @Override
    public int compare(NodeFile left, NodeFile right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        if (left.getDate() == null && right.getDate() == null) {
            return 0;
        }
        if (left.getDate() == null) {
            return -1;
        }
        return left.getDate().compareTo(right.getDate());
    }
}
