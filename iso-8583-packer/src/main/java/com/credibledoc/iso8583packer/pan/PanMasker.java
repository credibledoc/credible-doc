package com.credibledoc.iso8583packer.pan;

import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * See {@link #maskHex(String)} and {@link #maskValue(Object)} methods.
 * 
 * @author Kyrylo Semenko
 */
public class PanMasker implements Masker {

    public static final PanMasker INSTANCE = new PanMasker();
    
    private static final String FILLER = "*";

    /**
     * @return For example 1234***************
     */
    @Override
    public String maskHex(String hex) {
        if (hex == null) {
            return null;
        }
        return mask(hex);
    }

    /**
     * @return For example 1234***************
     */
    @Override
    public String maskValue(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + value.getClass().getSimpleName());
        }
        return mask((String) value);
    }

    private String mask(String hex) {
        StringBuilder stringBuilder = new StringBuilder(hex.length());
        for (int i = 0; i < hex.length(); i++) {
            if (i < 4) {
                stringBuilder.append(hex.charAt(i));
            } else {
                stringBuilder.append(FILLER);
            }
        }
        return stringBuilder.toString();
    }
}
