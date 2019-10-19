package com.credibledoc.log.labelizer.pagepattern;

import dev.morphia.annotations.Entity;

/**
 * This Data Transfer Object contains a state of this crawler. It represents a single Document or row in terms of
 * relation databases. It contains an information if some pattern has been
 * learned by {@link com.credibledoc.log.labelizer.classifier.LinesWithDateClassification} or not. It has
 * information of the {@link #pattern} source url.
 * 
 * @author Kyrylo Semenko
 */
@Entity
public class PagePattern {

    /**
     * The page where these patterns where found.
     */
    private String pageUrl;
    public static final String PAGE_URL = "pageUrl";

    /**
     * Contains date time pattern, for example <b>"yyyy.MM.dd HH:mm:ss Z"</b>
     */
    private String pattern;
    public static final String PATTERN = "pattern";

    /**
     * This value is 'true' when the {@link com.credibledoc.log.labelizer.classifier.LinesWithDateClassification}
     * network has been trained with this {@link #pattern}.
     */
    private boolean isTrained;

    /**
     * In case of error only.
     */
    private String errorMessage;
    public static final String ERROR_MESSAGE = "errorMessage";

    public PagePattern(String pageUrl, String pattern) {
        this.pageUrl = pageUrl;
        this.pattern = pattern;
    }

    public PagePattern() {
        // empty
    }

    @Override
    public String toString() {
        return "PagePattern{" +
            "pageUrl='" + pageUrl + '\'' +
            ", pattern='" + pattern + '\'' +
            ", errorMessage='" + errorMessage + '\'' +
            ", isTrained=" + isTrained +
            '}';
    }

    /**
     * @return The {@link #pageUrl} field value.
     */
    public String getPageUrl() {
        return pageUrl;
    }

    /**
     * @param pageUrl see the {@link #pageUrl} field description.
     */
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    /**
     * @return The {@link #pattern} field value.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern see the {@link #pattern} field description.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return The {@link #isTrained} field value.
     */
    public boolean isTrained() {
        return isTrained;
    }

    /**
     * @param trained see the {@link #isTrained} field description.
     */
    public void setTrained(boolean trained) {
        isTrained = trained;
    }

    /**
     * @return The {@link #errorMessage} field value.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage see the {@link #errorMessage} field description.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
