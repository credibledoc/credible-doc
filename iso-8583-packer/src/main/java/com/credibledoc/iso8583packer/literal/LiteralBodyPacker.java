package com.credibledoc.iso8583packer.literal;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * This {@link BodyPacker} does no conversion and leaves the input the same as the output.
 * 
 * @author Kyrylo Semenko
 */
public class LiteralBodyPacker implements BodyPacker {
    /**
     * The only instance of this {@link BodyPacker}.
     */
    public static final LiteralBodyPacker INSTANCE = new LiteralBodyPacker();

    /**
     * Private constructor so we don't allow multiple instances.
     */
    private LiteralBodyPacker() {
        // empty
    }

    /**
     * Expected object of byte[] type.
     * @param object the byte[] data to be packed.
     * @param bytes  an empty or partially filled bytes.
     * @param offset the index of the first unfilled byte in the bytes array from start packing at.
     */
    @Override
    public void pack(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return;
        }
        if (!(object instanceof byte[])) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getSimpleName());
        }
        byte[] data = (byte[]) object;
        System.arraycopy(data, 0, bytes, offset, data.length);
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes to unpack.
     * @param <T> byte[] array.
     * @return The byte[] array.
     */
    @Override
    public <T> T unpack(byte[] sourceData, int offset, int bytesCount) {
        byte[] ret = new byte[bytesCount];
        System.arraycopy(sourceData, offset, ret, 0, bytesCount);
        return (T) ret;
    }

    /**
     * @param object the Object for packing. Expected an array of bytes.
     * @return The byte array length.
     */
    @Override
    public int getPackedLength(Object object) {
        if (object == null) {
            return 0;
        }
        if (!(object instanceof byte[])) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getSimpleName());
        }
        return ((byte[])object).length;
    }
}
