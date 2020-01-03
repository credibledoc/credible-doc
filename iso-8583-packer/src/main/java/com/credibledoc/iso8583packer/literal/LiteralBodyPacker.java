package com.credibledoc.iso8583packer.literal;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * This {@link BodyPacker} does not convert any data and leaves the input the same as the output.
 * <p>
 * More examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/literal/literal-body-packer.md">literal-body-packer.md</a>
 * 
 * @author Kyrylo Semenko
 */
public class LiteralBodyPacker implements BodyPacker {

    /**
     * Single instance.
     */
    private static LiteralBodyPacker instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private LiteralBodyPacker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static LiteralBodyPacker getInstance() {
        if (instance == null) {
            instance = new LiteralBodyPacker();
        }
        return instance;
    }

    /**
     * Expected object of byte[] type.
     * @param object the byte[] source data to be packed.
     * @param bytes  empty or partially filled target bytes.
     * @param offset the index of the first unfilled byte in the target bytes array.
     */
    @Override
    public void pack(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return;
        }
        if (!(object instanceof byte[])) {
            throw new PackerRuntimeException("Expected array of bytes, but found " + object.getClass().getSimpleName());
        }
        byte[] data = (byte[]) object;
        int available = bytes.length - offset;
        if (data.length > available) {
            throw new PackerRuntimeException("Cannot pack data[] array to bytes[] array. " +
                "Available size in bytes[] array is '" + available +
                "', but required size is '" + data.length + "'.");
        }
        System.arraycopy(data, 0, bytes, offset, data.length);
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes to unpack.
     * @return The byte[] array.
     */
    @Override
    public byte[] unpack(byte[] sourceData, int offset, int bytesCount) {
        int available = sourceData.length - offset;
        if (bytesCount > available) {
            throw new PackerRuntimeException("Available number of bytes in sourceData[] array '" + available +
                "' is less than required bytesCount '" + bytesCount + "'");
        }
        byte[] ret = new byte[bytesCount];
        System.arraycopy(sourceData, offset, ret, 0, bytesCount);
        return ret;
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
            throw new PackerRuntimeException("Expected array of bytes, but found " + object.getClass().getSimpleName());
        }
        return ((byte[])object).length;
    }
}
