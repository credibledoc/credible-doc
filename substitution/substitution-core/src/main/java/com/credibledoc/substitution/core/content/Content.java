package com.credibledoc.substitution.core.content;

/**
 * This data object contains a result of content generation performed by {@link ContentGenerator}.
 */
// TODO Kyrylo Semenko - delete Content and create PostProcessor for transformation to svg or other formats.
public class Content {

    /**
     * This content will be placed instead of placeholder. It contains generated documentation part, for example
     * javaDoc of some method or a table generated from the application source code in the markdown format.
     */
    private String markdownContent;

    /**
     * This value can be 'null'. It contains PlantUML notations for generating PlantUML diagram.
     */
    private String plantUmlContent;

    /**
     * @return The {@link #markdownContent} field value.
     */
    public String getMarkdownContent() {
        return markdownContent;
    }

    /**
     * @param markdownContent see the {@link #markdownContent} field description.
     */
    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    /**
     * @return The {@link #plantUmlContent} field value.
     */
    public String getPlantUmlContent() {
        return plantUmlContent;
    }

    /**
     * @param plantUmlContent see the {@link #plantUmlContent} field description.
     */
    public void setPlantUmlContent(String plantUmlContent) {
        this.plantUmlContent = plantUmlContent;
    }
}
