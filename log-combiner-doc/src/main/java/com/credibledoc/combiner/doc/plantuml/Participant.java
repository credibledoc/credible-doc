package com.credibledoc.combiner.doc.plantuml;

import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.combiner.doc.CombinerDocMain;

/**
 * PlantUML participants, for example {@link #SUBSTITUTION_DOC}.
 * @author Kyrylo Semenko
 */
public enum Participant {

    /**
     * The placeholder-combiner module.
     */
    SUBSTITUTION_CORE("\"" + ResourceService.SUBSTITUTION_CORE_MODULE_NAME + "\""),

    /**
     * This module.
     */
    SUBSTITUTION_DOC("\"" + CombinerDocMain.SUBSTITUTION_DOC + "\"");

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
