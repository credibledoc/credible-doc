package com.credibledoc.substitution.core.json;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.Map;

/**
 * Provides serialization and deserialization methods for {@link Placeholder}s.
 *
 * @author Kyrylo Semenko
 */
public class JsonService {

    /**
     * Singleton.
     */
    private static JsonService instance;

    private JsonService() {
        // empty
    }

    /**
     * @return The {@link JsonService} singleton.
     */
    public static JsonService getInstance() {
        if (instance == null) {
            instance = new JsonService();
        }
        return instance;
    }

    /**
     * Convert JSON to {@link Placeholder} or its descendant. Client applications may decide
     * to extend the {@link Placeholder}.
     *
     * @param json       JSON representation of a {@link Placeholder} or its descendant
     * @param valueClass is not used, but may be used in extended descendants
     * @param <T>        {@link Placeholder} or its descendant class
     * @return {@link Placeholder} or its descendant
     */
    @SuppressWarnings("unchecked")
    public <T extends Placeholder> T readValue(String json, Class<T> valueClass) {
        try {
            Placeholder placeholder = new Placeholder();
            JsonValue value = Json.parse(json);
            JsonObject jsonObject = value.asObject();
            placeholder.setClassName(jsonObject.getString(Placeholder.FIELD_CLASS_NAME, null));
            placeholder.setDescription(jsonObject.getString(Placeholder.FIELD_DESCRIPTION, null));
            placeholder.setId(jsonObject.getString(Placeholder.FIELD_ID, null));
            if (jsonObject.get(Placeholder.FIELD_PARAMETERS) != null) {
                JsonObject parameters = jsonObject.get(Placeholder.FIELD_PARAMETERS).asObject();
                for (String name : parameters.names()) {
                    placeholder.getParameters().put(name, parameters.getString(name, null));
                }
            }
            if (jsonObject.get(Placeholder.FIELD_JSON_OBJECT) != null) {
                placeholder.setJsonObject(jsonObject.get(Placeholder.FIELD_JSON_OBJECT).asObject());
            }
            return (T) placeholder;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException("JSON cannot be processed: '" + json +
                "', valueClass: " + valueClass, e);
        }
    }

    /**
     * Crete a JSON from a {@link Placeholder} or its descendant. Client application can decide
     * to extend the {@link Placeholder}.
     *
     * @param placeholder {@link Placeholder} or its descendant.
     * @param <T>         {@link Placeholder} or its descendant class
     * @return JSON representation of the {@link Placeholder} or its descendant.
     */
    public <T extends Placeholder> String writeValueAsString(T placeholder) {
        try {
            JsonObject jsonObject = new JsonObject();
            String resource;
            if (placeholder.getResource().getType() == ResourceType.FILE) {
                resource = placeholder.getResource().getFile().getAbsolutePath();
            } else if (placeholder.getResource().getType() == ResourceType.CLASSPATH) {
                resource = placeholder.getResource().getPath();
            } else {
                throw new SubstitutionRuntimeException("Unknown ResourceType " + placeholder.getResource().getType());
            }
            jsonObject.add(Placeholder.FIELD_RESOURCE, resource);
            jsonObject.add(Placeholder.FIELD_CLASS_NAME, placeholder.getClassName());
            jsonObject.add(Placeholder.FIELD_DESCRIPTION, placeholder.getDescription());
            jsonObject.add(Placeholder.FIELD_ID, placeholder.getId());
            JsonObject parameters = new JsonObject();
            jsonObject.add(Placeholder.FIELD_PARAMETERS, parameters);
            for (Map.Entry<String, String> entry : placeholder.getParameters().entrySet()) {
                parameters.add(entry.getKey(), entry.getValue());
            }
            return jsonObject.toString();
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

}
