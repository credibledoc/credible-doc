package org.credibledoc.substitution.doc.module.tactic;

import org.credibledoc.substitution.doc.application.Application;
import org.credibledoc.substitution.doc.module.substitution.SubstitutionSpecificTactic;
import org.credibledoc.substitution.doc.specific.SpecificTactic;

/**
 * This enum represents applications, for example placeholder-substitution.
 * @author Kyrylo Semenko
 */
public enum TacticHolder implements Application {

    /**
     * The placeholder-substitution module. It is located in the
     * https://github.com/credibledoc/placeholder-substitution.git  repository.
     * <p>
     * Its log can be visualized and used for creation of this documentation.
     */
    SUBSTITUTION(SubstitutionSpecificTactic.class, "placeholder-substitution");

    /**
     * A strategy to be applied for looking for a date in a file.
     */
    private Class<? extends SpecificTactic> specificTacticClass;

    /**
     * Shortcut of the name, for printing in a log file.
     */
    private final String shortName;

    TacticHolder(Class<? extends SpecificTactic> dateFinderClass, String shortName) {
        this.specificTacticClass = dateFinderClass;
        this.shortName = shortName;
    }

    @Override
    public Class<? extends SpecificTactic> getSpecificTacticClass() {
        return specificTacticClass;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

}
