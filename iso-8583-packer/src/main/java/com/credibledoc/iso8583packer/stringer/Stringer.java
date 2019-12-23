package com.credibledoc.iso8583packer.stringer;

/**
 * Defines a method for conversion from {@link Object} to {@link String} for logging purposes.
 * 
 * @author Kyrylo Semenko
 */
public interface Stringer {
    
    /**
     * Convert object to {@link String}.
     * @param object the source of the {@link String}
     * @return For example Long 1 can be converted to "1".
     */
    String convert(Object object);
}
