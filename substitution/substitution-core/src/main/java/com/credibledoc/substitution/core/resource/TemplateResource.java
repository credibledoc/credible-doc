package com.credibledoc.substitution.core.resource;

import java.io.File;
import java.util.Objects;

/**
 * Contains information about a template used for content generation.
 * 
 * @author Kyrylo Semenko
 */
public class TemplateResource {

    /**
     * Contains a path to a template resource. The template is located in a jar file.
     * The value presents when the {@link TemplateResource} is of the
     * {@link ResourceType#CLASSPATH} type.
     */
    private String path;

    /**
     * Contains a template resource. The template is located in a file system.
     * The value presents when the {@link TemplateResource} is of the
     * {@link ResourceType#FILE} type.
     */
    private File file;

    /**
     * The {@link TemplateResource} type.
     */
    private ResourceType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateResource)) return false;
        TemplateResource that = (TemplateResource) o;
        return Objects.equals(getPath(), that.getPath()) &&
            Objects.equals(getFile(), that.getFile()) &&
            getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getFile(), getType());
    }

    @Override
    public String toString() {
        return "TemplateResource{" +
            "path='" + path + '\'' +
            ", file=" + file +
            ", type=" + type +
            '}';
    }

    /**
     * @return The {@link #path} field value.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path see the {@link #path} field description.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return The {@link #file} field value.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file see the {@link #file} field description.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return The {@link #type} field value.
     */
    public ResourceType getType() {
        return type;
    }

    /**
     * @param type see the {@link #type} field description.
     */
    public void setType(ResourceType type) {
        this.type = type;
    }
}
