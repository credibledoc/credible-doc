package com.credibledoc.iso8583packer.asciihex;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Implements ASCII {@link BodyPacker} of String value. Strings are converted to and from ASCII bytes.
 * This uses the {@link #ISO_88591} encoding.
 *
 * // TODO Kyrylo Semenko - example in documentation
 * @author Kyrylo Semenko
 */
public class AsciiBodyPacker implements BodyPacker {

    /**
     * An instance of this {@link BodyPacker}. Only one needed for the whole system
     */
    public static final BodyPacker INSTANCE = new AsciiBodyPacker();
    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;

    /**
     * Only one instance is allowed, see {@link #INSTANCE}
     */
    private AsciiBodyPacker() {
        // empty
    }

    /**
     * @param object the data to be packed. Expected String value.
     * @param bytes  an empty or partially filled bytes.
     * @param offset the index of the first unfilled byte in the bytes array from start packing at.
     */
    @Override
    public void pack(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return;
        }
        if (!(object instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getName());
        }
        String data = (String) object;
        for (int i = data.length() - 1; i >= 0; i--) {
            bytes[offset + i] = (byte) data.charAt(i);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unpack(byte[] sourceData, int offset, int bytesCount) {
        byte[] ret = new byte[bytesCount];
        System.arraycopy(sourceData, offset, ret, 0, bytesCount);
        return (T) new String(ret, ISO_88591);
    }

    @Override
    public int getPackedLength(Object object) {
        if (object == null) {
            return 0;
        }
        if (!(object instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getName());
        }

        String string = (String) object;

        return string.length();
    }
}
