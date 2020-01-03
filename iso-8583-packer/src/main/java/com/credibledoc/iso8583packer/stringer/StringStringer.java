package com.credibledoc.iso8583packer.stringer;

import java.util.Objects;

/**
 * Uses {@link Object#toString()} method.
 * 
 * @author Kyrylo Semenko
 */
public class StringStringer implements Stringer {

    /**
     * Single instance.
     */
    private static StringStringer instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private StringStringer() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static StringStringer getInstance() {
        if (instance == null) {
            instance = new StringStringer();
        }
        return instance;
    }

    @Override
    public String convert(Object object) {
        return Objects.toString(object);
    }
}
