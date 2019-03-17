package com.credibledoc.substitution.core.content;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateful singleton. Contains the {@link #contentGeneratorMap}.
 *
 * @author Kyrylo Semenko
 */
public class ContentGeneratorRepository {

    /**
     * Singleton.
     */
    private static ContentGeneratorRepository instance;

    /**
     * Empty constructor.
     */
    private ContentGeneratorRepository() {
        // empty
    }

    /**
     * @return The {@link ContentGeneratorRepository} singleton.
     */
    public static ContentGeneratorRepository getInstance() {
        if (instance == null) {
            instance = new ContentGeneratorRepository();
        }
        return instance;
    }

    /**
     * This map contains entries where a key is a {@link ContentGenerator} an implementation class
     * and value is the key instance.
     */
    private Map<Class<? extends ContentGenerator>, ContentGenerator> contentGeneratorMap = new HashMap<>();

    /**
     * @return The {@link #contentGeneratorMap} field value.
     */
    public Map<Class<? extends ContentGenerator>, ContentGenerator> getContentGeneratorMap() {
        return contentGeneratorMap;
    }

    /**
     * @param contentGeneratorMap see the {@link #contentGeneratorMap} field description.
     */
    public void setContentGeneratorMap(Map<Class<? extends ContentGenerator>, ContentGenerator> contentGeneratorMap) {
        this.contentGeneratorMap = contentGeneratorMap;
    }
}
