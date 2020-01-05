package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link BitmapPacker} implementation for IFB format.
 *
 * @author Kyrylo Semenko
 * // TODO Kyrylo Semenko - documentation
 */
public class IfaBitmapPacker implements BitmapPacker {

    /**
     * Contains created instances. Each instance is a Singleton.
     */
    private static Map<Integer, IfaBitmapPacker> instances = new ConcurrentHashMap<>();

    /**
     * Number of bytes in a packed state.
     */
    private int packedBytesLength;

    private IfaBitmapPacker(int packedBytesLength) {
        this.packedBytesLength = packedBytesLength;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param packedBytesLength see {@link #packedBytesLength}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfaBitmapPacker getInstance(int packedBytesLength) {
        instances.computeIfAbsent(packedBytesLength, k -> new IfaBitmapPacker(packedBytesLength));
        return instances.get(packedBytesLength);
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        int len = packedBytesLength >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : packedBytesLength;
        return HexService.bytesToHex(BitmapService.bitSet2byte(bitSet, len)).getBytes();
    }

    /**
     * @param msgValue the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(MsgValue msgValue, byte[] bytes, int offset) {
        BitSet bmap = BitmapService.hex2BitSet(bytes, offset, packedBytesLength << 3);
        msgValue.setBitSet(bmap);
        int len = bmap.get(1) ? 128 : 64;
        if (packedBytesLength > 16 && bmap.get(65)) {
            len = 192;
            bmap.clear(65);
        }
        return Math.min(packedBytesLength << 1, len >> 2);
    }

}
