package com.credibledoc.combiner.tactic;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful singleton. Contains a list of {@link Tactic}s.
 *
 * @author Kyrylo Semenko
 */
public class TacticRepository {

    /**
     * Singleton.
     */
    private static TacticRepository instance;

    /**
     * @return The {@link TacticRepository} singleton.
     */
    public static TacticRepository getInstance() {
        if (instance == null) {
            instance = new TacticRepository();
        }
        return instance;
    }

    /**
     * {@link Tactic}s for parsing different log formats
     */
    private List<Tactic> tactics = new ArrayList<>();

    /**
     * @return the {@link #tactics} value
     */
    public List<Tactic> getTactics() {
        return tactics;
    }

    /**
     * @param tactics see the {@link #tactics} field
     */
    public void setTactics(List<Tactic> tactics) {
        this.tactics = tactics;
    }
}
