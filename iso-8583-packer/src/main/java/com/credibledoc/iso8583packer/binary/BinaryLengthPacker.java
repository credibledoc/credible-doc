package com.credibledoc.iso8583packer.binary;

import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.length.LengthPacker;

/**
 * Calculates the {@link HeaderValue#getLengthBytes()} bytes and length for the {@link HeaderField#getLengthPacker()}.
 * // TODO Kyrylo Semenko - examples
 * @author Kyrylo Semenko
 */
public class BinaryLengthPacker implements LengthPacker {
    
    public static final BinaryLengthPacker B = new BinaryLengthPacker(1);
    public static final BinaryLengthPacker BB = new BinaryLengthPacker(2);

    /**
     * How many bytes will be occupied with data about the field body length.
     */
    private int nBytes;

    private BinaryLengthPacker(int nBytes) {
        this.nBytes = nBytes;
    }
    
    @Override
    public byte[] pack(int bodyBytesLength, Integer lenLength) {
        byte[] result = new byte[nBytes];
        for(int i = this.nBytes - 1; i >= 0; --i) {
            result[i] = (byte)(lenLength & 255);
            lenLength >>= 8;
        }
        return result;
    }

    @Override
    public int unpack(byte[] messageBytes, int offset, Integer lenLength) {
        int len = 0;
        for (int i = 0; i < this.nBytes; ++i) {
            len = 256 * len + (messageBytes[offset + i] & 255);
        }
        return len;
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return nBytes;
    }

    @Override
    public int calculateLenLength(int bodyBytesLength) {
        return nBytes;
    }
}
