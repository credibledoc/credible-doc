package com.credibledoc.iso8583packer.binary;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packs and unpacks {@link MsgValue#getBodyBytes()} length in the the binary format with fixed lenLength.
 * See the <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/binary/binary-length-packer.md">binary-length-packer.md</a> page.
 * @author Kyrylo Semenko
 */
public class BinaryLengthPacker implements LengthPacker {

    private static final int MAX_DECIMAL_IN_ONE_BYTE_255 = 255;
    private static final int NUM_DECIMALS_IN_ONE_BYTE_256 = 256;
    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, BinaryLengthPacker> instances = new ConcurrentHashMap<>();

    /**
     * How many bytes will be occupied with data about the field body length.
     */
    private int numBytes;

    private BinaryLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param numBytesParam see {@link #numBytes}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static BinaryLengthPacker getInstance(int numBytesParam) {
        instances.computeIfAbsent(numBytesParam, k -> new BinaryLengthPacker(numBytesParam));
        return instances.get(numBytesParam);
    }

    @Override
    public byte[] pack(int bodyBytesLength) {
        int maxPackedInt = (int)(Math.pow(16, numBytes * 2d) - 1);
        if (bodyBytesLength > maxPackedInt) {
            throw new PackerRuntimeException("The bodyBytesLength '" + bodyBytesLength +
                "' cannot be packed in bytes because it is greater than the maximum value '" + maxPackedInt +
                "' that can be packed in the bytes with the length '" + numBytes + "'.");
        }
        byte[] result = new byte[numBytes];
        for(int i = numBytes - 1; i >= 0; --i) {
            result[i] = (byte)(bodyBytesLength & MAX_DECIMAL_IN_ONE_BYTE_255);
            bodyBytesLength >>= 8;
        }
        return result;
    }

    @Override
    public int unpack(byte[] messageBytes, int offset) {
        int len = 0;
        for (int i = 0; i < this.numBytes; ++i) {
            len = NUM_DECIMALS_IN_ONE_BYTE_256 * len + (messageBytes[offset + i] & MAX_DECIMAL_IN_ONE_BYTE_255);
        }
        return len;
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

}
