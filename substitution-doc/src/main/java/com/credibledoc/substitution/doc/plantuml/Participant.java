package com.credibledoc.substitution.doc.plantuml;

import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.doc.SubstitutionDocMain;

/**
 * PlantUML participants, for example {@link #SUBSTITUTION_DOC}.
 * @author Kyrylo Semenko
 */
public enum Participant {

    /**
     * The placeholder-substitution module.
     */
    SUBSTITUTION_CORE("\"" + ResourceService.SUBSTITUTION_CORE_MODULE_NAME + "\""),

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
