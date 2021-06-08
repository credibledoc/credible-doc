package com.credibledoc.iso8583packer.masking;

import com.credibledoc.iso8583packer.string.StringUtils;

/**
 * Replace all bytes with '99' and value.toString() with '*'.
 * 
 * @author Kyrylo Semenko
 */
public class AnyMasker implements Masker {
    @Override
    public String maskHex(String hex) {
        return StringUtils.leftPad("", hex.length(), '9');
    }

    @Override
    public String maskValue(Object value) {
        return StringUtils.leftPad("", value.toString().length(), '*');
    }
}
