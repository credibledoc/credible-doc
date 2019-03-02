package com.credibledoc.substitution.placeholder;

import java.util.List;

/**
 * This service contains methods for working with {@link Placeholder}s.
 *
 * @author Kyrylo Semenko
 */
public class PlaceholderService {

    /**
     * Singleton.
     */
    private static PlaceholderService instance;

    private PlaceholderService() {
        // empty
    }

    /**
     * @return The {@link PlaceholderService} singleton.
     */
    public static PlaceholderService getInstance() {
        if (instance == null) {
            instance = new PlaceholderService();
        }
        return instance;
    }

    /**
     * Cal the {@link PlaceholderRepository#getPlaceholders()} method.
     * @return list of all {@link Placeholder}s.
     */
    public List<Placeholder> getPlaceholders() {
        return PlaceholderRepository.getInstance().getPlaceholders();
    }
}
