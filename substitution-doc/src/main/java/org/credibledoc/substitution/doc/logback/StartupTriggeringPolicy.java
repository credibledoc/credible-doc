package org.credibledoc.substitution.doc.logback;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

import java.io.File;

/**
 * {@link TriggeringPolicyBase} that triggered on startup.
 *
 * @param <E> this event not used in the trigger logic
 *
 * @author Kyrylo Semenko
 */
public class StartupTriggeringPolicy<E> extends TriggeringPolicyBase<E> {

    private boolean isTriggeringEvent = true;

    /**
     * Return 'true' when the application launched.
     * @param activeFile not used
     * @param event not used
     * @return Returns 'true' at first time. Then returns 'false'.
     */
    public boolean isTriggeringEvent(final File activeFile, final E event) {
        if (isTriggeringEvent) {
            isTriggeringEvent = false;
            return true;
        }
        return false;
    }

}
