package com.credibledoc.log.labelizer.pagepattern;

import com.credibledoc.log.labelizer.datastore.DatastoreService;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.internal.MorphiaCursor;

import java.util.Collections;
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
            .field(PagePattern.Fields.pageUrl)
            .equal(pageUrl).count() > 0;
    }

    /**
     * @param pattern the filter.
     * @return 'true' if there is the item with such pattern.
     */
    public boolean containsPattern(String pattern) {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.Fields.pattern)
            .equal(pattern).count() > 0;
    }

    /**
     * Save the entities to the database.
     */
    public void save(List<PagePattern> pagePattens) {
        datastore.save(pagePattens);
    }

    /**
     * Call the {@link #save(List)} method with a single item.
     * @param pagePattern the item will be saved or updated in the database. 
     */
    public void save(PagePattern pagePattern) {
        save(Collections.singletonList(pagePattern));
    }

    /**
     * @return All entities without {@link PagePattern#getPattern()} and {@link PagePattern#getVisited()} values.
     */
    public MorphiaCursor<PagePattern> getCursorOfEmptyPatterns() {
        return datastore.createQuery(PagePattern.class)
            .filter(PagePattern.Fields.pattern, null)
            .filter(PagePattern.Fields.visited, null)
            .find();
    }

    /**
     * @return Count of entities with non-empty {@link PagePattern#getPattern()} values.
     */
    public long countNotTrainedPatterns() {
        return existingPatternAndNotTrained()
            .count();
    }

    /**
     * Get a {@link PagePattern} with {@link PagePattern#isTrained()} empty or false
     * and existing {@link PagePattern#getPattern()}.
     * @return The first one from the filter.
     */
    public PagePattern getNotTrainedPattern() {
        return existingPatternAndNotTrained().first();
    }

    private Query<PagePattern> existingPatternAndNotTrained() {
        return datastore.createQuery(PagePattern.class)
            .field(PagePattern.Fields.pattern).exists()
            .field(PagePattern.Fields.isTrained).equal(false);
    }

    /**
     * Delete the entity from the database.
     * @param pagePattern entity to be deleted.
     */
    public void delete(PagePattern pagePattern) {
        datastore.delete(pagePattern);
    }

    /**
     * Set all {@link PagePattern#isTrained()} to 'false'.
     */
    public void resetTrained() {
        UpdateOperations<PagePattern> updateOperations = datastore.createUpdateOperations(PagePattern.class)
            .set(PagePattern.Fields.isTrained, false);
        
        Query<PagePattern> query = datastore.createQuery(PagePattern.class)
            .field(PagePattern.Fields.pattern).exists()
            .field(PagePattern.Fields.isTrained).equal(true);

        datastore.update(query, updateOperations);
    }
}
