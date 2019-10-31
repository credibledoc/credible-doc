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
public class VisitedUrl {
    public static final String URL = "url";

    @Id
    private ObjectId id;

    /**
     * The page visited by the {@link com.credibledoc.log.labelizer.crawler.Crawler}.
     */
    @Indexed(options = @IndexOptions(unique = true))
    private String url;

    public VisitedUrl(String url) {
        this.url = url;
    }

    /**
     * @return The {@link #url} field value.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url see the {@link #url} field description.
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
