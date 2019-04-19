package com.credibledoc.combiner.application.identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful object. Contains a list of {@link ApplicationIdentifier}s.
 */
class ApplicationIdentifierRepository {
    // TODO Kyrylo Semenko - nesmi byt singleton

    /**
     * Singleton.
     */
    private static ApplicationIdentifierRepository instance;

    /**
     * @return The {@link ApplicationIdentifierRepository} singleton.
     */
    public static ApplicationIdentifierRepository getInstance() {
        if (instance == null) {
            instance = new ApplicationIdentifierRepository();
        }
        return instance;
    }

    /**
     * Contains a list of {@link ApplicationIdentifier}s
     */
    private List<ApplicationIdentifier> applicationIdentifiers = new ArrayList<>();

    /**
     * @return the {@link #applicationIdentifiers} value
     */
    List<ApplicationIdentifier> getApplicationIdentifiers() {
        return applicationIdentifiers;
    }

}
