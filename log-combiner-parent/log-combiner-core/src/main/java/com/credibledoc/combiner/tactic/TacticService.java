package com.credibledoc.combiner.tactic;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;

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

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param line the line from the log file
     * @param logBufferedReader the {@link LogBufferedReader} read the line
     * @return {@link Tactic} or 'null' if not found
     */
    public Tactic findTactic(String line, LogBufferedReader logBufferedReader) {
        TacticService tacticService = getInstance();
        for (Tactic tactic : tacticService.getTactics()) {
            if (tactic.identifyApplication(line, logBufferedReader)) {
                return tactic;
            }
        }
        return null;
    }
}
