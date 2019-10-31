package com.credibledoc.log.labelizer.github;

import com.credibledoc.log.labelizer.datastore.DatastoreService;
import dev.morphia.Datastore;

import java.util.List;

/**
 * Data Access Object for the {@link GithubRepo} records.
 * 
 * @author Kyrylo Semenko
 */
public class GithubRepoRepository {
    /** Refers to a {@link DatastoreService#getDatastore()} object */
    private Datastore datastore;

    /**
     * Singleton.
     */
    private static GithubRepoRepository instance;

    /**
     * @return The {@link GithubRepoRepository} singleton.
     */
    public static GithubRepoRepository getInstance() {
        if (instance == null) {
            instance = new GithubRepoRepository();
            instance.datastore = DatastoreService.getInstance().getDatastore();
        }
        return instance;
    }

    /**
     * @return 'true' if the database contains a {@link GithubRepo} object with such {@link GithubRepo#getFullName()}.
     */
    public boolean contains(String repoFullName) {
        return datastore.createQuery(GithubRepo.class)
            .field(GithubRepo.FULL_NAME)
            .equal(repoFullName).count() > 0;
    }

    /**
     * Save entities to the database.
     */
    public void save(List<GithubRepo> githubRepos) {
        datastore.save(githubRepos);
    }

    /**
     * @return All documents from the {@link GithubRepo} collection where {@link GithubRepo#getVisited()} is 'null'.
     */
    public List<GithubRepo> selectNotVisited() {
        return datastore.find(GithubRepo.class).filter(GithubRepo.VISITED, null).find().toList();
    }
}
