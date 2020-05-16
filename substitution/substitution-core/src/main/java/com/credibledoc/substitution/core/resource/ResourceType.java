package com.credibledoc.substitution.core.resource;

/**
 * The {@link TemplateResource} type.
 * 
 * @author Kyrylo Semenko
 */
public enum ResourceType {

    /**
     * The {@link TemplateResource} is located in a file system.
     */
    FILE,

    /**
     * The {@link TemplateResource} is located in a jar file.
     */
    // TODO Kyrylo Semenko - delete CLASSPATH and ResourceType
    CLASSPATH
}
