package com.credibledoc.combiner.node.log;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.tactic.Tactic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Ordered set with {@link #tacticMap} for a better performance.
 * @param <E> The {@link NodeLog}
 */
public class NodeLogTreeSet<E> extends TreeSet<E> {
    private Map<Tactic, TreeSet<NodeLog>> tacticMap = new HashMap<>();

    @Override
    public boolean add(E element) {
        if (!(element instanceof NodeLog)) {
            throw new CombinerRuntimeException("Expected " + NodeLog.class.getCanonicalName() + " " +
                "but found " + element.getClass().getCanonicalName());
        }
        NodeLog nodeLog = (NodeLog) element;
        Tactic tactic = nodeLog.getTactic();
        if (tactic == null) {
            throw new CombinerRuntimeException(Tactic.class.getSimpleName() + " field cannot be null");
        }
        TreeSet<NodeLog> set = tacticMap.get(tactic);
        if (set == null) {
            set = new TreeSet<>();
            set.add(nodeLog);
            tacticMap.put(tactic, set);
            return super.add(element);
        }
        if (set.contains(nodeLog)) {
            return false;
        }
        set.add(nodeLog);
        
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E object : collection) {
            add(object);
        }
        return true;
    }

    public TreeSet<NodeLog> get(Tactic tactic) {
        return tacticMap.get(tactic);
    }
}
