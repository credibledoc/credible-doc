package com.credibledoc.log.labelizer.pagepattern;

import com.credibledoc.log.labelizer.datastore.DatastoreService;
import dev.morphia.Datastore;
import dev.morphia.query.internal.MorphiaCursor;

import java.util.List;

/**
 * Data access object with a {@link #datastore} state.
 * 
 * @author Kyrylo Semenko
 */
public class PagePatternRepository {
    /** Refers to a {@link DatastoreService#getDatastore()} object */
    private Datastore datastore;

    /**
     * Singleton.
     */
    private static PagePatternRepository instance;

    /**
     * @return The {@link PagePatternRepository} singleton.
     */
    public static PagePatternRepository getInstance() {
        if (instance == null) {
            instance = new PagePatternRepository();
            instance.datastore = DatastoreService.getInstance().getDatastore();
        }
        return instance;
    }

    /**
     * @param pageUrl the filter.
     * @return The value 'true' if there is the item with such pageUrl.
     */
    public boolean containsPage(String pageUrl) {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.PAGE_URL)
            .equal(pageUrl).count() > 0;
    }

    /**
     * @param pattern the filter.
     * @return 'true' if there is the item with such pattern.
     */
    public boolean containsPattern(String pattern) {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.PATTERN)
            .equal(pattern).count() > 0;
    }

    /**
     * Save the entities to DB.
     */
    public void save(List<PagePattern> pagePattens) {
        datastore.save(pagePattens);
    }

    /**
     * @return All entities without {@link PagePattern#getPattern()} and {@link PagePattern#getVisited()} values.
     */
    public MorphiaCursor<PagePattern> getCursorOfEmptyPatterns() {
        return datastore.createQuery(PagePattern.class)
            .filter(PagePattern.PATTERN, null)
            .filter(PagePattern.VISITED, null)
            .find();
    }
}
