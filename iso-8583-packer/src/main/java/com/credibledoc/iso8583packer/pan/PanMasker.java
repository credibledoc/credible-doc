package com.credibledoc.iso8583packer.pan;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.masking.Masker;

/**
 * See {@link #maskHex(String)} and {@link #maskValue(Object)} methods.
 * 
 * See the
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/pan/pan-masker.md">masker.md</a>
 * documentation.
 * 
 * @author Kyrylo Semenko
 */
public class PanMasker implements Masker {

    /**
     * Single instance.
     */
    private static final PanMasker instance = new PanMasker();

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private PanMasker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static PanMasker getInstance() {
        return instance;
    }
    
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
