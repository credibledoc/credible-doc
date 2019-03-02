package com.credibledoc.substitution.core.placeholder;

import com.credibledoc.substitution.core.configuration.ConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * This stateful object contains list of {@link Placeholder}s.
 *
 * @author Kyrylo Semenko
 */
public class PlaceholderRepository {

    /**
     * Singleton.
     */
    private static PlaceholderRepository instance;

    private PlaceholderRepository() {
        // empty
    }

    /**
     * @return The {@link PlaceholderRepository} singleton.
     */
    public static PlaceholderRepository getInstance() {
        if (instance == null) {
            instance = new PlaceholderRepository();
        }
        return instance;
    }

    /**
     * {@link Placeholder}s parsed from files in the
     * {@link ConfigurationService#TEMPLATES_RESOURCE}
     */
    private List<Placeholder> placeholders = new ArrayList<>();

    /**
     * @return The {@link #placeholders} field value.
     */
    public List<Placeholder> getPlaceholders() {
        return placeholders;
    }

    /**
     * @param placeholders see the {@link #placeholders} field
     */
    public void setPlaceholders(List<Placeholder> placeholders) {
        this.placeholders = placeholders;
    }
}
