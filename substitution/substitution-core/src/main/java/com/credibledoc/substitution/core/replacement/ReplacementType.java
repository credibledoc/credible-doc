package com.credibledoc.substitution.core.replacement;

/**
 * Defines how an svg content will be inserted into a template. For example as an embedded <b>svg</b> tag or as a link
 * to an svg file.
 * 
 * @author Kyrylo Semenko
 */
public enum ReplacementType {
    HTML_EMBEDDED;

    /**
     * The {@link com.credibledoc.substitution.core.placeholder.Placeholder} parameter key.
     */
    public static final String TARGET_FORMAT = "targetFormat";
}
