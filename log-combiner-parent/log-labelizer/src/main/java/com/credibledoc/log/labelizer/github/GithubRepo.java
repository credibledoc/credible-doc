package com.credibledoc.log.labelizer.github;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

/**
 * Data Transfer Object.
 * 
 * @author Kyrylo Semenko
 */
@Entity
public class GithubRepo {
    public static final String FULL_NAME = "fullName";
    public static final String VISITED = "visited";

    @Id
    private ObjectId id;
    
    /**
     * For example credibledoc/credible-doc.
     */
    @Indexed(options = @IndexOptions(unique = true))
    private String fullName;

    /**
     * Does this repository processed and all files where checked?
     */
    private Boolean visited;
    
    public GithubRepo() {
        // empty constructor
    }

    public GithubRepo(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return The {@link #fullName} field value.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName see the {@link #fullName} field description.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return The {@link #visited} field value.
     */
    public Boolean getVisited() {
        return visited;
    }

    /**
     * @param visited see the {@link #visited} field description.
     */
    public void setVisited(Boolean visited) {
        this.visited = visited;
    }
}
