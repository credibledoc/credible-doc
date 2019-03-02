package com.credibledoc.substitution.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This singleton provides services for working with {@link ContentGenerator}s.
 *
 * @author Kyrylo Semenko
 */
public class ContentGeneratorService {

    /**
     * This map contains entries where a key is a {@link ContentGenerator} subtype class and value is the key instance.
     */
    private Map<Class<? extends ContentGenerator>, ContentGenerator> contentGeneratorMap = new HashMap<>();

    /**
     * Singleton.
     */
    private static ContentGeneratorService instance;

    /**
     * Empty constructor.
     */
    private ContentGeneratorService() {
        // empty
    }

    /**
     * @return The {@link ContentGeneratorService} singleton.
     */
    public static ContentGeneratorService getInstance() {
        if (instance == null) {
            instance = new ContentGeneratorService();
        }
        return instance;
    }

    /**
     * Append {@link ContentGenerator}s from the argument to the {@link #contentGeneratorMap}.
     * @param contentGenerators implementations of the {@link ContentGenerator} interface.
     */
    public void addContentGenerators(Set<ContentGenerator> contentGenerators) {
        for (ContentGenerator contentGenerator : contentGenerators) {
            contentGeneratorMap.put(contentGenerator.getClass(), contentGenerator);
        }
    }
}
