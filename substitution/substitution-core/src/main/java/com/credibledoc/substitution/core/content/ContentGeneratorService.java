package com.credibledoc.substitution.core.content;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This singleton provides services for working with {@link ContentGenerator}s.
 *
 * @author Kyrylo Semenko
 */
public class ContentGeneratorService {

    /**
     * Singleton.
     */
    private static final ContentGeneratorService instance = new ContentGeneratorService();

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
        return instance;
    }

    /**
     * Append {@link ContentGenerator}s from the argument to the {@link ContentGeneratorRepository}.
     *
     * @param contentGenerators implementations of the {@link ContentGenerator} interface.
     */
    public void addContentGenerators(Collection<ContentGenerator> contentGenerators) {
        Map<Class<? extends ContentGenerator>, ContentGenerator> contentGeneratorMap = new HashMap<>();
        for (ContentGenerator contentGenerator : contentGenerators) {
            contentGeneratorMap.put(contentGenerator.getClass(), contentGenerator);
        }
        ContentGeneratorRepository.getInstance().getContentGeneratorMap().putAll(contentGeneratorMap);
    }

    /**
     * Find {@link ContentGenerator} in the {@link ContentGeneratorRepository}
     *
     * @param contentGeneratorClass type of the {@link ContentGenerator}
     * @return The {@link ContentGenerator} or throw an {@link Exception}
     */
    public ContentGenerator getContentGenerator(Class<? extends ContentGenerator> contentGeneratorClass) {
        try {
            Map<Class<? extends ContentGenerator>, ContentGenerator> contentGeneratorMap =
                ContentGeneratorRepository.getInstance().getContentGeneratorMap();
            if (!contentGeneratorMap.containsKey(contentGeneratorClass)) {
                ContentGenerator contentGenerator = contentGeneratorClass.newInstance();
                contentGeneratorMap.put(contentGeneratorClass, contentGenerator);
            }
            return contentGeneratorMap.get(contentGeneratorClass);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }
}
