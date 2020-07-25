package com.credibledoc.substitution.core.placeholder;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.eclipsesource.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This data object represents a placeholder in a template file.
 * The placeholder begins with
 * {@link ConfigurationService#PLACEHOLDER_BEGIN}
 * and ends with
 * {@link ConfigurationService#PLACEHOLDER_END}
 * tags.
 *
 * @author Kyrylo Semenko
 */
public class Placeholder {

    public static final String FIELD_JSON_OBJECT = "jsonObject";
    public static final String FIELD_PARAMETERS = "parameters";
    public static final String FIELD_RESOURCE = "resource";
    public static final String FIELD_CLASS_NAME = "className";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ID = "id";

    /**
     * A resource this placeholder belongs to, for example
     * <pre>/template/doc/README.md</pre>
     */
    private TemplateResource resource;

    /**
     * Identifier, for example "1". Should be unique within a template.
     * The value is generated.
     */
    private String id;

    /**
     * The name of a class for generating content.
     * This placeholder will be replaced with the content.
     * The field is mandatory.
     */
    private String className;

    /**
     * Description for debugging purposes, for example
     * <pre>An UML diagram of application launching.</pre>
     * It can be use for generating an image alternative text, for logging and so on.
     * The field is optional.
     */
    private String description;

    /**
     * The parameters can be used for customization of the placeholder content.
     * The field is optional.
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Optional parameter with arbitrary content. Can be used like {@link #parameters},
     * but in a more generalized way.
     * The field is optional.
     */
    private JsonObject jsonObject;

    @Override
    public String toString() {
        return "{" +
            "id='" + id + '\'' +
            ", className='" + className + '\'' +
            ", description='" + description + '\'' +
            ", parameters=" + parameters + '\'' +
            ", resource='" + resource + '\'' +
            ", jsonObject='" + jsonObject +
            '}';
    }

    /**
     * @return The {@link #resource} field value.
     */
    public TemplateResource getResource() {
        return resource;
    }

    /**
     * @param resource see the {@link #resource} field description.
     */
    public void setResource(TemplateResource resource) {
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

    /**
     * @return The {@link #jsonObject} field value.
     */
    public JsonObject getJsonObject() {
        return jsonObject;
    }

    /**
     * @param jsonObject see the {@link #jsonObject} field description.
     */
    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
