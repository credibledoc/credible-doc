package com.credibledoc.log.labelizer.github;

import com.credibledoc.log.labelizer.datastore.DatastoreService;
import dev.morphia.Datastore;

import java.util.List;

/**
 * Data Access Object for the {@link VisitedUrl} records.
 * 
 * @author Kyrylo Semenko
 */
public class VisitedUrlRepository {
    /** Refers to a {@link DatastoreService#getDatastore()} object */
    private Datastore datastore;

    /**
     * Singleton.
     */
    private static VisitedUrlRepository instance;

    /**
     * @return The {@link VisitedUrlRepository} singleton.
     */
    public static VisitedUrlRepository getInstance() {
        if (instance == null) {
            instance = new VisitedUrlRepository();
            instance.datastore = DatastoreService.getInstance().getDatastore();
        }
        return instance;
    }

    /**
     * @return 'true' if the database contains a {@link VisitedUrl} object with such {@link VisitedUrl#getUrl()}.
     * @param url the filter
     */
    public boolean contains(String url) {
        return datastore.createQuery(VisitedUrl.class)
            .field(VisitedUrl.URL)
            .equal(url).count() > 0;
    }

    /**
     * Save entities to the database.
     * @param visitedUrlList entities to be saved
     */
    public void save(List<VisitedUrl> visitedUrlList) {
        datastore.save(visitedUrlList);
    }
}
