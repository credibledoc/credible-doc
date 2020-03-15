package com.credibledoc.combiner.tactic;

import java.util.HashSet;
import java.util.Set;

/**
 * Stateful singleton. Contains a set of {@link Tactic}s.
 *
 * @author Kyrylo Semenko
 */
public class TacticRepository {

    /**
     * {@link Tactic}s for parsing different log formats
     */
    private Set<Tactic> tactics = new HashSet<>();

    /**
     * @return The {@link #tactics} field value.
     */
    public Set<Tactic> getTactics() {
        return tactics;
    }

    /**
     * @param tactics see the {@link #tactics} field description.
     */
    public void setTactics(Set<Tactic> tactics) {
        this.tactics = tactics;
    }
}
