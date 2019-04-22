package com.credibledoc.combiner.application;

import com.credibledoc.combiner.tactic.Tactic;

/**
 * This interface represents one application that generates log files.
 * This application holds an instance of {@link Tactic}, se
 * the {@link #getTactic()} method and has a short name,
 * see the {@link #getShortName()} method.
 */
public interface Application {

    /**
     * @return An instance of a class implemented the {@link Tactic} interface.
     */
    Tactic getTactic();

    /**
     * This name is used for identification of log lines in created
     * reports. It should be short and unique in the {@link Application}
     * implementations.
     *
     * @return for example 'my-application-or-module'
     */
    String getShortName();
}
