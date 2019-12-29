package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // TODO Kyrylo Semenko - examples
 * 
 * @author Kyrylo Semenko
 */
public class BcdLengthPacker implements LengthPacker {

    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, BcdLengthPacker> instances = new ConcurrentHashMap<>();

    /**
     * How many bytes this subfield occupies in the {@link com.credibledoc.iso8583packer.message.MsgValue} field.
     */
    private int numBytes;

    private BcdLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Static factory. Creates ans returns singletons.
     * @param numBytes see {@link #numBytes}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static BcdLengthPacker getInstance(int numBytes) {
        instances.computeIfAbsent(numBytes, k -> new BcdLengthPacker(numBytes));
        return instances.get(numBytes);
    }

    /**
     * @param lenLength is not used, the lenLength is defined in the {@link #BcdLengthPacker(int)} constructor.
     */
    @Override
    public byte[] pack(int bodyBytesLength, int lenLength) {
        String lenString = Integer.toString(bodyBytesLength);
        String hex = StringUtils.leftPad(lenString, numBytes * 2, '0');
        return HexService.hex2byte(hex);
    }

    /**
     * @param lenLength is not used, the lenLength is defined in the {@link #BcdLengthPacker(int)} constructor.
     */
    @Override
    public int unpack(byte[] messageBytes, int offset, int lenLength) {
        byte[] lenBytes = new byte[numBytes];
        System.arraycopy(messageBytes, offset, lenBytes, 0, lenBytes.length);
        return Integer.parseInt(HexService.bytesToHex(lenBytes));
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

    @Override
    public int calculateLenLength(int bodyBytesLength) {
        return numBytes;
    }
}
