package com.credibledoc.combiner.application.identifier;

import java.util.List;

/**
 * This service contains methods for working with {@link ApplicationIdentifier}s.
 *
 * @author Kyrylo Semenko
 */
public class ApplicationIdentifierService {

    /**
     * Singleton.
     */
    private static ApplicationIdentifierService instance;

    /**
     * @return The {@link ApplicationIdentifierService} singleton.
     */
    public static ApplicationIdentifierService getInstance() {
        if (instance == null) {
            instance = new ApplicationIdentifierService();
        }
        return instance;
    }

    /**
     * Add the {@link ApplicationIdentifier} to the {@link ApplicationIdentifierRepository}
     * @param applicationIdentifier for addition
     */
    public void addApplicationIdentifier(ApplicationIdentifier applicationIdentifier) {
        ApplicationIdentifierRepository.getInstance().getApplicationIdentifiers().add(applicationIdentifier);
    }

    /**
     * Call the {@link ApplicationIdentifierRepository#getApplicationIdentifiers()} method
     * @return All items from the {@link ApplicationIdentifierRepository}
     */
    public List<ApplicationIdentifier> getApplicationIdentifiers() {
        return ApplicationIdentifierRepository.getInstance().getApplicationIdentifiers();
    }
}
