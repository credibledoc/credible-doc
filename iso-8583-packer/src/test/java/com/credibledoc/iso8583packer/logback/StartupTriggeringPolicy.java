package com.credibledoc.iso8583packer.logback;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

import java.io.File;

/**
 * The {@link TriggeringPolicyBase} that triggered on startup when the previous file is not empty.
 *
 * @param <E> this event not used in the trigger logic
 *
 * @author Kyrylo Semenko
 */
public class StartupTriggeringPolicy<E> extends TriggeringPolicyBase<E> {

    private boolean isTriggeringEvent = true;

    /**
     * Return 'true' when the application launched.
     * @param activeFile is used for the decision
     * @param event is not used
     * @return Return 'true' at first time. Then return 'false'.
     */
    public boolean isTriggeringEvent(final File activeFile, final E event) {
        if (isTriggeringEvent) {
            isTriggeringEvent = false;
            // Without this condition a zero - length file has been created next to the activeFile
            return activeFile.length() > 0;
        }
        return false;
    }

}
