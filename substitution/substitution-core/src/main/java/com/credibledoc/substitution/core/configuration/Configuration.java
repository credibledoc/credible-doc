package com.credibledoc.substitution.core.configuration;

/**
 * This data object contains application configuration properties.
 *
 * @author Kyrylo Semenko
 */
public class Configuration {

    /**
     * This parameter is stored in the property with the
     * {@link ConfigurationService#TEMPLATES_RESOURCE_KEY} configuration key.
     * <p>
     * Its default value is {@link ConfigurationService#TEMPLATES_RESOURCE}.
     */
    @ConfigurationProperty(key = ConfigurationService.TEMPLATES_RESOURCE_KEY,
            defaultValue = ConfigurationService.TEMPLATES_RESOURCE)
    private String templatesResource;

    /**
     * This parameter is stored in the property with the
     * {@link ConfigurationService#PLACEHOLDER_BEGIN_KEY} configuration key.
     * <p>
     * Its default value is {@link ConfigurationService#PLACEHOLDER_BEGIN}.
     */
    @ConfigurationProperty(key = ConfigurationService.PLACEHOLDER_BEGIN_KEY,
            defaultValue = ConfigurationService.PLACEHOLDER_BEGIN)
    private String placeholderBegin;

    /**
     * This parameter is stored in the property with the
     * {@link ConfigurationService#PLACEHOLDER_END_KEY} configuration key.
     * <p>
     * Its default value is {@link ConfigurationService#PLACEHOLDER_END}.
     */
    @ConfigurationProperty(key = ConfigurationService.PLACEHOLDER_END_KEY,
            defaultValue = ConfigurationService.PLACEHOLDER_END)
    private String placeholderEnd;

    /**
     * This parameter is stored in the property with the
     * {@link ConfigurationService#TARGET_DIRECTORY_KEY} configuration key.
     * <p>
     * Its default value is {@link ConfigurationService#TARGET_DIRECTORY}.
     */
    @ConfigurationProperty(key = ConfigurationService.TARGET_DIRECTORY_KEY,
            defaultValue = ConfigurationService.TARGET_DIRECTORY)
    private String targetDirectory;

    /**
     * This parameter is stored in the property with the
     * {@link ConfigurationService#REPLACE_FILTER_ID_KEY} configuration key.
     * <p>
     * Its default value is {@link ConfigurationService#REPLACE_FILTER_ID}.
     */
    @ConfigurationProperty(key = ConfigurationService.REPLACE_FILTER_ID_KEY,
            defaultValue = ConfigurationService.REPLACE_FILTER_ID)
    private String replaceFilterId;

    /**
     * @return The {@link #templatesResource} field value.
     */
    public String getTemplatesResource() {
        return templatesResource;
    }

    /**
     * @param templatesResource see the {@link #templatesResource} field
     */
    public void setTemplatesResource(String templatesResource) {
        this.templatesResource = templatesResource;
    }

    /**
     * @return The {@link #placeholderBegin} field value.
     */
    public String getPlaceholderBegin() {
        return placeholderBegin;
    }

    /**
     * @param placeholderBegin see the {@link #placeholderBegin} field
     */
    public void setPlaceholderBegin(String placeholderBegin) {
        this.placeholderBegin = placeholderBegin;
    }

    /**
     * @return The {@link #placeholderEnd} field value.
     */
    public String getPlaceholderEnd() {
        return placeholderEnd;
    }

    /**
     * @param placeholderEnd see the {@link #placeholderEnd} field
     */
    public void setPlaceholderEnd(String placeholderEnd) {
        this.placeholderEnd = placeholderEnd;
    }

    /**
     * @return The {@link #targetDirectory} field value.
     */
    public String getTargetDirectory() {
        return targetDirectory;
    }

    /**
     * @param targetDirectory see the {@link #targetDirectory} field
     *                        description.
     */
    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    /**
     * @return The {@link #replaceFilterId} field value.
     */
    public String getReplaceFilterId() {
        return replaceFilterId;
    }

    /**
     * @param replaceFilterId see the {@link #replaceFilterId} field description.
     */
    public void setReplaceFilterId(String replaceFilterId) {
        this.replaceFilterId = replaceFilterId;
    }
}
