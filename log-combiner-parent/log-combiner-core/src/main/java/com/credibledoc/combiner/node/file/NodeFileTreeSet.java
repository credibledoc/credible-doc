package com.credibledoc.combiner.node.file;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.tactic.Tactic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Ordered set with {@link #tacticMap} for a better performance.
 * @param <E> The {@link NodeFile}
 */
public class NodeFileTreeSet<E> extends TreeSet<E> {
    private final transient Map<Tactic, TreeSet<NodeFile>> tacticMap = new HashMap<>();

    @Override
    public boolean add(E element) {
        if (!(element instanceof NodeFile)) {
            throw new CombinerRuntimeException("Expected " + NodeFile.class.getCanonicalName() + " " +
                "but found " + element.getClass().getCanonicalName());
        }
        NodeFile nodeFile = (NodeFile) element;
        if (nodeFile.getNodeLog() == null) {
            throw new CombinerRuntimeException(NodeLog.class.getSimpleName() + " field cannot be null");
        }
        Tactic tactic = nodeFile.getNodeLog().getTactic();
        if (tactic == null) {
            throw new CombinerRuntimeException(NodeLog.class.getSimpleName() + " " + Tactic.class.getSimpleName() + " field cannot be null");
        }
        TreeSet<NodeFile> set = tacticMap.get(tactic);
        if (set == null) {
            set = new TreeSet<>();
            set.add(nodeFile);
            tacticMap.put(tactic, set);
            return super.add(element);
        }
        if (set.contains(nodeFile)) {
            return false;
        }
        set.add(nodeFile);
        
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E object : collection) {
            add(object);
        }
        return true;
    }

    @Override
    public void clear() {
        super.clear();
        for (Map.Entry<Tactic, TreeSet<NodeFile>> entry : tacticMap.entrySet()) {
            entry.getValue().clear();
        }
        tacticMap.clear();
    }

    @Override
    public boolean remove(Object element) {
        if (!(element instanceof NodeFile)) {
            throw new CombinerRuntimeException("Expected " + NodeFile.class.getCanonicalName() + " " +
                "but found " + element.getClass().getCanonicalName());
        }
        NodeFile nodeFile = (NodeFile) element;

        for (Map.Entry<Tactic, TreeSet<NodeFile>> entry : tacticMap.entrySet()) {
            entry.getValue().remove(nodeFile);
        }
        return super.remove(element);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeFileTreeSet)) return false;
        if (!super.equals(o)) return false;
        NodeFileTreeSet<?> that = (NodeFileTreeSet<?>) o;
        return tacticMap.equals(that.tacticMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tacticMap);
    }

    public TreeSet<NodeFile> get(Tactic tactic) {
        return tacticMap.get(tactic);
    }
}
