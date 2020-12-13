package com.credibledoc.iso8583packer.binary;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.stringer.Stringer;

/**
 * Converts byte array to hex String.
 * 
 * @author Kyrylo Semenko
 */
public class BinaryToHexStringer implements Stringer {

    /**
     * Single instance.
     */
    private static final BinaryToHexStringer instance = new BinaryToHexStringer();

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private BinaryToHexStringer() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static BinaryToHexStringer getInstance() {
        return instance;
    }

    @Override
    public String convert(Object object) {
        if (object == null) {
            return null;
        }
        if (!(object instanceof byte[])) {
            throw new PackerRuntimeException("Expected byte array but found " + object.getClass().getSimpleName());
        }
        return HexService.bytesToHex((byte[]) object);
    }
}
