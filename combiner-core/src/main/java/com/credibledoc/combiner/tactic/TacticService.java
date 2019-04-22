package com.credibledoc.combiner.tactic;

import java.util.List;

/**
 * Service for working with {@link Tactic}.
 *
 * @author Kyrylo Semenko
 */
public class TacticService {

    /**
     * Singleton.
     */
    private static TacticService instance;

    /**
     * @return The {@link TacticService} singleton.
     */
    public static TacticService getInstance() {
        if (instance == null) {
            instance = new TacticService();
        }
        return instance;
    }

    /**
     * @return All {@link Tactic} instances from the {@link TacticRepository}.
     */
    public List<Tactic> getTactics() {
        return TacticRepository.getInstance().getTactics();
    }
}
