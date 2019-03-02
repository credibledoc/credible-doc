package org.credibledoc.substitution.doc.plantuml;

import org.credibledoc.substitution.doc.SubstitutionDocMain;

/**
 * PlantUML participants, for example {@link #PLACEHOLDER_SUBSTITUTION}.
 * @author Kyrylo Semenko
 */
public enum Participant {

    /**
     * The placeholder-substitution module.
     */
    PLACEHOLDER_SUBSTITUTION("\"placeholder-substitution\""),

    /**
     * This module.
     */
    SUBSTITUTION_DOC("\"" + SubstitutionDocMain.SUBSTITUTION_DOC + "\"");

    /** A name in a diagram */
    private final String uml;
    
    /**
     * @param uml see {@link #uml}
     */
    Participant(String uml) {
        this.uml = uml;
    }

    /**
     * @return The {@link Participant#uml} field
     */
    public String getUml() {
        return uml;
    }

}
