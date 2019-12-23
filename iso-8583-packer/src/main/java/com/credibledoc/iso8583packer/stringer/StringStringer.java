package com.credibledoc.iso8583packer.stringer;

import java.util.Objects;

/**
 * Uses {@link Object#toString()} method.
 * 
 * @author Kyrylo Semenko
 */
public class StringStringer implements Stringer {

    /**
     * Static instance holder.
     */
    public static final Stringer INSTANCE = new StringStringer();

    @Override
    public String convert(Object object) {
        return Objects.toString(object);
    }
}
