package com.credibledoc.iso8583packer.offset;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * This object contains a state of some byte array offset. It will be used for avoiding of passing a primitive value
 * and for decomposition of methods which uses offsets.
 * 
 * @author Kyrylo Semenko
 */
public class Offset {
    /**
     * Index in some array.
     */
    private int value;

    @Override
    public String toString() {
        return "Offset{" +
                "value=" + value +
                '}';
    }

    /**
     * @return The {@link #value} field value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value see the {@link #value} field description.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Add addition to the current {@link #value}
     * @param addition cannot be 'null'
     */
    public void add(Integer addition) {
        if (addition == null) {
            throw new PackerRuntimeException("Parameter 'addition' cannot be 'null'.");
        }
        value = value + addition;
    }
}
