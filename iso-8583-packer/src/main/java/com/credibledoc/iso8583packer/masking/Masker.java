package com.credibledoc.iso8583packer.masking;

import com.credibledoc.iso8583packer.message.MsgValue;

/**
 * Uses for masking of private sensitive data for logging purposes.
 * 
 * @author Kyrylo Semenko
 */
public interface Masker {
    /**
     * Hides private data for logging.
     * @param hex the {@link MsgValue#getBodyBytes()} field.
     * @return For example 9999999999
     */
    String maskHex(String hex);

    /**
     * Hides private data for logging.
     * @param value the {@link MsgValue#getBodyValue()} field.
     * @return For example 123******
     */
    String maskValue(Object value);
}
