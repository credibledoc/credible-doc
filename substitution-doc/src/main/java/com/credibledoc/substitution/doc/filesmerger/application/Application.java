package com.credibledoc.substitution.doc.filesmerger.application;

import com.credibledoc.substitution.doc.filesmerger.specific.SpecificTactic;

/**
 * This interface represents one application that generates log files.
 * This application holds an instance of {@link SpecificTactic}, se
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
