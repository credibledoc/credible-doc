package com.credibledoc.combiner.tactic;

import com.credibledoc.combiner.exception.CombinerRuntimeException;

import java.util.ArrayList;
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
    public List<Tactic> getSpecificTactics() {
        return TacticRepository.getInstance().getTactics();
    }

    public Tactic findByClass(Class<? extends Tactic> specificTacticClass) {
        for (Tactic tactic : getSpecificTactics()) {
            if (specificTacticClass.isAssignableFrom(tactic.getClass())) {
                return tactic;
            }
        }
        List<Class> specificTacticClasses = new ArrayList<>();
        for (Tactic tactic : getSpecificTactics()) {
            specificTacticClasses.add(tactic.getClass());
        }
        throw new CombinerRuntimeException("Tactic instance of '" + specificTacticClass +
            "' class cannot be found in repository with classes: " + specificTacticClasses);
    }
}
