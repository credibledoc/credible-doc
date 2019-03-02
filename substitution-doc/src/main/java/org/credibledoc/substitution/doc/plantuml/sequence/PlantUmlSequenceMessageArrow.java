package org.credibledoc.substitution.doc.plantuml.sequence;

/**
 * Enum of possible message arrows in a sequence diagram, see <a href="http://plantuml.com/sequence-diagram">http://plantuml.com/sequence-diagram</a>
 * @author Kyrylo Semenko
 */
public enum PlantUmlSequenceMessageArrow {
    
    /** Arrow ' -> ' */
    FULL_ARROW(" -> "),
    
    /** Arrow ' --> ' */
    DASH_ARROW(" --> "),
    
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
