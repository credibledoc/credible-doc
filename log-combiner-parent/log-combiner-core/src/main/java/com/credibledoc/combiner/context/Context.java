package com.credibledoc.combiner.context;

import com.credibledoc.combiner.node.file.NodeFileRepository;
import com.credibledoc.combiner.node.log.NodeLogRepository;
import com.credibledoc.combiner.tactic.TacticRepository;

/**
 * Contains instances of stateful objects (repositories) used in Combiner:
 * <ul>
 *     <li>{@link #nodeFileRepository}</li>
 *     <li>{@link #nodeLogRepository}</li>
 *     <li>{@link #tacticRepository}</li>
 * </ul>
 * 
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - rename to CombinerContext
public class Context {
    /**
     * Contains {@link com.credibledoc.combiner.node.file.NodeFile} instances.
     */
    private NodeFileRepository nodeFileRepository;

    /**
     * Contains {@link com.credibledoc.combiner.node.log.NodeLog} instances.
     */
    private NodeLogRepository nodeLogRepository;

    /**
     * Contains {@link com.credibledoc.combiner.tactic.Tactic} instances.
     */
    private TacticRepository tacticRepository;

    @Override
    public String toString() {
        return "Context{" +
            "nodeFileRepository=" + nodeFileRepository +
            ", nodeLogRepository=" + nodeLogRepository +
            ", tacticRepository=" + tacticRepository +
            '}';
    }

    /**
     * @return The {@link #nodeFileRepository} field value.
     */
    public NodeFileRepository getNodeFileRepository() {
        return nodeFileRepository;
    }

    /**
     * @param nodeFileRepository see the {@link #nodeFileRepository} field description.
     */
    public void setNodeFileRepository(NodeFileRepository nodeFileRepository) {
        this.nodeFileRepository = nodeFileRepository;
    }

    /**
     * @return The {@link #nodeLogRepository} field value.
     */
    public NodeLogRepository getNodeLogRepository() {
        return nodeLogRepository;
    }

    /**
     * @param nodeLogRepository see the {@link #nodeLogRepository} field description.
     */
    public void setNodeLogRepository(NodeLogRepository nodeLogRepository) {
        this.nodeLogRepository = nodeLogRepository;
    }

    /**
     * @return The {@link #tacticRepository} field value.
     */
    public TacticRepository getTacticRepository() {
        return tacticRepository;
    }

    /**
     * @param tacticRepository see the {@link #tacticRepository} field description.
     */
    public void setTacticRepository(TacticRepository tacticRepository) {
        this.tacticRepository = tacticRepository;
    }

    /**
     * Create new instances of {@link #nodeFileRepository}, {@link #nodeLogRepository}
     * and {@link #tacticRepository}.
     * @return the current instance of {@link Context}.
     */
    public Context init() {
        this.nodeFileRepository = new NodeFileRepository();
        this.nodeLogRepository = new NodeLogRepository();
        this.tacticRepository = new TacticRepository();
        return this;
    }
}
