package org.credibledoc.substitution.doc.application;

import org.credibledoc.substitution.doc.specific.SpecificTactic;

/**
 * This interface represents behavior of an item of enumeration.
 * This item holds an instance of {@link SpecificTactic}, se
 * the {@link #getSpecificTacticClass()} method and has a short name,
 * see the {@link #getShortName()} method.
 */
public interface Application {

    /**
     * @return A class of {@link SpecificTactic} subtype.
     */
    Class<? extends SpecificTactic> getSpecificTacticClass();

    /**
     * This name is used for identification of log lines in created
     * reports. It should be short and unique in the {@link Application}
     * implementations.
     *
     * @return for example 'my-application-or-module'
     */
    String getShortName();
}
