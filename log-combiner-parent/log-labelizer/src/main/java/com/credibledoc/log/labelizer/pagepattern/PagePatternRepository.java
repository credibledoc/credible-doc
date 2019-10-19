package com.credibledoc.log.labelizer.pagepattern;

import com.credibledoc.log.labelizer.datastore.DatastoreService;
import dev.morphia.Datastore;

import java.util.List;

/**
 * Data access object with a {@link #datastore} state.
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

    public boolean containsPage(String pageUrl) {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.PAGE_URL)
            .equal(pageUrl).count() > 0;
    }

    public boolean containsPattern(String pattern) {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.PATTERN)
            .equal(pattern).count() > 0;
    }

    public void save(List<PagePattern> pagePattens) {
        datastore.save(pagePattens);
    }
}
