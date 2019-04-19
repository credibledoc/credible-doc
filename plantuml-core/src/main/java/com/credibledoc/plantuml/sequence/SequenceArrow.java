package com.credibledoc.plantuml.sequence;

/**
 * The enum of some message arrows in a sequence diagram,
 * see the <a href="http://plantuml.com/sequence-diagram">http://plantuml.com/sequence-diagram</a>
 * documentation.
 *
 * @author Kyrylo Semenko
 */
public enum SequenceArrow {

    /**
     * Arrow {@code ' ..> '}
     */
    DEPENDENCY_ARROW(" ..> "),

    /**
     * Arrow {@code ' -> '}
     */
    INHERITANCE_ARROW(" -> "),

    /**
     * Arrow {@code ' --> '}
     */
    IMPLEMENTATION_ARROW(" --> "),

    /**
     * Arrow {@code ' -[#red]>x '}
     */
    ERROR_RED_ARROW(" -[#red]>x "),

    /**
     * Arrow {@code ' <-> '}
     */
    DOUBLE_SIZE_FULL_ARROW(" <-> "),

    /**
     * {@link #INHERITANCE_ARROW} alias
     */
    FULL_ARROW(" -> "),

    /**
     * {@link #IMPLEMENTATION_ARROW} alias
     */
    DASH_ARROW(" --> "),

    /**
     * {@link #DOUBLE_SIZE_FULL_ARROW} alias
     */
    DOUBLE_SIZED_FULL_ARROW(" <-> ");

    /**
     * A mame in a diagram
     */
    private final String uml;

    /**
     * @param uml see {@link #uml}
     */
    SequenceArrow(String uml) {
        this.uml = uml;
    }

    /**
     * @return The {@link SequenceArrow#uml} field
     */
    public String getUml() {
        return uml;
    }

}
