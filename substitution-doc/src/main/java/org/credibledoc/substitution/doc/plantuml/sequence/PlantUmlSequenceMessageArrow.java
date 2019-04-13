package org.credibledoc.substitution.doc.plantuml.sequence;

/**
 * Enum of possible message arrows in a sequence diagram, see <a href="http://plantuml.com/sequence-diagram">http://plantuml.com/sequence-diagram</a>
 * @author Kyrylo Semenko
 */
public enum PlantUmlSequenceMessageArrow {
    
    /** Arrow ' ..> ' */
    DEPENDENCY_ARROW(" ..> "),

    /** Arrow ' -> ' */
    INHERITANCE_ARROW(" -> "),
    
    /** Arrow ' --> ' */
    IMPLEMENTATION_ARROW(" --> "),
    
    /** Arrow ' -[#red]>x ' */
    ERROR_RED_ARROW(" -[#red]>x "),
    
    /** Arrow ' <-> ' */
    DOUBLE_SIZED_FULL_ARROW(" <-> ");
    
    /** A mame in a diagram */
    private final String uml;
    
    /**
     * @param uml see {@link #uml}
     */
    PlantUmlSequenceMessageArrow(String uml) {
        this.uml = uml;
    }

    /**
     * @return The {@link PlantUmlSequenceMessageArrow#uml} field
     */
    public String getUml() {
        return uml;
    }

}
