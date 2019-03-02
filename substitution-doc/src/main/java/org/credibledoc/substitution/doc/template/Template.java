package org.credibledoc.substitution.doc.template;

/**
 * Type of template file, contains a {@link #templateRelativePath} field
 * @author Kyrylo Semenko
 */
public enum Template {
    
    /** HTML styles */
    CSS("/template/css/css.css"),
    
    /** js/jquery-1.11.0.min.js */
    JQUERY("/template/js/jquery-1.11.0.min.js"),
    
    /** js/script.js */
    JAVASCRIPT("/template/js/script.js"),
    
    /** End of Plant UML */
    END_UML_DIV("/template/endUmlDiv.html"),

    /** A header of plain text template */
    SEQUENCE_DIAGRAM_HEADER("/template/sequenceDiagramHeader.html"),
    
    /** A footer of plain text template */
    SEQUENCE_DIAGRAM_FOOTER("/template/sequenceDiagramFooter.html"),
    
    /** A header of source file escaped as html */
    SOURCE_LOG_HEADER("/template/sourceLogHeader.html");
    
    /** A path in a classpath, for example "/html/template/sourceLog.html" */
    private final String templateRelativePath;
    
    /** Constructor to set {@link #templateRelativePath} */
    Template(String templateRelativePath) {
        this.templateRelativePath = templateRelativePath;
    }

    /**
     * @return The {@link Template#templateRelativePath} field
     */
    public String getTemplateRelativePath() {
        return templateRelativePath;
    }

}
