package com.apache.credibledoc.plantuml.sequence;

/**
 * The enum of some message arrows in a sequence diagram,
 * see the <a href="http://plantuml.com/sequence-diagram">http://plantuml.com/sequence-diagram</a>
 * documentation.
 *
 * @author Kyrylo Semenko
 */
public enum SequenceArrow {

    /**
     * Arrow {@code ' -> '}
     */
    FULL_ARROW(" -> "),

    /**
     * Arrow {@code ' --> '}
     */
    DASH_ARROW(" --> "),

    /**
     * Arrow {@code ' -[#red]>x '}
     */
    ERROR_RED_ARROW(" -[#red]>x "),

    /**
     * Arrow {@code ' <-> '}
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
