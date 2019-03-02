package com.credibledoc.substitution.placeholder;

import com.credibledoc.substitution.configuration.ConfigurationService;

import java.util.HashMap;
import java.util.Map;

/**
 * This data object represents a placeholder inside a markdown template file.
 * The placeholder begins with
 * {@link ConfigurationService#PLACEHOLDER_BEGIN}
 * and ends with
 * {@link ConfigurationService#PLACEHOLDER_END}
 * tags.
 *
 * @author Kyrylo Semenko
 */
public class Placeholder {

    /**
     * A resource this placeholder belongs to, for example
     * <pre>/template/doc/README.md</pre>
     */
    private String resource;

    /**
     * Identifier, for example "1". Should be unique within a template.
     */
    private String id;

    /**
     * The name of a class for generation of content.
     * This placeholder will be replaced with the content.
     */
    private String className;

    /**
     * Description for debugging purposes, for example
     * <pre>An UML diagram of application launching.</pre>
     * It uses for generation of alternative text of markdown
     * image.
     */
    private String description;

    /**
     * Each placeholder can have zero or more parameters. These parameters
     * can be used for customization of its content generation.
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * @return The {@link #resource} field value.
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource see the {@link #resource} field
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return The {@link #id} field value.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id see the {@link #id} field
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The {@link #className} field value.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className see the {@link #className} field
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return The {@link #description} field value.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description see the {@link #description} field
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The {@link #parameters} field value.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters see the {@link #parameters} field description.
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
